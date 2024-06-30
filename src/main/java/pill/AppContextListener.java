package pill;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {
	public void contextInitialized(javax.servlet.ServletContextEvent sce) {
		
		String s = sce.getServletContext().getRealPath("/");
		// ServletContext b = sce.getServletContext().getContext("/");
		// b.
		Pill.setRoot(s);
		System.out.println("Context Initialized: " + sce);
	}
}
