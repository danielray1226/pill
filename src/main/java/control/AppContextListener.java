package control;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {
	static Brain brain;
	public void contextInitialized(javax.servlet.ServletContextEvent sce) {
		
		String s = sce.getServletContext().getRealPath("/");
		// ServletContext b = sce.getServletContext().getContext("/");
		// b.
		brain = new Brain();
		brain.init(s);
		System.out.println("Context Initialized: " + sce);
	}
	public void contextDestroyed(javax.servlet.ServletContextEvent sce) {
		brain.destroy();
	}
}
