package control;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import misc.JsonUtils;
import misc.ProcessMaster;

public class Dispenser {
	int servonum;
	Brain brain;
	private int irPin;

	static final double DEG_PER_MS = 180.0 / 1300;
	static final double JERK_DEG_PER_MS = 180.0 / 1300;

	public Dispenser(int servonum, int irPin, Brain brain) {
		this.servonum = servonum;
		this.irPin = irPin;
		this.brain = brain;
	}

	public boolean dispense() throws IOException, InterruptedException, ExecutionException {
		ProcessMaster servoMaster = brain.getServoController();
		ProcessMaster irMaster = brain.getIrController();
		
		int currentMaxAngle = 165;
		while (currentMaxAngle < 190) {
			JsonObject json = new JsonObject();
			json.addProperty("angle", currentMaxAngle);
			json.addProperty("servonum", servonum);
			servoMaster.sendobject(json);
			long sleep = (long) (currentMaxAngle / DEG_PER_MS);
			System.out.println("sleeping: " + sleep + ", after signal to rotate to " + currentMaxAngle);
			Thread.sleep(sleep);
			
			int lastAngle=currentMaxAngle;
			for (int jerkInc : new int[] {7, -7, 7}) {
				int jerkAngle=currentMaxAngle+jerkInc;
				long jerkSleep=(long)(Math.abs(jerkAngle-lastAngle)/JERK_DEG_PER_MS);
				json.addProperty("angle", jerkAngle);
				System.out.println("jerk: "+json+", sleep: "+jerkSleep);
				servoMaster.sendobject(json);
				lastAngle=jerkAngle;
				Thread.sleep(jerkSleep);
				//json.addProperty("angle", currentMaxAngle);
				//servoMaster.sendobject(json);
				//Thread.sleep(jerkSleep);
			}
			
			//Thread.sleep(jerkSleep);
			// assume we had picked the pill
			json.addProperty("angle", 0);
			servoMaster.sendobject(json);

			Future<JsonElement> irFuture = brain.getExecutorService().submit(new Callable<JsonElement>() {
				@Override
				public JsonElement call() throws Exception {
					JsonObject irDetectCommand = new JsonObject();
					JsonArray pinsArray = new JsonArray();
					pinsArray.add(irPin);
					irDetectCommand.add("pins", pinsArray);
					irDetectCommand.addProperty("waitMs", sleep);
					System.out.println("IR detect command: " + irDetectCommand);

					JsonElement irReply = irMaster.sendobject(irDetectCommand);
					System.out.println("Got IR reply: " + irReply);
					return irReply;
				}
			});

			System.out.println("sleep+ing: " + sleep + ", after signal to go back to 0");
			Thread.sleep(sleep);
			// Lets analyse IR reply
			JsonElement irReply=irFuture.get();
			String pinStatus=JsonUtils.getString(irReply, ""+irPin);
			System.out.println("Got pin status: " + pinStatus);
			if ("active".equals(pinStatus)) {
				System.out.println("Yey! IR detected");
				return true;
			}
			currentMaxAngle += 4;
		}
		return false;
	}

	public static void main(String[] args) throws Exception {
		boolean su=Brain.getBrain().dispense(0);
		System.out.println("dispensed: "+su);
		System.exit(0);
	}

}
