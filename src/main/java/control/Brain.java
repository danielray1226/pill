package control;

import misc.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import misc.EnvUtils;
import misc.JsonUtils;
import misc.ProcessMaster;
import mjpeg.Mjpeg;

public class Brain {
	static class Helper {
		static final Brain brain = initBrain();

		static Brain initBrain() {
			Brain b = new Brain();
			if (defaultroot == null) {
				String context = System.getenv("pillcontent");
				if (context == null) {
					throw new RuntimeException("Please define the environment variable pillcontent.");
				}
				b.init(context);
				return b;
			}

			b.init(defaultroot);
			return b;
		}
	}

	volatile JsonElement faceDetections;
	volatile long lastFaceDetectionMS;
	List<Dispenser> dispensers = new ArrayList<Dispenser>();
	ProcessMaster servoController;
	ProcessMaster irController;
	ScheduledExecutorService schedule = Executors.newSingleThreadScheduledExecutor();
	ExecutorService offloaded = Executors.newCachedThreadPool();
	
	ExecutorService soundExecutor = Executors.newSingleThreadExecutor();
	public void say(final String text) {
		soundExecutor.submit(new Callable<Void>() {
			public Void call() throws Exception {
				EnvUtils.espeak(text);
				return null;
			}
		});
	}
	
	
	public ExecutorService getExecutorService() {return offloaded;}
	ProcessMaster cameraMaster;
	ProcessMaster face;
	AtomicLong count = new AtomicLong();
	String dir = "/dev/shm/images";
	static volatile String defaultroot;
	String root;

	public static String getRoot() {

		if (defaultroot != null)
			return defaultroot;
		String context = System.getenv("pillcontent");
		if (context == null) {
			throw new RuntimeException("Please define the environment variable pillcontent.");
		}
		return context;
	}

	static Brain getBrain() {

		return Helper.brain;
	}

	public static void setDefaultRoot(String defroot) {
		defaultroot = defroot;
	}

	volatile boolean destroy = false;

	public void init(String root) {
		this.root = root;
		cameraMaster = new ProcessMaster("python3", root + "/WEB-INF/scripts/" + EnvUtils.getEnvName() + "/pict.py",
				dir);
		//face = new ProcessMaster("python3", root + "/WEB-INF/scripts/" + EnvUtils.getEnvName() + "/yoloface.py",
		//		root + "/WEB-INF/scripts/yolov8n-face.pt");
		face = new ProcessMaster("bash", Brain.getRoot() + "/WEB-INF/scripts/" + EnvUtils.getEnvName() + "/yoloface.sh",
				root + "/WEB-INF/scripts/yolov8n-face.pt");
		String subscript = Brain.getRoot() + "/WEB-INF/scripts/" + EnvUtils.getEnvName() + "/servo.sh";
		servoController = new ProcessMaster("sh", subscript);
		irController = new ProcessMaster("python3", root + "/WEB-INF/scripts/" + EnvUtils.getEnvName() + "/irdetect.py");
		
		
		schedule.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					tick();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}, 1000, 60, TimeUnit.MILLISECONDS);
		try {
			Files.createDirectories(Paths.get(dir));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		int[] irPins=new int[] {17,27,22,5};
		for (int i = 0; i < 4; i++) {
			dispensers.add(new Dispenser(i,irPins[i], this));
		}
		schedule.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					JsonObject message = new JsonObject();
					message.addProperty("type", "screensaver");
					message.addProperty("screensaver", ss);
					ss=!ss;
//					Broadcaster.broadcast(message);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}, 10, 10, TimeUnit.SECONDS);
	}
	static boolean ss = false;
	public boolean dispense(int servonum) throws IOException, InterruptedException, ExecutionException {
		return dispensers.get(servonum).dispense();
	}

	public ProcessMaster getServoController() {
		return servoController;
	}
	public ProcessMaster getIrController() {
		return irController;
	}
	
	public void destroy() {
		destroy = true;
		schedule.shutdownNow();
		cameraMaster.destroy();
		face.destroy();

	}

	public Brain() {

	}

	public static void main(String[] args) throws IOException {
		Brain b = new Brain();
	}

	public void tick() throws IOException {
		// System.out.println("tick-tock");
		if (destroy)
			return;
		JsonObject json = new JsonObject();
		JsonElement arr = JsonUtils.getJsonElement(faceDetections, "faces");
		if (arr != null && arr.isJsonArray()) {
			json.add("faces", arr);
		}
		JsonElement elem = cameraMaster.sendobject(json);
		String name = JsonUtils.getString(elem, "file");
		if (name == null) {
			return;
		}
		TempFile f = new TempFile(new File(name));
		sendToFaceDetection(f);
		// System.out.println(elem);
		byte[] img = f.readAllBytes();
		f.decrementUsage();
		Mjpeg.pushImage(img, "default");
		String aiFile = JsonUtils.getString(elem, "aiFile");
		if (aiFile != null) {
			TempFile aif = new TempFile(new File(aiFile));
			Mjpeg.pushImage(aif.readAllBytes(), "ai");
			aif.decrementUsage();
		}
	}

	volatile boolean isRunning = false;
	Object mutex = new Object();

	private void sendToFaceDetection(TempFile tempFile) {
		if (!isRunning) {
			tempFile.incrementUsage();
			offloaded.submit(new Runnable() {

				@Override
				public void run() {
					try {
						synchronized (mutex) {
							if (isRunning)
								return;
							isRunning = true;
						}
						JsonElement ai;
						JsonObject j = new JsonObject();
						j.addProperty("file", tempFile.getAbsolutePath());
						try {
							faceDetections = face.sendobject(j);
							lastFaceDetectionMS = System.currentTimeMillis();
							System.err.println(faceDetections);
							// System.out.println(ai);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						isRunning = false;

					} finally {
						tempFile.decrementUsage();
					}
				}
			});
		}
	}

}
