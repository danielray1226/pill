package control;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import misc.EnvUtils;
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
	List<Dispenser> dispensers = new ArrayList<Dispenser>();
	ProcessMaster servoController;
	ScheduledExecutorService schedule = Executors.newSingleThreadScheduledExecutor();
	ExecutorService offloaded = Executors.newCachedThreadPool();
	ProcessMaster pm;
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
		pm = new ProcessMaster("python3", root + "/WEB-INF/scripts/" + EnvUtils.getEnvName() + "/pict.py");
		face = new ProcessMaster("python3", root + "/WEB-INF/scripts/" + EnvUtils.getEnvName() + "/face.py",
				root + "/WEB-INF/scripts/haarcascade_frontalface_alt.xml");
		String subscript = Brain.getRoot() + "/WEB-INF/scripts/" + EnvUtils.getEnvName() + "/servo.sh";
		servoController = new ProcessMaster("sh", subscript);
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
		for (int i = 0; i < 4; i++) {
			dispensers.add(new Dispenser(i, this));
		}
	}

	public void dispense(int servonum) throws IOException, InterruptedException {
		dispensers.get(servonum).dispense();

	}

	public ProcessMaster getServoController() {
		return servoController;
	}

	public void destroy() {
		destroy = true;
		schedule.shutdownNow();
		pm.destroy();
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
		long amount = count.incrementAndGet();
		String name = dir + "/" + amount + ".jpeg";
		json.addProperty("file", name);
		JsonElement elem = pm.sendobject(json);
		TempFile f = new TempFile(new File(name));
		sendToFaceDetection(f);
		// System.out.println(elem);
		try { 
			byte[] img = f.readAllBytes();
		

		f.decrementUsage();

		Mjpeg.pushImage(img, "default");
		} catch (Exception e) {
			
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
							System.err.println(faceDetections);
							//System.out.println(ai);
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
