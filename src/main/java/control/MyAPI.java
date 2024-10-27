package control;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Servlet implementation class MyAPI
 */
@WebServlet("/MyAPIServlet")
public class MyAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MyAPI() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

		JsonElement je = JsonParser.parseReader(request.getReader());
		JsonObject obj = (JsonObject)je;
		String type = obj.get("type").getAsString();
		if ("dispense".equals(type)) {
			int servonum = obj.get("number").getAsInt();
			boolean success=false;
			try {
				Brain.getBrain().say("Dispencing from container "+(servonum+1));
				success=Brain.getBrain().dispense(servonum);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			if (success) Brain.getBrain().say("Successfuly dispenced from container "+(servonum+1));
			else  Brain.getBrain().say("Failed to dispence from container "+(servonum+1));
			
			JsonObject res = new JsonObject();
			res.addProperty("success", success);
			response.getOutputStream().write(res.toString().getBytes(StandardCharsets.UTF_8));
		} else if ("screensaverTouched".equals(type)) {
			Brain.getBrain().setPersonPresent(true);
		}
	}

}
