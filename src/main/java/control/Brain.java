package control;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import misc.ProcessMaster;
import mjpeg.Mjpeg;

public class Brain {
	ScheduledExecutorService schedule = Executors.newSingleThreadScheduledExecutor();
	ProcessMaster pm;
	AtomicInteger count = new AtomicInteger();
	String dir = "/tmp/images";
	String root;
	volatile boolean destroy = false;

	
	public void init(String root) {
		this.root = root;
		pm = new ProcessMaster("python3", root + "/WEB-INF/scripts/pict.py");
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

	}

	public void destroy() {
		destroy = true;
		schedule.shutdownNow();
		pm.destroy();

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
		int amount = count.incrementAndGet();
		String name = dir + "/" + amount + ".jpeg";
		json.addProperty("file", name);
		JsonElement elem = pm.sendobject(json);
		// System.out.println(elem);
		byte[] img = Files.readAllBytes(new File(name).toPath());
		Files.delete(new File(name).toPath());
		Mjpeg.pushImage(img, "default");
	}
}
