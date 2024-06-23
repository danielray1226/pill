package mjpeg;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.image.BufferedImage;

/**
 * Servlet implementation class StreamerServlet
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/stream/*" })
public class Streamer extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Streamer() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("Path: " + request.getPathInfo());
		/*
		response.getWriter().append("getContextPath : ").append(request.getContextPath());
		response.getWriter().append("\n");
		response.getWriter().append("getPathInfo : ").append(request.getPathInfo());
		response.getWriter().append("\n");
		response.getWriter().append("Served at: ").append(request.getContextPath());
		response.getWriter().append("\n");
		*/
		
	    final long startTime = System.nanoTime();
	    final AsyncContext asyncContext = request.startAsync(request, response);
	    asyncContext.setTimeout(1000000000000L);
	    new Thread() {

	        @Override
	        public void run() {
	          try {
	            ServletResponse response = asyncContext.getResponse();
	            //response.setContentType("text/plain");
	           // Thread.sleep(2000);
	            simulate((HttpServletResponse)response);
	            //PrintWriter out = response.getWriter();
	            //response.
	            
	            //out.print("Work completed.   Time elapsed: " + (System.nanoTime() - startTime));
	            //out.flush();
	            asyncContext.complete();
	          } catch (IOException | InterruptedException e) {
	        	  e.printStackTrace();
	        	  asyncContext.complete();
	            throw new RuntimeException(e);
	          }
	        }
	      }.start();
	    
	}
	

	static byte[] getNextImage() throws IOException {
		BufferedImage bufferedImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		Graphics2D g2d = bufferedImage.createGraphics();
		Font font = new Font("Georgia", Font.BOLD, 18);
		g2d.setFont(font);
		
		g2d.setBackground(java.awt.Color.black);
		Random rand = new Random();
		
		int r=(int) (rand.nextFloat()*255);
		int g=(int) (rand.nextFloat()*255);
		int b=(int) (rand.nextFloat()*255);
		
		g2d.setColor(new java.awt.Color(r,g,b));
		g2d.fillRect(50 , 50,  100, 100);
		
		ImageIO.write(bufferedImage, "jpg", bos);
		return bos.toByteArray();
		
	}
	static void simulate(HttpServletResponse response) throws IOException, InterruptedException {
		//response.setContentType("multipart/x-mixed-replace; boundary=danielboundary");//"image/jpeg");
		response.setHeader("Content-Type", "multipart/x-mixed-replace; boundary=danielboundary");//"image/jpeg");
		response.setHeader("Connection", "Keep-Alive");
		ServletOutputStream o = response.getOutputStream();
		//o.write("Connection: Keep-Alive\r\n".getBytes());//"image/jpeg");
		//o.write("Content-Type: multipart/x-mixed-replace; boundary=myframe\r\n\r\n".getBytes());//"image/jpeg");
		//o.flush();


		boolean firstTime=true;
		for (;;) {
			byte[] img=getNextImage();
			int size=img.length;
			/*
			o.write(("--danielboundary\r\nContent-Type: image/jpeg\r\nContent-Size: "+size+"\r\n\r\n").getBytes());
			o.write(img);
			o.write("\r\n".getBytes());
			*/
			o.write(("Content-Type: image/jpeg\r\nContent-Size: "+size+"\r\n\r\n").getBytes());
			o.write(img);
			o.write("\r\n--danielboundary\r\n".getBytes());
			/*if (firstTime) {
				o.write("\r\n--danielboundary\r\n".getBytes());
				firstTime=false;
			}*/
			
			//o.write(("Content-Type: image/jpeg\r\nContent-Size: ").getBytes());
			
			//o.write(("Content-Type: image/jpeg\r\nContent-Size: "+size+"\r\n\r\n").getBytes());
			
			//o.write("\r\n--danielboundary\r\n".getBytes());
			//o.write("\r\n--danielboundary\r\n".getBytes());
			
			response.flushBuffer();
			Thread.sleep(10);
		}
		
		//o.flush();
		//o.close();
			
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

