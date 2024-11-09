package misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.nio.channels.Channel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ProcessMaster implements AutoCloseable{
	List<String> arguments;
	Process process;
	DataInputStream dis;
	DataOutputStream dos;
	boolean manualClose = false;
	public ProcessMaster(String... args) {
		arguments = new ArrayList<String>();
		for (String i : args) {
			arguments.add(i);
		}
	}

	public ProcessMaster(List<String> args) {
		arguments = args;
	}

	public void bootstrap() throws IOException {
		ProcessBuilder b = new ProcessBuilder(arguments);
		b.redirectOutput(Redirect.PIPE);
		b.redirectError(Redirect.INHERIT);
		process = b.start();
		dis = new DataInputStream(process.getInputStream());
		dos = new DataOutputStream(process.getOutputStream());
	}

	byte[] readFull(int length) throws IOException {
		byte[] ret = new byte[length];
		int remaining = length;
		int offset = 0;
		for (;;) {
			int chunklength = dis.read(ret, offset, remaining);
			offset = offset + chunklength;
			remaining -= chunklength;
			if (remaining == 0)
				break;
		}
		return ret;
	}

	byte[] trysenddata(byte[] data) throws IOException {
		dos.writeInt(data.length);
		dos.write(data);
		dos.flush();
		int length = dis.readInt();
		byte[] buff = readFull(length);
		return buff;

	}

	public synchronized byte[] senddata(byte[] data) throws IOException {
		if (manualClose) {
			throw new IOException("Manually Closed");
		}
		IOException exc = null;
		for (int i = 0; i < 4; i++) {
			try {
				if (process == null) {
					bootstrap();
				}
				return trysenddata(data);
			} catch (IOException io) {
				exc = io;
				close();
			}
		}
		throw exc;
	}

	public JsonElement sendobject(JsonObject json) throws IOException {
		byte[] binaryret;
		if (json == null) {
			binaryret = senddata(new byte[0]);
		} else {
			binaryret = senddata(json.toString().getBytes(StandardCharsets.UTF_8));
		}
		if (binaryret.length == 0) {
			return null;
		}
		String str = new String(binaryret, StandardCharsets.UTF_8);
		return JsonParser.parseString(str);
	}
	public void destroy() {
		close();
		manualClose = true;
	}
	public void close() {
		if (process != null) {
			process.destroy();
			try {
				process.waitFor();
			} catch (InterruptedException e) {
			}
			try {
				if (dis != null)
					dis.close();
			} catch (IOException e) {
			}
			try {
				if (dos != null)
					dos.close();
			} catch (IOException e) {
			}
			process = null;
		}

	}

	public static void main(String[] args) throws IOException {
		String dir = "/tmp/images";
		Files.createDirectories(Paths.get(dir));
		try (ProcessMaster p = new ProcessMaster("python3", "/home/daniel/workspace/pill/stuff/pict.py")) {
			for (int i = 0; i < 10; i++) {
				JsonObject json = new JsonObject();
				json.addProperty("file", dir+"/"+i+".bmp");
				JsonElement elem = p.sendobject(json);
				System.out.println(elem);
			}
		} 
		System.out.println(" end ");
	}

}
