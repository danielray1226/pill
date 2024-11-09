package control;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TempFile {
	File file;
	int count = 1;
	public TempFile (File f) {
		file=f;
	}
	public synchronized TempFile incrementUsage () {
		count++;
		return this;
	}
	
	public synchronized void decrementUsage (){
		count--;
		if(count==0) {
			try {
			Files.delete(file.toPath());
			}
			catch( Exception e){
				e.printStackTrace();
			}
		}
	}
	public byte[] readAllBytes() throws IOException {
		return Files.readAllBytes(file.toPath());
	}
	public String getAbsolutePath () {
		return file.getAbsolutePath();
	}
}
