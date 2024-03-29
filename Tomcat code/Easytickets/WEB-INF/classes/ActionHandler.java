
package easytickets;

import utils.Mail;

import javax.servlet.http.*;
import javax.servlet.annotation.MultipartConfig;
import java.io.*;
import java.nio.file.Files;
import java.util.*; 
import org.json.JSONObject;
import org.json.JSONArray;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import utils.Util;
import java.nio.charset.StandardCharsets;
import com.oreilly.servlet.MultipartRequest;
  
@MultipartConfig(
    maxFileSize = 80485760, // Set to 10 MB in bytes (adjust as needed)
    maxRequestSize = 80971520, // Set to 20 MB in bytes (adjust as needed)
    fileSizeThreshold = 3048576 // 1 MB in bytes
)

public class ActionHandler extends Util
{
	private static volatile ActionHandler instance = null;

    public ActionHandler() {
    }

    public static ActionHandler getInstance() {
        if (instance == null) {
            Class var0 = ActionHandler.class;
            synchronized(ActionHandler.class) {
                if (instance == null) {
                    instance = new ActionHandler();
                }
            }
        }

        return instance;
    }
	
	public void sendOTP(HttpServletRequest request, HttpServletResponse response)throws Exception 
	{
		try{
			JSONObject result=new JSONObject();
			String status="",message="Error Occurred!!Check your credentials!";
			String mailId = request.getParameter("mailId");
			boolean existingUser=true;
			
			Random random = new Random();
			String oneTimePassword = String.valueOf(random.nextInt(9000) + 1000);
			String content="Please enter the below OTP to continue.<table width='750px' border='0' cellspacing='0' cellpadding='0'><tbody><tr><td background='https://i.postimg.cc/SRG7Mhp5/OTPTemplate.jpg'  style='background-repeat:no-repeat' width='100%' height='750' valign='top' class='bgresize'><div><table width='50%' border='0' cellspacing='0' cellpadding='0'><tbody><tr><td align='left' valign='top' class='mobile-padding'><table width='100%' border='0' cellspacing='0' cellpadding='0'><tbody><tr><td align='left' valign='top' style='padding-top: 220px;padding-left:215px;font-size:45px;color: #1F4167;letter-spacing: 14px;' class='padding65'><span class='banner-heading55'>"+oneTimePassword+"</span></td></tr></tbody></table></td></tr></tbody></table></div></td></tr></tbody></table>";
			status=Mail.sendMail(mailId,content);
			
			if(status.equals("success"))
			{
				UpdateHandler.addOtpDetails(mailId,oneTimePassword);
				result.put("userMail",mailId);
				result.put("OTP",oneTimePassword);
				message="OTP send Successfully!!";
			}
			result.put("message",message);
			result.put("status",status);
			PrintWriter out=response.getWriter();
			out.print(result);
		}
		catch(Exception e){
			e.printStackTrace();
		}
    }
	
	 public static String generateCsrfToken(SecretKey secretKey) {
		 
		long expirationTime = 20 * 60 * 1000;
        return Jwts.builder()
                .signWith(secretKey)
				.setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .compact();
    }
	
