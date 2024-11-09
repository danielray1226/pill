package control;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import misc.EnvUtils;
import misc.JsonUtils;

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
		System.out.println("Got "+obj);
		String type = obj.get("type").getAsString();
		if ("dispense".equals(type)) {
			Brain.getBrain().userPressButton();
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
		} else if ("schedule".equals(type)) {
			String schedunit=JsonUtils.getString(obj, "schedunit");
			// {"type":"schedule","schedunit":"never","schedWakeBefore":"20","schedWaitAfter":"20","schedstart":""}
			if ("never".equals(schedunit)) {
				Brain.getBrain().say("Dispense skedule will not be used");
				Brain.getBrain().cancelScheduler();
			} else {
				 String schedWakeBefore=JsonUtils.getString(obj, "schedWakeBefore");
				 String schedWaitAfter=JsonUtils.getString(obj, "schedWaitAfter");
				 String schedWakeBeforeUnit=JsonUtils.getString(obj, "schedWakeBeforeUnit");
				 String schedWaitAfterUnit=JsonUtils.getString(obj, "schedWaitAfterUnit");
				 
				 if (schedWakeBefore.isEmpty()) schedWakeBefore="1";
				 if (schedWaitAfter.isEmpty()) schedWaitAfter="1";
				 
				 String schedevery=JsonUtils.getString(obj, "schedevery");
				 if (schedevery.isEmpty()) schedevery="1";

				 
				 double wakeBefore=Double.parseDouble(schedWakeBefore);
				 double waitAfter=Double.parseDouble(schedWaitAfter);
				 double every=Double.parseDouble(schedevery);
				 String text="Dispense schedule will run every "+schedevery+" "+schedunit+(schedevery.equals("1")?"":"s");
				 //Brain.getBrain().say(text);
				 text="I will remind you "+schedWakeBefore+" "+schedunit+(schedWakeBefore.equals("1")?"":"s")+" before each scheduled despense";
				 //Brain.getBrain().say(text);
				 text="And, I will remind you up to "+schedWaitAfter+" "+schedunit+(schedWaitAfter.equals("1")?"":"s")+" after each scheduled despense, if medicine is not taken";
				 //Brain.getBrain().say(text);

				 Date start=new Date();
				 
				 String schedstart=JsonUtils.getString(obj, "schedstart");
				 if (schedstart.isEmpty()) {
					 Brain.getBrain().say("Initiating dispense scheduler now");	 
				 } else {
					 schedstart=schedstart.replace('T', ' ');
					 SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm");//"2024-11-01T21:08"
					 try {
						start = dateFormat.parse(schedstart);
						System.out.println(start);
						SimpleDateFormat sayDateFormat=new SimpleDateFormat("EEEE, h:mm a");
						String time=sayDateFormat.format(start);
						Brain.getBrain().say("Initiating dispense scheduler on "+time);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				 }
				 String numTreatments=JsonUtils.getString(obj, "numTreatments");
				 if (numTreatments.isEmpty()) numTreatments="1";
				 int num=Integer.parseInt(numTreatments);
					// cast time unit to ms
				 if ("second".equals(schedunit)) {
					 every*=1000;
				 } else if ("minute".equals(schedunit)) {
					 every*=1000*60;
				 } else if ("hour".equals(schedunit)) {
					 every*=1000*60*60;						 
				 } else if ("day".equals(schedunit)) {
					 every*=1000*24*60*60;						 
				 } else if ("week".equals(schedunit)) {
					 every*=1000*7*24*60*60;						 
				 }
				 if ("second".equals(schedWakeBeforeUnit)) {
					 wakeBefore*=1000;
				 } else if ("minute".equals(schedWakeBeforeUnit)) {
					 wakeBefore*= 1000*60;
				 } else if ("hour".equals(schedWakeBeforeUnit)) {
					 wakeBefore*=1000*60*60;						 
				 }
				 if ("second".equals(schedWaitAfterUnit)) {
					 waitAfter*=1000;
				 } else if ("minute".equals(schedWaitAfterUnit)) {
					 waitAfter*=1000*60;
				 } else if ("hour".equals(schedWaitAfterUnit)) {
					 waitAfter*=1000*60*60;
					 
				 } 
				 
				 try {
					Brain.getBrain().setScheduler(start, num, (long)every, (long)wakeBefore, (long)waitAfter);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 
				//"schedWakeBefore":"20","schedWaitAfter":"20"
				 //{"type":"schedule","schedunit":"minute","schedWakeBefore":"20","schedWaitAfter":"20","schedstart":"2024-11-01T21:08"}
				 
			}
			/*"schedWakeBefore": schedWakeBefore.value,
			"schedWaitAfter": schedWaitAfter.value,
			"schedstart" : schedstart.value*/
		}  else if ("listWifi".equals(type)) {
			JsonArray wifis=new JsonArray();
			for (String wifi : EnvUtils.listWifi()) {
				wifis.add(wifi);
			}
			response.getOutputStream().write(wifis.toString().getBytes(StandardCharsets.UTF_8));
		}  else if ("currentWifi".equals(type)) {
			JsonObject ret=new JsonObject();
			ret.addProperty("wifi", EnvUtils.currentWifi());
			ret.addProperty("ip", EnvUtils.currentIp());
			response.getOutputStream().write(ret.toString().getBytes(StandardCharsets.UTF_8));
		}  else if ("setWifi".equals(type)) {
			//var data = { "type": "setWifi", "wifi" : document.getElementById('wifisId').value, "password":	
			String wifi=JsonUtils.getString(obj, "wifi");
			String password=JsonUtils.getString(obj, "password");
			System.out.println(""+wifi+" : "+password);
			try {
				EnvUtils.connectWifi(wifi, password);
				JsonObject ret=new JsonObject();
				ret.addProperty("success", true);
				response.getOutputStream().write(ret.toString().getBytes(StandardCharsets.UTF_8));
			} catch (Exception e) {
				e.printStackTrace();
				JsonObject ret=new JsonObject();
				ret.addProperty("success", false);
				ret.addProperty("message", e.getMessage());
				response.getOutputStream().write(ret.toString().getBytes(StandardCharsets.UTF_8));
			}
		}  else if ("setPillWifi".equals(type)) {
			String password=JsonUtils.getString(obj, "password");
			try {
				EnvUtils.pillWifi(password);
				JsonObject ret=new JsonObject();
				ret.addProperty("success", true);
				response.getOutputStream().write(ret.toString().getBytes(StandardCharsets.UTF_8));
			} catch (Exception e) {
				e.printStackTrace();
				JsonObject ret=new JsonObject();
				ret.addProperty("success", false);
				ret.addProperty("message", e.getMessage());
				response.getOutputStream().write(ret.toString().getBytes(StandardCharsets.UTF_8));
			}
		}
		
		
		
		 
	}

}
