package misc;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;

public class EnvUtils {
	public static String getEnvName() {
		return EnvHelper.env;
		
	}
	static class EnvHelper {
		static final String env = init();
		static String init() {
			if ("aarch64".equals(Helper.arch)) {
				return "real";
			}
			else return "simulation";
			
		}
	}
	static class Helper {
		static final String arch = getArch();
	}
	static String getArch(){ 
		try {
		ProcessBuilder b = new ProcessBuilder("uname",  "-m");
		b.redirectOutput(Redirect.PIPE);
		b.redirectError(Redirect.INHERIT);
		Process process = b.start();
		try {
			
		
		try (BufferedReader buffread = new BufferedReader(new InputStreamReader(process.getInputStream()))) { 
			return buffread.readLine();
		}
		}
		finally {
			if (process.waitFor() != 0) throw new RuntimeException("uname is not available.");
			
		}
		}
		catch (Exception e) {
			throw new RuntimeException ("Error: "+  e.getMessage());
		}
	}

	public static void espeak(String text) {
		try {
			// espeak -v en+f5 -s130 "outch, it seems to be a dispense issue, haha haha haha, now, go away, and never come back"
			ProcessBuilder b = new ProcessBuilder("espeak", "-v", "en+f2", "-s150", text);
			b.redirectOutput(Redirect.INHERIT);
			b.redirectError(Redirect.INHERIT);
			Process process = b.start();
			if (process.waitFor() != 0) throw new RuntimeException("espeak error");
		} catch (Exception e) {
			throw new RuntimeException("Error: " + e.getMessage());
		}
	}
	
	public static void main(String[] args) {
		System.out.println("(" + EnvUtils.getEnvName() + ")");
	}

}
