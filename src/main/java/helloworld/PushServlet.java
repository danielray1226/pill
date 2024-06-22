package helloworld;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class PushServlet
 */
@WebServlet(urlPatterns = { "/PushServlet"} )
public class PushServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PushServlet() {
    	
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	            response.setContentType("text/plain");
	            
	            try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	           
	            PrintWriter out = response.getWriter();
	            String resp="{\"showScreen\":\"" + nextScreen()+ "\"}";
	            out.print(resp);
	            out.flush();
	            System.out.println("replyed at "+new java.util.Date()+" with "+resp);

		
	}
	static String[] names=new String[] {"welcomeId", "dispensingScreenId", "Schedule", "Settings"};
	static int current=0;
	static String nextScreen() {
		++current;
		if (current>=names.length) current=0;
		return names[current];
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
