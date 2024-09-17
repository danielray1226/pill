package control;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

public class BConnection {
	AsyncContext asyncContext;
	boolean isClosed = false;

	BConnection(AsyncContext a) throws IOException {
		asyncContext = a;
		asyncContext.getRequest().getInputStream().setReadListener(readListener);
		asyncContext.addListener(asyncListener);

	}

	LinkedList<JsonObject> messagesToWrite = new LinkedList<>();
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

	public void sendMessage(JsonObject message) throws IOException {
		send(message, true);
	}

	public void send(JsonObject message, boolean sendout) throws IOException {
		synchronized (this) {
			messagesToWrite.add(message);
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
		Broadcaster.cleanup(this);
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


			if (outputStream.isReady() && !messagesToWrite.isEmpty() && !isClosed) {

				JsonObject first = messagesToWrite.removeFirst();
				

				byte[] b = first.toString().getBytes(StandardCharsets.UTF_8);
				try {
					outputStream.write(b);
				} catch (Exception e) {
					errorOut = true;
				}
				asyncContext.complete();
				// System.out.println("writing first extract");
				// outputStream.flush();

			}
		}
		if (errorOut) {
			// cleanup();
		}

	}

	public void setupReply() throws IOException {
		asyncContext.getResponse().getOutputStream().setWriteListener(writeListener);
		HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
		response.setContentType("application/json");
		response.setHeader("Connection", "Keep-Alive");

	}

}
