package control;

import java.io.IOException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import misc.ProcessMaster;

public class Dispenser {
	int servonum;
	Brain brain;

	public Dispenser(int servonum, Brain brain) {
		this.servonum = servonum;
		this.brain = brain;
	}

	public void dispense() throws IOException, InterruptedException {
		ProcessMaster s = brain.getServoController();
		JsonObject json = new JsonObject();
		json.addProperty("angle", 180);
		json.addProperty("servonum", servonum);
		JsonElement elem = s.sendobject(json);
		Thread.sleep(1250);
		json.addProperty("angle", 0);
		s.sendobject(json);
		Thread.sleep(1250);
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		Brain.getBrain().dispense(0);
	}

}