	public void authenticateUser(HttpServletRequest request, HttpServletResponse response)throws Exception 
	{
		JSONObject result=new JSONObject();
		String status="error",message="Login failed..Please try again later!..";
		try{
			String credential = request.getParameter("mailId"),credentail_type = "mobile",
			  oneTimePassword = request.getParameter("OTP");
			
			 if(credential.contains("@"))
			   {credentail_type="mail";}
			
			result=ViewHandler.verifyOTP(credentail_type,credential,oneTimePassword);
			status=result.getString("status");
			if(status.equals("success"))
			{
				int atIndex = credential.indexOf('@');
				String username = credential.substring(0, atIndex);
				Properties props=new Properties();
				props.put("Name",username);
				props.put("Mail",credential);
				props.put("Mobile","");
				props.put("acc_type","User");
				
				String userId=UpdateHandler.addUser(props);
				if(userId!=null || !userId.equals(""))
				{
					HttpSession session = request.getSession(true);
					session.setAttribute("userId", userId);
					SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
					session.setAttribute("csrf_key",secretKey);
					String csrfToken = generateCsrfToken(secretKey);
					session.setAttribute("csrfToken", csrfToken);
					result.put("csrfToken",csrfToken);
					result.put("JSESSIONID",session.getId());
					//String cookieValue = "username=" + username + ";JSESSIONID="+session.getId()+"; Path=/;SameSite=None";
					// Add the Set-Cookie header to the response
					//response.setHeader("Set-Cookie", cookieValue);
					
					 Cookie cookie = new Cookie("JSESSIONID", session.getId());
					 cookie.setSecure(false); // For HTTPS
					 cookie.setHttpOnly(false);
					//cookie.setSameSite("None"); // For cross-site requests
					response.addCookie(cookie);
					JSONObject userInfo=ViewHandler.getUserInfo(userId);
					result.put("acc_type",userInfo.getString("acc_type"));
					result.put("userId",userId);
					message="Login Successfully...";
				}
			}
			else
			{
				message=result.getString("message");
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		result.put("status",status);
		result.put("message",message);
		
		PrintWriter out=response.getWriter();
		out.print(result);
    }
	
	public void verifyCredentials(HttpServletRequest request,HttpServletResponse response)throws Exception
	{
		try{
			JSONObject params=new JSONObject(request.getParameter("params"));
			String credential  = params.getString("credential"),
			   credentail_type = "mail",
			   otp             = params.getString("otp");
			   
			JSONObject result=ViewHandler.verifyOTP(credentail_type,credential,otp);
		    PrintWriter out=response.getWriter();
			out.print(result);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void getUserInfo(HttpServletRequest request,HttpServletResponse response)throws Exception
	{
		JSONObject userInfo=new JSONObject();
		try
		{
			String user_id=request.getParameter("user_id");
			userInfo=ViewHandler.getUserInfo(user_id);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		PrintWriter out=response.getWriter();
		out.print(userInfo);		
	}
	
	public void updateProfilePic(HttpServletRequest request,HttpServletResponse response)throws Exception
	{
		JSONObject obj=new JSONObject();
		try
		{
			
			JSONObject dir_details=checkApacheDirectory();
			if(dir_details.getString("status").equals("success"))
			{
				String path=dir_details.getString("path");
				MultipartRequest m=new MultipartRequest(request,path,10 * 1024 * 1024);
				String user_id =  m.getParameter("data");
				Enumeration files = m.getFileNames();
				while (files.hasMoreElements()) {
					String name = (String) files.nextElement();
					String filename = m.getFilesystemName(name);

					File file = m.getFile(name);
					byte[] fileBytes = Files.readAllBytes(file.toPath());
					obj=UpdateHandler.updateProfilePicture(fileBytes,user_id);
				}
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		PrintWriter out=response.getWriter();
		out.print(obj);
	}
	
	
	public void hostRequest(HttpServletRequest request, HttpServletResponse response)throws Exception
	{
		try{
			JSONObject params=new JSONObject(request.getParameter("params"));
			params=UpdateHandler.addNewhostRequest(params);
			PrintWriter out=response.getWriter();
			out.print(params);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void getManageAccLayoutDetails(HttpServletRequest request,HttpServletResponse response)throws Exception
	{
		try{
			
			JSONObject layoutDetails=new JSONObject();
			
			ArrayList<String> cols = new ArrayList<String>();
			cols.add("Name");
			cols.add("Mobile");
			cols.add("Mail");
			cols.add("City");
			cols.add("State");
			cols.add("Pincode");
			cols.add("Acc Type");
			layoutDetails.put("acc_cols",cols);
			
			cols=new ArrayList<String>();
			cols.add("Name");
			cols.add("Mobile");
			cols.add("Mail");
			cols.add("TheatreName");
			cols.add("City");
			cols.add("State");
			cols.add("Pincode");
			cols.add("Req Type");
			layoutDetails.put("req_cols",cols);
			
			ArrayList<String> filterOptions = new ArrayList<String>();
			filterOptions.add("All");
			filterOptions.add("Host");
			filterOptions.add("User");
			layoutDetails.put("acc_filterBy",filterOptions);
			
			filterOptions=new ArrayList<String>();
			filterOptions.add("All");
			filterOptions.add("Host Requests");
			filterOptions.add("Theatre Requests");
			layoutDetails.put("req_filterBy",filterOptions);
			
			ArrayList<String> sortColumn = new ArrayList<String>();
			sortColumn.add("Name");
			sortColumn.add("Mail");
			sortColumn.add("City");
			sortColumn.add("State");
			layoutDetails.put("sortCol",sortColumn);
			
			PrintWriter out=response.getWriter();
			out.print(layoutDetails);		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void getAccountsDetails(HttpServletRequest request,HttpServletResponse response)throws Exception
	{
		try{
			JSONObject resultData=new JSONObject();
			JSONObject params=new JSONObject(request.getParameter("params"));
			
			String sortColumn=params.getString("sortCol"),
				   search_string=params.getString("search"),
				   filterBy=params.getString("filterBy"),
				   sortBy = params.getBoolean("sortBy")== true? "ASC" : "DESC" ;
				   
				   sortColumn= sortColumn.equals("Name") ? "fname" : sortColumn.toLowerCase();
				   
			resultData.put("accountDetails",ViewHandler.getAccounts(search_string,filterBy,sortColumn,sortBy));
			
			PrintWriter out=response.getWriter();
			out.print(resultData);			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void getReqDetails(HttpServletRequest request,HttpServletResponse response)throws Exception
	{
		try{
			JSONObject resultData=new JSONObject();
			JSONObject params=new JSONObject(request.getParameter("params"));
			JSONArray reqDetails=new JSONArray();
			
			String search_string=params.getString("search"),
				   filterBy=params.getString("filterBy");
				   
				if(filterBy.equals("All"))
				{
					ViewHandler.getAllHostRequests(reqDetails,search_string);
					ViewHandler.getTheatreReqDetails(reqDetails,search_string);
				}
				else if(filterBy.equals("Theatre Requests"))
				{
					ViewHandler.getTheatreReqDetails(reqDetails,search_string);
				}
				else
				{
					ViewHandler.getAllHostRequests(reqDetails,search_string);
				}
				   
			resultData.put("req_details",reqDetails);
			
			PrintWriter out=response.getWriter();
			out.print(resultData);			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	public void upgradeAccount(HttpServletRequest request,HttpServletResponse response)throws Exception
	{
		try{
			String requestId = request.getParameter("requestId");
			JSONObject resultData=new JSONObject();
			String status="error",message="An error occurred while attempting to change to a host account.!";
			
			Properties props=ViewHandler.getHostReqDetails(requestId);
			props.put("acc_type","Host");
			props.put("status","approved");
			
			if(!ViewHandler.checkDuplicateTheatre(getStringProperty(props,"TheatreName"),getStringProperty(props,"Pincode")))
			{
				UpdateHandler.deleteHostRequest(requestId);
				String user_id=UpdateHandler.addUser(props);
				props.put("userId",user_id);
				UpdateHandler.addNewTheatre(props);
				
				JSONObject userInfo=ViewHandler.getUserInfo(user_id);
		        String mail=userInfo.getString("mail");
				
				String content="<html><head><title>Congratulations! You're Now a EasyTickets Host!</title></head><body><div style='width: 600px; margin: 0 auto; font-family: sans-serif;'><header style='background-color: #d7f7d7; padding: 10px;'><h2>Fantastic News, "+userInfo.getString("fname")+" "+userInfo.getString("lname")+"!</h2><p>We're thrilled to inform you that your request to become a EasyTickets Host has been approved!</p></header><section style='padding: 20px;'><p>Get ready to unleash your movie magic! Your passion for cinema and dedication to the EasyTickets community have impressed us, and we're excited to welcome you as a Host.</p><p>As a Host, you have the power to:</p><ul><li>Create and manage your own virtual theaters.</li><li>Showcase your curated movie selections and showtimes.</li><li>Interact with moviegoers and build your own audience.</li><li>Contribute to the diverse and vibrant world of EasyTickets.</li></ul><p>To get started on your hosting journey, simply follow these steps:</p><ol><li>Log in to your EasyTickets account with your registered mail.</li><li>Click on the 'Profile > Buisness Details' section.</li><li>Start creating your first theater, adding movies, and setting showtimes!</li></ol><p>Thank You</p></section><div style='background-color: #d7f7d7; padding: 20px;'><p>Sincerely,</p><p>The EasyTickets Team</p></div></div></body></html>";
				
				if(mail!=null&& !mail.equals("")){
					Mail.sendMail(mail,content);
				}
				
				status="success";
				message="The account has been granted to host privileges.!";
			}
			
			resultData.put("status",status);
			resultData.put("message",message);
			
			PrintWriter out=response.getWriter();
			out.print(resultData);			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void toggleAccount(HttpServletRequest request,HttpServletResponse response)throws Exception
	{
		try{
			String user_id = request.getParameter("user_id");
			String acc_type=request.getParameter("acc_type");
			JSONObject resultData=UpdateHandler.toggleAccount(user_id,acc_type);
			if((resultData.getString("status")).equals("success"))
			{
				JSONObject userInfo=ViewHandler.getUserInfo(user_id);
		        String mail=userInfo.getString("mail");
				String content="";
				if(acc_type.equals("Host"))
				{
					content="<html><head><title>Congratulations! You're Now a EasyTickets Host!</title></head><body><div style='width: 600px; margin: 0 auto; font-family: sans-serif;'><header style='background-color: #d7f7d7; padding: 10px;'><h2>Congratulations, "+userInfo.getString("fname")+" "+userInfo.getString("lname")+"!</h2><p>Your EasyTickets account has been upgraded to Host privileges!</p></header><section style='padding: 20px;'><p>We're thrilled to invite you to enjoy the benefits of being a EasyTickets Host! This exciting change empowers you to:</p><ul><li>Register and manage your own theaters virtually.</li><li>Set ticket prices and manage bookings for your screenings.</li><li>Interact with moviegoers and build your audience.</li></ul><p>As a Host, you play a crucial role in enriching the EasyTickets experience for everyone. We believe your creativity and passion for movies will contribute significantly to our platform.</p><p>To explore your Host privileges and start setting up your virtual theater, simply:</p><ol><li>Log in to your EasyTickets account.</li><li>Click on the 'Profile > Business Details'.</li><li>Start creating your first theater, adding movies, and managing shows!</li></ol><p>If you have any questions or need assistance, simply reply to this email! We're here to help. We're happy to help you navigate your new hosting journey.</p><p>Welcome to the world of movie hosting!</p></section><div style='background-color: #d7f7d7; padding: 20px;'><p>Sincerely,</p><p>The EasyTickets Team</p></div></div></body></html>";
				}
				else
				{
					content="<html><head><title>EasyTickets Account Update</title></head><body><div style='width: 600px; margin: 0 auto; font-family: sans-serif;'><header style='background-color: #d7f7d7; padding: 20px;'><h2>Hi  "+userInfo.getString("fname")+" "+userInfo.getString("lname")+",</h2><p>This is an important update regarding your EasyTickets account.</p></header><section style='padding: 20px;'><p>As a result of "+request.getParameter("reason")+", your account privileges have been adjusted to User level.</p><p>This means that you will no longer have access to features associated with Hosting, such as creating and managing theaters, uploading movie content, and setting ticket prices.</p><p>We understand this change might affect your current plans, and we apologize for any inconvenience it may cause.</p><p>If you have any questions or need assistance, simply reply to this email! We're here to help.</p><p>Thank you for your understanding.</p></section><div style='background-color: #d7f7d7; padding: 20px;'><p>Sincerely,</p><p>The EasyTickets Team</p></div></div></body></html>";
				}
				
				if(mail!=null&& !mail.equals("")){
					Mail.sendMail(mail,content);
				}
			}
			PrintWriter out=response.getWriter();
			out.print(resultData);			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void denyHostRequest(HttpServletRequest request, HttpServletResponse response)throws Exception
	{
		try{
			String requestId = request.getParameter("requestId"),reason=request.getParameter("reason"),mail=request.getParameter("mail"),name=request.getParameter("name");
			JSONObject result=UpdateHandler.deleteHostRequest(requestId);
			if((result.getString("status")).equals("success"))
			{
				String content="<html><head><title>MovieTicketBooking Host Application Update</title></head><body><div style='width: 600px; margin: 0 auto; font-family: sans-serif;'><header style='background-color: #f7f7f7; padding: 20px;'><h2>Hi "+name+",</h2><p>This is an update regarding your recent application to become a EasyTickets Host.</p></header><section style='padding: 20px;'><p>We appreciate your interest in joining our community of Hosts and bringing your passion for movies to the platform.</p><p>After careful consideration of your application, we regret to inform you that it has not been approved at this time.</p><p>Unfortunately, we cannot share specific details regarding the reasons for individual application decisions. However, we encourage you to keep exploring your love for movies and consider reapplying in the future if you gain additional experience or address any areas we highlighted.</p><p>Rest assured, you can still enjoy EasyTickets as a user, browsing and booking tickets for exciting screenings. We constantly strive to improve our platform and welcome your feedback on ways to enhance the experience for everyone.</p><p>Thank you for your understanding.</p></section><div style='background-color: #f7f7f7; padding: 20px;'><p>Sincerely,</p><p>The EasyTickets Team</p></div></div></body></html>";
				
				if(mail!=null&& !mail.equals("")){
					Mail.sendMail(mail,content);
				}
				
			}
			PrintWriter out=response.getWriter();
			out.print(result);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void toggleTheatreRequest(HttpServletRequest request,HttpServletResponse response)throws Exception
	{
		try
		{
			String theatre_id=request.getParameter("theatre_id"),status=request.getParameter("status"),toggleStatus="error",name = request.getParameter("name"),mail=request.getParameter("mail");
			if((UpdateHandler.toggleTheatreStatus(theatre_id,status)).equals("success"))
			{
				String content="";
				if(status.equals("approved"))
				{
					content="<html><head><title>Congratulations! Your Movie Theater is Approved!</title><style>body{color:black}</style></head><body><div style='width: 600px; margin: 0 auto; font-family: sans-serif;'><header style='background-color: #d7f7d7; padding: 20px;'><h2>Fantastic news, "+name+"!</h2><p>We're thrilled to announce that your request to add a new theater to EasyTickets has been approved!</p></header><section style='padding: 20px;'><p>Your passion for movies and commitment to engaging our community have impressed us, and we can't wait to see your new theater come to life.</p><p>Here's what happens next:</p><ol><li>Head to the 'Profile > Buisness Details' section in your account and add screens under the Theatres.</li><li>Start adding movie listings and setting showtimes.</li><li>Get creative! Promote your theater, engage with your audience, and share your love of movies.</li></ol><p>We're excited to welcome your new theater to EasyTickets! Get ready to entertain and connect with moviegoers everywhere.</p></section><div style='background-color: #d7f7d7; padding: 20px;'><p>Sincerely,</p><p>The EasyTickets Team</p></div></div></body></html>";
				}
				else
				{
					content="<html><head><title>EasyTickets: Theater Addition Update</title><style>body{color:black}.im{color:black}</style></head><body><div style='width: 600px; margin: 0 auto; font-family: sans-serif;'><header style='background-color: #f7f7f7; padding: 20px;'><h2>Hi "+name+",</h2><p>This is an update regarding your recent request to add a new theater to EasyTickets.</p></header><section style='padding: 20px;'><p>We appreciate your interest in expanding the EasyTickets platform by adding the "+request.getParameter("theatreName")+". However, after careful consideration, we're unable to approve your request at this time.</p><p>"+request.getParameter("reason")+". We understand this might be disappointing, and we apologize for any inconvenience caused.</p><ul><li>Reply to this email with any additional information about your theater.</li></ul><p>We're always looking for ways to improve and expand EasyTickets, and we value your feedback. We encourage you to submit future theater addition requests that align with our current needs and guidelines.</p><p>Thank you for your understanding and continued support of EasyTickets.</p></section><div style='background-color: #f7f7f7; padding: 20px;'><p>Sincerely,</p><p>The EasyTickets Team</p></div></div></body></html>";
				}
				
				if(mail!=null&& !mail.equals("")){
					Mail.sendMail(mail,content);
				}
				
				toggleStatus="success";
			}
			JSONObject result=new JSONObject();
			result.put("status",toggleStatus);
			PrintWriter out=response.getWriter();
			out.print(result);			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}		
	}
	
	public void deleteAccount(HttpServletRequest request, HttpServletResponse response)throws Exception
	{
		try{
			String user_id = request.getParameter("user_id");
			String reason=request.getParameter("reason");
			JSONObject userInfo=ViewHandler.getUserInfo(user_id);
		    String mail=userInfo.getString("mail");
			JSONObject result=UpdateHandler.deleteAccount(user_id);
			if((result.getString("status")).equals("success"))
			{
				String content="<html><head><title>Account Removal Notification</title><style>body {font-family: Arial, sans-serif;margin: 0;padding: 20px;}.header {font-weight: bold;font-size: 18px;margin-bottom: 10px;}.body {line-height: 1.5;}.link {color: #007bff;}.footer{margin-top: 20px;color: #777;font-size:12px;}</style></head><body><div style='width: 600px; margin: 0 auto; font-family: sans-serif;'><header style='background-color: #f7f7f7; padding: 20px;'><h2>Hi "+userInfo.getString("fname")+" "+userInfo.getString("lname")+",</h2><p>Your Account on EasyTickets Has Been Removed by Admin.</p></header><div class='body'><br>This email is to inform you that your account on EasyTickets has been removed, effective immediately. We understand this may be inconvenient, and we want to provide you with details about why this action was taken:<br><br>"+reason+"<br><br>We appreciate your understanding.<br><br><div style='background-color: #f7f7f7; padding: 20px;'><p>Sincerely,</p><p>The EasyTickets Team</p></div><div class='footer'><p>This is an automated email. Please do not reply to this email.</p></div></div></body></html>";
				
				if(mail!=null&& !mail.equals("")){
					Mail.sendMail(mail,content);
				}
			}
				
			PrintWriter out=response.getWriter();
			out.print(result);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	public void getProfile(HttpServletRequest request, HttpServletResponse response)throws Exception
	{
		try{
			String user_id = request.getParameter("userId");
			JSONObject user_details=ViewHandler.getUserDetailsWithHostInfo(user_id);
			
			if((user_details.length())==0)
			{
				user_details.put("status","error");
				user_details.put("message","User Details not found!!..");
			}
			
			PrintWriter out=response.getWriter();
			out.print(user_details);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void getTheatres(HttpServletRequest request, HttpServletResponse response)throws Exception
	{
		try{
			String userId = request.getParameter("userId");
			JSONObject resultObj=ViewHandler.getTheatresDetails(userId);
			
			if((resultObj.length())==0)
			{
				resultObj.put("status","error");
				resultObj.put("message","Theatre Details not found!!..");
			}
			
			PrintWriter out=response.getWriter();
			out.print(resultObj);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void deleteTheatre(HttpServletRequest request, HttpServletResponse response)throws Exception
	{
		try{
			String theatre_id = request.getParameter("theatre_id");
			JSONObject resultObj=UpdateHandler.deleteTheatre(theatre_id);
			
			PrintWriter out=response.getWriter();
			out.print(resultObj);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void deleteScreen(HttpServletRequest request, HttpServletResponse response)throws Exception
	{
		try{
			String screen_id = request.getParameter("screen_id");
			JSONObject resultObj=UpdateHandler.deleteScreen(screen_id);
			
			PrintWriter out=response.getWriter();
			out.print(resultObj);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void theatreReq(HttpServletRequest request, HttpServletResponse response)throws Exception
	{
		try{
			JSONObject params=new JSONObject(request.getParameter("params"));
			Properties theatre=new Properties();
			theatre.put("userId",params.getString("userId"));
			theatre.put("TheatreName",params.getString("theatre_name"));
			theatre.put("located_at",params.getString("located_at"));
			theatre.put("Pincode",params.getString("tpincode"));
			theatre.put("City",params.getString("tcity"));
			theatre.put("State",params.getString("tstate"));
			theatre.put("status","request");
			JSONObject resultObj=UpdateHandler.addNewTheatre(theatre);
			
			if ("success".equals(resultObj.getString("status"))) {
					resultObj.put("message", "Request sent! We'll reach out after approval.");
			}
			
			PrintWriter out=response.getWriter();
			out.print(resultObj);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void addNewScreen(HttpServletRequest request, HttpServletResponse response)throws Exception
	{
		try{
			JSONObject params=new JSONObject(request.getParameter("params"));
			Properties screen_details=new Properties();
			screen_details.put("theatre_id",params.getString("theatre_id"));
			screen_details.put("user_id",params.getString("user_id"));
			screen_details.put("screen_name",params.getString("screen_name"));
			screen_details.put("silver_seats", jsonArrayToStringArray(params.getJSONArray("silver_seats")));
			screen_details.put("silver_dimension",params.getString("silver_dimension"));
			screen_details.put("gold_seats",jsonArrayToStringArray(params.getJSONArray("gold_seats")));
			screen_details.put("gold_dimension",params.getString("gold_dimension"));
			screen_details.put("platinum_seats",jsonArrayToStringArray(params.getJSONArray("platinum_seats")));
			screen_details.put("platinum_dimension",params.getString("platinum_dimension"));
			screen_details.put("diamond_seats",jsonArrayToStringArray(params.getJSONArray("diamond_seats")));
			screen_details.put("diamond_dimension",params.getString("diamond_dimension"));
			JSONObject resultObj=UpdateHandler.addScreen(screen_details);
			
			PrintWriter out=response.getWriter();
			out.print(resultObj);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void updateProfileDetails(HttpServletRequest request, HttpServletResponse response)throws Exception
	{
		try{
			JSONObject params=new JSONObject(request.getParameter("params"));
			Properties profile_details=new Properties();
			profile_details.put("fname",params.getString("fname"));
			profile_details.put("lname",params.getString("lname"));
			profile_details.put("mobile",params.getString("mobile"));
			profile_details.put("dob",params.getString("birthday"));
			profile_details.put("gender",params.getString("gender"));
			profile_details.put("pincode",params.getString("pincode"));
			profile_details.put("city",params.getString("city"));
			profile_details.put("state",params.getString("state"));
			profile_details.put("userId",params.getString("userId"));
			JSONObject resultObj=UpdateHandler.updateProfileDetails(profile_details);
			
			PrintWriter out=response.getWriter();
			out.print(resultObj);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	public void updateTheatreDetails(HttpServletRequest request, HttpServletResponse response)throws Exception
	{
		try{
			JSONObject params=new JSONObject(request.getParameter("params"));
			Properties screen_details=new Properties();
			screen_details.put("screen_id",params.getString("screen_id"));
			screen_details.put("user_id",params.getString("user_id"));
			screen_details.put("screen_name",params.getString("screen_name"));
			screen_details.put("silver_seats", jsonArrayToStringArray(params.getJSONArray("silver_seats")));
			screen_details.put("silver_dimension",params.getString("silver_dimension"));
			screen_details.put("gold_seats",jsonArrayToStringArray(params.getJSONArray("gold_seats")));
			screen_details.put("gold_dimension",params.getString("gold_dimension"));
			screen_details.put("platinum_seats",jsonArrayToStringArray(params.getJSONArray("platinum_seats")));
			screen_details.put("platinum_dimension",params.getString("platinum_dimension"));
			screen_details.put("diamond_seats",jsonArrayToStringArray(params.getJSONArray("diamond_seats")));
			screen_details.put("diamond_dimension",params.getString("diamond_dimension"));
			params=UpdateHandler.updateTheatreDetails(screen_details);
			
			PrintWriter out=response.getWriter();
			out.print(params);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void checkShowClash(HttpServletRequest request,HttpServletResponse response)throws Exception
	{
		try{
			JSONObject params=new JSONObject(request.getParameter("params"));
			params=ViewHandler.CheckShowClash(params);
			
			PrintWriter out=response.getWriter();
			out.print(params);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void addShows(HttpServletRequest request,HttpServletResponse response)throws Exception
	{
		try{
			JSONObject resultObj=new JSONObject();
			String status,message;
			JSONObject params=new JSONObject(request.getParameter("params"));
			
			JSONObject movie_details=params.getJSONObject("movie_details");
			
			movie_details=UpdateHandler.insertMovieDetails(movie_details);
			status=movie_details.getString("status");
			message=movie_details.getString("message");
			
			if((movie_details.getString("status")).equals("success"))
			{
				JSONObject show_status=UpdateHandler.addNewShows(params.getJSONArray("shows"));
				
				if((show_status.getString("status")).equals("success"))
				{
					if(!(movie_details.getBoolean("genreInsertionStatus") && !movie_details.getBoolean("castInsertionStatus")))
					{
						status="info";
						message="Show added successfully, but there was an error updating the cast and genre details.";
					}
					else if(!movie_details.getBoolean("genreInsertionStatus"))
					{
						status="info";
						message="Show added successfully, but there was an error updating the genre details.";
					}
					else if(!movie_details.getBoolean("castInsertionStatus"))
					{
						status="info";
						message="Show added successfully, but there was an error updating the cast details.";
					}
				}
			}
			
			resultObj.put("status",status);
			resultObj.put("message",message);
			PrintWriter out=response.getWriter();
			out.print(resultObj);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void getShows(HttpServletRequest request,HttpServletResponse response)throws Exception
	{
		try
		{
			JSONObject params = new JSONObject(request.getParameter("params"));
			String searchValue = params.getString("searchText");
			String location   = params.getString("location");
			JSONArray filters   = params.getJSONArray("filters");
			getFilters(params,filters);
			
			JSONObject resultObj=new JSONObject();
			resultObj.put("shows",ViewHandler.getShows(searchValue,location,params.getString("genres"),params.getString("languages")));
			resultObj.put("genres",getGenres());
			resultObj.put("languages",getAllLanguages());
			PrintWriter out=response.getWriter();
			out.print(resultObj);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void getFilters(JSONObject jsonObj,JSONArray filters)
	{
		try
		{
			StringJoiner genres = new StringJoiner(",");
			StringJoiner languages = new StringJoiner(",");
			for(int i=0;i<filters.length();i++)
			{
				JSONObject filter=filters.getJSONObject(i);
				if((filter.getString("filterType")).equals("genre"))
				{
					genres.add("'" +filter.getString("filterValue") +"'");
				}
				else
				{
					languages.add("'" +filter.getString("filterValue") +"'");
				}
			}
			jsonObj.put("genres",genres.toString());
			jsonObj.put("languages",languages.toString());
				
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void getMovieDetails(HttpServletRequest request,HttpServletResponse response)throws Exception
	{
		try
		{
			JSONObject params = new JSONObject(request.getParameter("params"));
			String show_id = params.getString("show_id");
			Date show_date=parseToDate(null,params.getString("show_date"),"yyyy-MM-dd");
			JSONObject resultObj=new JSONObject();
			ArrayList<Date> show_dates=ViewHandler.getShowDates(show_id);
			
			if(show_date==null)
			{
				// Properties date=(Properties)show_dates.get(0);
				show_date=(Date)show_dates.get(0);
			}
			
			resultObj.put("show_dates",show_dates);
			resultObj.put("movie_details",ViewHandler.getMovieDetails(show_id));
			resultObj.put("show_details",ViewHandler.getAllshows(show_id,show_date,params.getJSONArray("showTimeFilter")));
			
			PrintWriter out=response.getWriter();
			out.print(resultObj);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void getShowDetails(HttpServletRequest request,HttpServletResponse response)throws Exception
	{
		try
		{
			String history_id=request.getParameter("history_id");
			JSONObject result = ViewHandler.getShowDetails(history_id);
			
			PrintWriter out=response.getWriter();
			out.print(result);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}		
	}
	
	public static void getScreenDetails(HttpServletRequest request,HttpServletResponse response)throws Exception
	{
		try
		{
			JSONObject params=new JSONObject(request.getParameter("params"));
			String user_id=params.getString("user_id"),screen_id=params.getString("screen_id");
			
			JSONObject result=ViewHandler.getScreenDetails(screen_id,user_id);
			
			PrintWriter out=response.getWriter();
			out.print(result);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public static void getTheatreDetails(HttpServletRequest request,HttpServletResponse response) throws Exception
	{
		try{
			JSONObject result=new JSONObject(request.getParameter("params"));
			result=ViewHandler.getTheatreDetails(result);
			PrintWriter out=response.getWriter();
			out.print(result);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void blockTickets(HttpServletRequest request,HttpServletResponse response)throws Exception
	{
		try
		{
			JSONObject jsonObj=new JSONObject(request.getParameter("params"));
			int show_id=Integer.parseInt(jsonObj.getString("show_id"));
			String diamond[]=jsonArrayToStringArray(jsonObj.getJSONArray("diamond")),
				   platinum[]=jsonArrayToStringArray(jsonObj.getJSONArray("platinum")),
				   gold[]=jsonArrayToStringArray(jsonObj.getJSONArray("gold")),
				   silver[]=jsonArrayToStringArray(jsonObj.getJSONArray("silver"));
				   
			jsonObj=UpdateHandler.blockTickets(show_id,diamond,platinum,gold,silver);
			PrintWriter out=response.getWriter();
			out.print(jsonObj);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}		
	}
	
	public static void getTicketDetails(HttpServletRequest request,HttpServletResponse response)throws Exception
	{
		try
		{
			JSONObject jsonObj=new JSONObject(request.getParameter("params"));
			String block_id=jsonObj.getString("block_id"),
				   history_id=jsonObj.getString("history_id"),
				   status="error",message="Error Occurred while retrieve records!!." ;
				   
			jsonObj=ViewHandler.getBlockedTickets(block_id);
			if((jsonObj.get("status")).equals("success"))
			{
				JSONObject show_details=ViewHandler.getShowDetails(history_id);
				jsonObj.put("screen_id",show_details.get("screen_id"));
				jsonObj.put("screen_name",show_details.get("screen_name"));
				jsonObj.put("language",show_details.get("language"));
				jsonObj.put("projection",show_details.get("projection"));
				jsonObj.put("movie_name",show_details.get("movie_name"));
				jsonObj.put("movie_certificate",show_details.get("movie_certificate"));
				jsonObj.put("theatre_name",show_details.get("theatre_name"));
				jsonObj.put("located_at",show_details.get("located_at"));
				jsonObj.put("show_startDate",show_details.get("show_startDate"));
				jsonObj.put("diamond_price",show_details.get("diamond_price"));
				jsonObj.put("platinum_price",show_details.get("platinum_price"));
				jsonObj.put("gold_price",show_details.get("gold_price"));
				jsonObj.put("silver_price",show_details.get("silver_price"));
				jsonObj.put("block_id",block_id);
				jsonObj.put("history_id",history_id);
				status="success";
				message="Records retrieved successfully!!..";
			}
			
			jsonObj.put("status",status);
			jsonObj.put("message",message);
			
			PrintWriter out=response.getWriter();
			out.print(jsonObj);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}		
	}
	
	public static void confirmTickets(HttpServletRequest request,HttpServletResponse response)throws Exception
	{
		try
		{
			JSONObject params=new JSONObject(request.getParameter("params"));
			String status="error",message="Error Occurred while Book Tickets!!Please try again after some time!..";
			String history_id=params.getString("history_id"),
				   block_id=params.getString("block_id"),
				   movie_time=params.getString("movie_time"),
				   user_id=params.getString("user_id"),
				   seats=params.getString("seats"),
				   mail=ViewHandler.getUserMail(user_id);
		    double total_price=params.getDouble("total_price");
			JSONObject show_details=ViewHandler.getShowDetails(history_id);
			String poster_path=show_details.getString("poster_path"),
					   movie_name=show_details.getString("movie_name"),
					   theatre_name=show_details.getString("theatre_name"),
					   located_at=show_details.getString("located_at"),
					   screen_name=show_details.getString("screen_name"),
					   language=show_details.getString("language"),
					   certificate=show_details.getString("movie_certificate"),
					   projection=show_details.getString("projection");
			Date d1 = parseToDate(null,show_details.getString("show_startDate"),"yyyy-MM-dd HH:mm");
			String date[]=(parseToString(d1,"MMMM,EEEE,hh:mma,DD,YYYY")).split(",");
				
			String booking_id=generateBookingId(user_id,show_details.getString("movie_name"),block_id);
			
			BufferedImage qrCodeImage = generateQRCode(booking_id+","+movie_name+","+theatre_name+","+located_at+","+screen_name+","+certificate+","+total_price+","+seats);
			String encodedQr=null;
			if (qrCodeImage != null) {
				encodedQr = convertImageToByteArray(qrCodeImage);
		   
			}
		
			String confirmStatus=UpdateHandler.bookTickets(booking_id,user_id,block_id,history_id,total_price,encodedQr);
				   
			if(confirmStatus.equals("success"))
			{
				String content="<html><head><style>.ticket-info{text-align:center;align-items:center;}.date{font-size:8px;border-top:1px solid gray;border-bottom:1px solid gray;padding:5px 0;font-weight:700;display:flex;align-items:center;margin:0px;}.date span{width:100px;}.date span:first-child{text-align:left;}.date span:last-child{text-align:right;}.date .timing{color:#d83565;font-size:8px;padding:0px 99px;}.details td{font-size:15px;padding:5px;}.qr{height: -webkit-fill-available;}h1{margin:0px;font-size:25px;}</style></head><body><table width='100px' border='0' cellspacing='0' cellpadding='0' height='100px'><tbody><tr><td width='100%' height='100%' valign='top' class='bgresize'><div><table width='50%' height='50%' border='0' cellspacing='0' cellpadding='0'><tbody><tr><td valign='top' class='mobile-padding'><div style='width:200px;height:300px;background-image:url(https://image.tmdb.org/t/p/original/"+poster_path+");background-size:cover;background-position:center;'><table width='100%' border='0' cellspacing='0' cellpadding='0'><tbody><tr><td align='left' valign='top' class='padding65'><p style='margin:1px;writing-mode: vertical-rl;font-size: 15px;color: gray;  padding: 5px 0px; transform: rotate(-180deg);'><span class='banner-heading55' style='padding-left:0px;padding-bottom:10px;padding-top:10px;'>EasyTickets</span><span class='banner-heading55' style='padding-left:0px;padding-bottom:10px;padding-top:10px;'>EasyTickets</span><span class='banner-heading55' style='padding-left:0px;padding-bottom:10px;padding-top:10px;'>EasyTickets</span></p></td></tr></tbody></table></div></td></tr></tbody></table></div></td><td width='100%' height='100%' valign='top' class='bgresize'><div><table width='50%' height='50%' border='0' cellspacing='0' cellpadding='0'><tbody><tr><td valign='top' class='mobile-padding'><div style='width:500px;height:260px;background-color:#acbdf55c;padding:20px;'><div class='ticket-info'><p class='date'><span>"+formatDate(String.valueOf(d1.getDate()))+" "+date[0]+"</span><span class='timing'>"+date[2]+", "+date[1]+"</span><span>"+date[4]+"</span></p></div><div><table><tbody><tr><td style='width:80%'><table class='details'><tbody><tr><td colspan='2'><h1>"+movie_name+"("+certificate+")</h1></td></tr><tr><td>BOOKING ID</td><td>"+booking_id+"</td></tr><tr><td>Details </td><td>"+screen_name+", "+language+" - "+projection+"</td></tr><tr><td>Theatre </td><td>"+theatre_name+", "+located_at+"</td></tr><tr><td>Tickets("+(seats.split(",")).length+")</td><td>"+seats+"</td></tr><tr><td>Price</td><td style='vertical-align: top;'> "+formatCurrency(total_price)+"</td></tr></tbody></table></td><td><div class='qr'><img src='data:image/png;base64," + encodedQr + "'  style='width:100px;height:100px;' alt='QR Code'></div></td></tr></tbody></table></div></div></td></tr></tbody";
				
				if(mail!=null&& !mail.equals("")){
				 Mail.sendMail(mail,content);
				}
				status="success";
				message="Tikcets Booked successfully!!!..";
			}
			else if(confirmStatus.equals("timeOut"))
			{
				message="Unfortunately, your booking timed out due to inactivity. Please try again later!!";
			}
			
			params=new JSONObject();
			params.put("status",status);
			params.put("message",message);

			PrintWriter out=response.getWriter();
			out.print(params);
		}catch(Exception e)
		{
			e.printStackTrace();
		}		
	}
	
	
	public static void getBookingHistories(HttpServletRequest request,HttpServletResponse response)throws Exception
	{
		try
		{
			JSONObject params=new JSONObject(request.getParameter("params"));
			String user_id=params.getString("user_id"),date1=params.getString("from"),date2=params.getString("to");
			params=new JSONObject();
			params.put("histories",ViewHandler.getBookingHistories(user_id,date1,date2));
			
			PrintWriter out=response.getWriter();
			out.print(params);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	public static void getShowHistories(HttpServletRequest request,HttpServletResponse response)
	{
		try
		{
			JSONObject params=new JSONObject(request.getParameter("params"));
			String user_id=params.getString("user_id");
			
			params=new JSONObject();
			params.put("histories",ViewHandler.getShowHistories(user_id));
			PrintWriter out=response.getWriter();
			out.print(params);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void toggleShowStatus(HttpServletRequest request,HttpServletResponse response)
	{
		try
		{
			JSONObject params=new JSONObject(request.getParameter("params"));
			
			params=UpdateHandler.toggleShowStatus(params.getString("history_id"),params.getString("status"));
			
			PrintWriter out=response.getWriter();
			out.print(params);			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}		
	}
	
	public static void deleteShowHistory(HttpServletRequest request,HttpServletResponse response)
	{
		try
		{
			JSONObject params=new JSONObject(request.getParameter("params"));
			
			params=UpdateHandler.deleteShowHistory(params.getString("history_id"));
			
			PrintWriter out=response.getWriter();
			out.print(params);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}		
	}

	public static String convertImageToByteArray(BufferedImage image) 
	{
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", baos);
            byte[] byteArray = baos.toByteArray();
            // // return baos.toByteArray();
			 return Base64.getEncoder().encodeToString(byteArray);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
	
	public static String generateBookingId(String user_id,String movie_name,String block_id)
	{
		String movieCode=getAsciiSubstring(movie_name.toUpperCase(),3);
		return movieCode + user_id.substring(user_id.length()-3) + block_id;
	}
}

