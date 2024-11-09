package misc;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;

import control.Brain;

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
	
	public static List<String> runScript(String script) {
		try {
			String subscript = Brain.getRoot() + "/WEB-INF/scripts/" + EnvUtils.getEnvName() + "/"+script;
			ProcessBuilder b = new ProcessBuilder("/bin/bash", subscript);
			b.redirectOutput(Redirect.PIPE);
			b.redirectError(Redirect.INHERIT);
			Process process = b.start();
			try {

				try (BufferedReader buffread = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
					String line=null;
					List<String> ret=new ArrayList<String>();
					while ( (line=buffread.readLine()) !=null) {
						line=line.trim();
						if (!line.isEmpty()) ret.add(line);
					}
					return ret;
				}
			} finally {
				if (process.waitFor() != 0)
					throw new RuntimeException("failed to run script "+script);

			}
		} catch (Exception e) {
			throw new RuntimeException("Error: " + e.getMessage());
		}
	}
	public static List<String> listWifi() {
		return runScript("list_wifi.sh");
	}
	public static String currentWifi() {
		 List<String> lst = runScript("current_wifi.sh");
		 return  (lst.size()>0)? lst.get(0) : null;
	}
	public static String currentIp() {
		 List<String> lst = runScript("current_ip.sh");
		 return  (lst.size()>0)? lst.get(0) : null;
	}
	

	
	public static void main(String[] args) {
		System.out.println("(" + EnvUtils.getEnvName() + ")");
		System.out.println(listWifi());
		System.out.println(currentWifi());
	}

	public static void connectWifi(String wifi, String password) throws IOException{
		String wpa_supplicant_template = Brain.getRoot() + "/WEB-INF/scripts/" + EnvUtils.getEnvName() + "/wpa_supplicant.conf";
		String wpa=new String(Files.readAllBytes(new File(wpa_supplicant_template).toPath()), StandardCharsets.UTF_8);
		wpa=wpa.replace("__WIFI__", wifi);
		wpa=wpa.replace("__PASSWORD__", password);
		Files.write(new File("/tmp/wpa_supplicant.conf").toPath(), wpa.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.CREATE);
		runScript("set_wifi.sh");
		Files.deleteIfExists(new File("/tmp/wpa_supplicant.conf").toPath());
	}

	public static void pillWifi(String password) throws IOException{
		String hostapd_template = Brain.getRoot() + "/WEB-INF/scripts/" + EnvUtils.getEnvName() + "/hostapd.conf";
		String hostapd=new String(Files.readAllBytes(new File(hostapd_template).toPath()), StandardCharsets.UTF_8);
		hostapd=hostapd.replace("__PASSWORD__", password);
		Files.write(new File("/tmp/hostapd.conf").toPath(), hostapd.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.CREATE);
		runScript("set_hostapd.sh");
		Files.deleteIfExists(new File("/tmp/hostapd.conf").toPath());
	}

}
