package mjpeg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;

class Chunk {
	LinkedList<byte[]> data = new LinkedList<>();
	static byte[] contentType = "Content-Type: image/jpeg\r\n\r\n".getBytes();
	static byte[] boundary = "\r\n--danielboundary\r\n".getBytes();

	Chunk(byte[] myData) {
		data.add(myData);
		data.add(boundary);
		data.add(contentType);
	}
	Chunk(){
		data.add(contentType);
	}
	boolean isWhole () { 
		return data.size() == 3;
	}
	boolean isEmpty() {
		return data.isEmpty();

	}

	byte[] extract() {
		return data.removeFirst();
	}
}

public class ImagePusher {
	AsyncContext asyncContext;
	boolean headerssent = false;
	String cam;
	boolean isClosed = false;

	ImagePusher(AsyncContext a, String camname) throws IOException {
		cam = camname;
		asyncContext = a;
		Chunk newchunk = new Chunk();
		dataToWrite.add(newchunk);
		pushImg(catjpeg, false);
		pushImg(catjpeg, false);
		asyncContext.getRequest().getInputStream().setReadListener(readListener);
		asyncContext.addListener(asyncListener);

	}

	LinkedList<Chunk> dataToWrite = new LinkedList<>();
	ReadListener readListener = new ReadListener() {

		@Override
		public void onAllDataRead() throws IOException {
			setupReply();

		}

		@Override
		public void onDataAvailable() throws IOException {
			ServletInputStream sis = asyncContext.getRequest().getInputStream();
			byte[] buffer = new byte[4096];
			while (sis.isReady()) {
				sis.read(buffer);
			}
		}

		@Override
		public void onError(Throwable t) {
			// TODO Auto-generated method stub
			cleanup();
		}

	};
	WriteListener writeListener = new WriteListener() {

		@Override
		public void onError(Throwable t) {
			// TODO Auto-generated method stub
			cleanup();
		}

		@Override
		public void onWritePossible() throws IOException {
			sendout();
		}

	};
	AsyncListener asyncListener = new AsyncListener() {

		@Override
		public void onComplete(AsyncEvent arg0) throws IOException {
			System.out.println("COMPLETE!!!");
			cleanup();
		}

		@Override
		public void onError(AsyncEvent arg0) throws IOException {
			System.out.println("ERROR!!!");
			cleanup();

		}

		@Override
		public void onStartAsync(AsyncEvent arg0) throws IOException {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTimeout(AsyncEvent arg0) throws IOException {
			System.out.println("TIMEDOUT!!!");
			cleanup();
		}

	};

	public void pushImg(byte[] img) throws IOException {
		pushImg(img, true);
	}

	public void pushImg(byte[] img, boolean sendout) throws IOException {
		// o.write(("Content-Type: image/jpeg\r\nContent-Size:
		// "+size+"\r\n\r\n").getBytes());
		// o.write(("Content-Type: image/jpeg\r\n\r\n").getBytes());
		// o.write(img);
		// o.write("\r\n--danielboundary\r\n".getBytes());
		synchronized (this) {
			// dataToWrite.add(("Content-Type: image/jpeg\r\nContent-Size: " + img.length +
			// "\r\n\r\n").getBytes());
			/*
			 * ListIterator<Chunk> i = dataToWrite.listIterator(); while(i.hasNext()) {
			 * Chunk chunk = i.next();
			 * 
			 * }
			 */
			//if (dataToWrite.element())
			ListIterator<Chunk> it = dataToWrite.listIterator();
			//boolean nuke = false;
			int count = 0;
			while(it.hasNext()) {
				Chunk current = it.next();
				if (count >2) {
					it.remove();
					continue;
				}
				if (current.isWhole()) {
					//nuke = true; 
					count++;
				}
			}
			dataToWrite.add(new Chunk(img));
			
			// dataToWrite.add(new Chunk(null));
		}

		try {
			if (sendout) {
				sendout();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		;

	}

	void cleanup() {
		synchronized (this) {
			if (isClosed) {
				return;
			}
			isClosed = true;
		}
		Mjpeg.cleanup(cam, this);
		try {
			asyncContext.complete();
		} catch (Exception e) {
		}

	}

	boolean firstreply = true;

	public void sendout() throws IOException {
		boolean errorOut = false;
		synchronized (this) {
			ServletOutputStream outputStream = asyncContext.getResponse().getOutputStream();
			if (firstreply) {
				HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
				response.setHeader("Content-Type", "multipart/x-mixed-replace; boundary=danielboundary");// "image/jpeg");
				response.setHeader("Connection", "Keep-Alive");
				
				firstreply = false;
			}
			while (outputStream.isReady() && !dataToWrite.isEmpty() && !isClosed) {


				Chunk first = dataToWrite.getFirst();
				if (first.isEmpty()) {
					dataToWrite.removeFirst();
					continue;
				}
				byte[] b = first.extract();
				try {
				outputStream.write(b);
				}
				catch(Exception e) {
					errorOut=true;
					break;
				}
				// System.out.println("writing first extract");
				// outputStream.flush();

			}
		}
		if (errorOut) {
			//cleanup();
		}

	}

	public void setupReply() throws IOException {
		asyncContext.getResponse().getOutputStream().setWriteListener(writeListener);
		//pushImg(catjpeg);
		//pushImg(catjpeg);
	}

	static String catb64 = "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";
	public static byte[] catjpeg = Base64.getDecoder().decode(catb64);
}
