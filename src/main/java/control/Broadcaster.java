package control;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.JsonObject;

import control.Brain;

/**
 * Servlet implementation class Broadcaster
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/messages/*" })
public class Broadcaster extends HttpServlet {
	static Set<BConnection> connections = new HashSet<>();

	static void cleanup(BConnection connection) {
		synchronized (connections) {
				if (connections != null) {
				System.out.println("cleanup: "	+ connection);
				connections.remove(connection);
			}
		}
	}
	
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Broadcaster() {
		
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		AsyncContext asyncContext = request.startAsync(request, response);
		asyncContext.setTimeout(1000000000000L);
		BConnection connect = new BConnection(asyncContext);
		synchronized (connections) {
			connections.add(connect);
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

	static public void broadcast(JsonObject message) throws IOException {
		synchronized (connections) {
			for (BConnection i : connections) {
				i.sendMessage(message);
			}
		}
	}

}
