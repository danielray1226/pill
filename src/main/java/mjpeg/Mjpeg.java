package mjpeg;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import control.Brain;

/**
 * Servlet implementation class mjpeg
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/mjpeg/*" })
public class Mjpeg extends HttpServlet {
	static Map<String, Set<ImagePusher>> track = new HashMap<>();

	static void cleanup(String cameraname, ImagePusher img) {
		synchronized (track) {
			Set<ImagePusher> mySet = track.get(cameraname);
			if (mySet != null) {
				System.out.println("cleanup: "	+ img);
				mySet.remove(img);
			}
		}
	}

	static Brain b = new Brain();
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Mjpeg() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String path = request.getPathInfo();
		if (path != null) {
			path = path.substring(1);
		} else {
			path = "default";
		}
		AsyncContext asyncContext = request.startAsync(request, response);
		asyncContext.setTimeout(1000000000000L);
		ImagePusher img = new ImagePusher(asyncContext, path);
		synchronized (track) {
			Set<ImagePusher> mySet = track.get(path);
			if (mySet == null) {
				mySet = new HashSet<>();
				track.put(path, mySet);
			}
			mySet.add(img);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	static public void pushImage(byte[] img, String camera) throws IOException {
		synchronized (track) {
			Set<ImagePusher> pushers = track.get(camera);
			if (pushers == null) {
				return;
			}
			for (ImagePusher i : pushers) {
				i.pushImg(img);
			}
		}
	}

}
