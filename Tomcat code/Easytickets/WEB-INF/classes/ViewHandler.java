
package easytickets;

import utils.Database;
import utils.Util;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.time.LocalTime;
import java.time.ZoneId;

public class ViewHandler extends Util
{	
	
	public  static JSONObject verifyOTP(String credentail_type,String credential,String oneTimePassword)throws Exception
	{
		JSONObject resultObj=new JSONObject();
		String status="error";
		String message="Invalid OTP!!...";
		try
		{
			String tableName="otp";
			Connection con=Database.getConnection();
			PreparedStatement st=con.prepareStatement("select * from "+tableName+" where "+credentail_type+"='"+credential+"' AND oneTimePassword='"+oneTimePassword+"';");
			ResultSet rs=st.executeQuery();
			resultObj.put("credential",credential);
			resultObj.put("oneTimePassword",oneTimePassword);
			if(rs.next())
			{
				ZoneId istZone = ZoneId.of("Asia/Kolkata");
				LocalTime currentTime = LocalTime.now(istZone);
				Time time = Time.valueOf(currentTime);
				Time time2=rs.getTime(3);
				int comparison = time.compareTo(time2);
				if(comparison>0)
				{
					message="OTP Expired!!!..Resend OTP";
				}
				else
				{
					status="success";
					message="Otp verified Successfully!!";
				}
			}
			rs.close();
			st.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		resultObj.put("status",status);
		resultObj.put("message",message);
		return resultObj;
	}
	
	public static JSONObject getUserInfo(String userId)throws Exception
	{
		JSONObject userInfo=new JSONObject();
		String status="error",message="User Information unavailable!!..";
		try{
			Connection con=Database.getConnection();
			PreparedStatement stmt=con.prepareStatement("select * from accounts where userId='"+userId+"';");
			ResultSet rs=stmt.executeQuery();
			
			if(rs.next())
			{
				userInfo.put("mail",rs.getString("mail"));
				userInfo.put("profile_pic",Base64.getEncoder().encodeToString(rs.getBytes("profile_pic")));
				userInfo.put("fname",rs.getString("fname"));
				userInfo.put("lname",rs.getString("lname"));
				userInfo.put("dob",rs.getString("dob"));
				userInfo.put("gender",rs.getString("mobile"));
				userInfo.put("pincode",rs.getString("pincode"));
				userInfo.put("city",rs.getString("city"));
				userInfo.put("state",rs.getString("state"));
				userInfo.put("acc_type",rs.getString("acc_type"));
				status="success";
				message="User Information fetched successfully!!..";
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		userInfo.put("status",status);
		userInfo.put("message",message);
		return userInfo;
	}
	
	public static JSONObject getUserDetailsWithHostInfo(String userId)
	{
		JSONObject user_details=new JSONObject();
		try{
			Connection con=Database.getConnection();
			PreparedStatement stmt=con.prepareStatement("SELECT  accounts.fname,accounts.lname,accounts.userid,accounts.acc_type ,accounts.mail,accounts.pincode,accounts.mobile,accounts.dob,accounts.gender,theatres.theatre_id,theatres.located_at,theatres.tpincode,theatres.tcity,theatres.tstate,theatres.theatre_name,theatres.status,theatre_details.screen_id,theatre_details.screen_name,theatre_details.silver_seats,theatre_details.gold_seats,theatre_details.platinum_seats,theatre_details.diamond_seats FROM accounts LEFT JOIN theatres ON accounts.userid = theatres.userid LEFT JOIN theatre_details ON theatres.theatre_id = theatre_details.theatre_id where accounts.userid='"+userId+"';");
			ResultSet rs=stmt.executeQuery();
			
			Properties theatres=new Properties();
			Properties theatre;
			Properties screens;
			Properties screen;
			while(rs.next())
			{
				String status=rs.getString("status"),
					   theatre_id=rs.getString("theatre_id");
				if(theatre_id!=null && rs.getString("status").equals("approved"))
				{
					if(theatres.containsKey(theatre_id))
					{
						theatre = (Properties)theatres.get(theatre_id);
						screens =(Properties) theatre.getOrDefault("screens",new Properties());
						
						screen=new Properties();
						screen.put("screen_id",rs.getString("screen_id"));
						screen.put("screen_name",rs.getString("screen_name"));
						screen.put("platinum_seats",arrayToJsonarray(rs.getArray("platinum_seats")));
						screen.put("diamond_seats",arrayToJsonarray(rs.getArray("diamond_seats")));
						screen.put("gold_seats",arrayToJsonarray(rs.getArray("gold_seats")));
						screen.put("silver_seats",arrayToJsonarray(rs.getArray("silver_seats")));
						screens.put(rs.getString("screen_id"),screen);
						
						theatre.put("screens",screens);
						theatres.put(theatre_id,theatre);
					}
					else
					{
						theatre=new Properties();
						theatre.put("theatre_id",theatre_id);
						theatre.put("theatre_name",rs.getString("theatre_name"));
						theatre.put("located_at",rs.getString("located_at"));
						theatre.put("tCity",rs.getString("tcity"));
						theatre.put("tState",rs.getString("tstate"));
						theatre.put("tPincode",rs.getString("tpincode"));
						
						if(rs.getString("screen_id")!=null)
						{
							screens=new Properties();
							screen=new Properties();
							screen.put("screen_id",rs.getString("screen_id"));
							screen.put("screen_name",rs.getString("screen_name"));
							screen.put("platinum_seats",arrayToJsonarray(rs.getArray("platinum_seats")));
							screen.put("diamond_seats",arrayToJsonarray(rs.getArray("diamond_seats")));
							screen.put("gold_seats",arrayToJsonarray(rs.getArray("gold_seats")));
							screen.put("silver_seats",arrayToJsonarray(rs.getArray("silver_seats")));
							screens.put(rs.getString("screen_id"),screen);
							theatre.put("screens",screens);
						}
						
						theatres.put(theatre_id,theatre);
					}
				}
				if(user_details.length()==0)
				{
					user_details.put("userId",rs.getString("userId"));
					user_details.put("fname",rs.getString("fname"));
					user_details.put("lname",rs.getString("lname"));
					user_details.put("dob",rs.getString("dob"));
					user_details.put("gender",rs.getString("gender"));
					user_details.put("mail",rs.getString("mail"));
					user_details.put("mobile",rs.getString("mobile")); 
					user_details.put("pincode",rs.getString("pincode"));
					user_details.put("acc_type",rs.getString("acc_type"));
				}
			}
			user_details.put("theatres",theatres);
			
			rs.close();
			stmt.close();			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return user_details;
	}
	
	public static String getUserMail(String user_id)throws Exception
	{
		String user_mail="";
		try
		{
			Connection con=Database.getConnection();
			PreparedStatement pstmt=con.prepareStatement("select mail from accounts where userId='"+user_id+"';");
			ResultSet rs=pstmt.executeQuery();
			
			if(rs.next())
			{
				user_mail=rs.getString("mail");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return user_mail;
	}
	
	public static JSONObject getTheatresDetails(String userId)
	{
		JSONObject resultObj=new JSONObject();
		try{
			Connection con=Database.getConnection();
			PreparedStatement stmt=con.prepareStatement("SELECT theatres.theatre_id,theatres.tcity,theatres.theatre_name,theatres.status,theatre_details.screen_id,theatre_details.screen_name FROM theatres RIGHT JOIN theatre_details ON theatres.theatre_id = theatre_details.theatre_id where theatres.userid='"+userId+"' AND theatres.status='approved';");
			ResultSet rs=stmt.executeQuery();
			
			Properties theatres=new Properties();
			Properties theatre;
			Properties screen_details=new Properties();
			JSONArray screens=new JSONArray();
			Properties screen;
			while(rs.next())
			{
				String theatre_id=rs.getString("theatre_id");
				if(theatre_id!=null)
				{
					if(!theatres.containsKey(theatre_id))
					{
						theatre=new Properties();
						theatre.put("theatre_id",theatre_id);
						theatre.put("theatre_name",rs.getString("theatre_name"));
						theatre.put("tCity",rs.getString("tcity"));
						theatres.put(theatre_id,theatre);
					}
					if(rs.getString("screen_id")!=null)
					{
						if(screen_details.containsKey(theatre_id))
						{
							screens =(JSONArray) screen_details.get(theatre_id);
							screen=new Properties();
							screen.put("screen_id",rs.getString("screen_id"));
							screen.put("screen_name",rs.getString("screen_name"));
							screens.put(screen);
							
							screen_details.put(theatre_id,screens);
						}
						else
						{
							screens=new JSONArray();
							screen=new Properties();
							screen.put("screen_id",rs.getString("screen_id"));
							screen.put("screen_name",rs.getString("screen_name"));
							screens.put(screen);
							
							screen_details.put(theatre_id,screens);
						}
					}
				}
			}
			
			resultObj.put("theatres",theatres);
			resultObj.put("screens",screen_details);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return resultObj;
	}
	
	public static JSONArray getAccounts(String search_string,String filterBy,String sortColumn,String sortBy)
	{
		JSONArray hostDetails=new JSONArray();
		try{
			JSONObject account;
			String query = "select * from accounts where ";
			Connection con=Database.getConnection();
			
			if(!search_string.equals("") && search_string != null)
			{
				query +="((fname IS NOT NULL AND fname ILIKE '%"+search_string+"%') OR (lname IS NOT NULL AND lname ILIKE '%"+search_string+"%') OR (mail IS NOT NULL AND mail ILIKE '%"+search_string+"%') OR (mobile IS NOT NULL AND mobile ILIKE '%"+search_string+"%') OR (gender IS NOT NULL AND gender ILIKE '%"+search_string+"%') OR (dob IS NOT NULL AND dob ILIKE '%"+search_string+"%') OR (pincode IS NOT NULL AND pincode ILIKE '%"+search_string+"%') OR (city IS NOT NULL AND city ILIKE '%"+search_string+"%') OR (state IS NOT NULL AND state ILIKE '%"+search_string+"%')) AND";
			}
			
			if(filterBy.equals("All"))
			{
				query+=" acc_type !='Admin' ";
			}
			else
			{
				query+=" acc_type='"+filterBy+"' ";
			}
			
			query+=" ORDER BY "+sortColumn+" "+sortBy+";";
			
			PreparedStatement st=con.prepareStatement(query);
			ResultSet rs=st.executeQuery();
			while(rs.next())
			{
				account=new JSONObject();
				String lname=rs.getString("lname");
				account.put("userId",rs.getString("userId"));
				account.put("Name",rs.getString("fname")+""+(lname == null ? "": " "+lname));
				account.put("Mobile",rs.getString("mobile"));
				account.put("Mail",rs.getString("mail"));
				account.put("City",rs.getString("city"));
				account.put("State",rs.getString("state"));
				account.put("Pincode",rs.getString("pincode"));
				account.put("Acc Type",rs.getString("acc_type"));
				hostDetails.put(account);
			}
			rs.close();
			st.close();
		}catch(Exception e){
			e.printStackTrace();
		}	
		return hostDetails;
	}
	
	public static void getAllHostRequests(JSONArray hostRequests,String search_string)
	{
		try{
			JSONObject account;
			
			String query="select * from host_requests ";
			
			if(!search_string.equals("") && search_string != null)
			{
				query +="where( (fname IS NOT NULL AND fname ILIKE '%"+search_string+"%') OR (mail IS NOT NULL AND mail ILIKE '%"+search_string+"%') OR (mobile IS NOT NULL AND mobile ILIKE '%"+search_string+"%') OR (tpincode IS NOT NULL AND tpincode ILIKE '%"+search_string+"%') OR (tcity IS NOT NULL AND tcity ILIKE '%"+search_string+"%') OR (tstate IS NOT NULL AND tstate ILIKE '%"+search_string+"%') )";
			}
			
			query+=" ORDER BY fname ASC;";
			
			Connection con=Database.getConnection();
			PreparedStatement st=con.prepareStatement(query);
			ResultSet rs=st.executeQuery();
			while(rs.next())
			{
				account=new JSONObject();
				account.put("RequestId",rs.getString("request_id"));
				account.put("Name",rs.getString("fname"));
				account.put("Mobile",rs.getString("mobile"));
				account.put("Mail",rs.getString("mail"));
				account.put("TheatreName",rs.getString("theatre_name"));
				account.put("City",rs.getString("tcity"));
				account.put("State",rs.getString("tstate"));
				account.put("Pincode",rs.getString("tpincode"));
				account.put("Req Type","Host");
				hostRequests.put(account);
			}
			rs.close();
			st.close();
		}catch(Exception e){
			e.printStackTrace();
		}	
	}
	
	public static void getTheatreReqDetails(JSONArray req_details,String search_string)
	{
		try{
			JSONObject account;
			String query="select accounts.fname,accounts.lname,accounts.mail,accounts.mobile,theatres.theatre_id,theatres.theatre_name ,theatres.tpincode ,theatres.tcity ,theatres.tstate from accounts left join theatres on accounts.userid =theatres.userid where ";
			
			if(!search_string.equals("") && search_string != null)
			{
				query +="( (fname IS NOT NULL AND fname ILIKE '%"+search_string+"%') OR (lname IS NOT NULL AND lname ILIKE '%"+search_string+"%') OR (mail IS NOT NULL AND mail ILIKE '%"+search_string+"%') OR (mobile IS NOT NULL AND mobile ILIKE '%"+search_string+"%') OR (tpincode IS NOT NULL AND tpincode ILIKE '%"+search_string+"%') OR (tcity IS NOT NULL AND tcity ILIKE '%"+search_string+"%') OR (tstate IS NOT NULL AND tstate ILIKE '%"+search_string+"%') ) AND ";
			}
			
			query+="theatres.status='request' ORDER BY fname ASC;";
			
			Connection con=Database.getConnection();
			PreparedStatement st=con.prepareStatement(query);
			ResultSet rs=st.executeQuery();
			while(rs.next())
			{
				account=new JSONObject();
				account.put("theatre_id",rs.getString("theatre_id"));
				String lname=rs.getString("lname");
				account.put("Name",rs.getString("fname")+""+(lname == null ? "": " "+lname));
				account.put("Mobile",rs.getString("mobile"));
				account.put("Mail",rs.getString("mail"));
				account.put("TheatreName",rs.getString("theatre_name"));
				account.put("City",rs.getString("tcity"));
				account.put("State",rs.getString("tstate"));
				account.put("Pincode",rs.getString("tpincode"));
				account.put("Req Type","Theatre");
				req_details.put(account);
			}
			rs.close();
			st.close();
		}catch(Exception e){
			e.printStackTrace();
		}	
	}
	
	public static JSONObject getTheatreDetails(JSONObject details)throws Exception
	{
		JSONObject result=new JSONObject();
		String status="error",message="Error Occurred while fetching Theatre Details!..";
		try
		{
			Connection con=Database.getConnection();
			PreparedStatement pstmt=con.prepareStatement("select theatres.theatre_name,theatres.theatre_id,theatres.located_at from theatres where theatres.theatre_id='"+details.getString("theatre_id")+"' and theatres.userId='"+details.getString("user_id")+"';");
			ResultSet rs=pstmt.executeQuery();
			
			if(rs.next())
			{
				result.put("theatre_name",rs.getString("theatre_name"));
				result.put("theatre_id",rs.getString("theatre_id"));
				result.put("located_at",rs.getString("located_at"));
				status="success";
				message="Theatre Details Obtainer Successfully..";
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		result.put("status",status);
		result.put("message",message);
		
		return result;
	}
	
	public static Properties getHostReqDetails(String requestId)
	{
		Properties req_details=new Properties();
		try{
			Connection con=Database.getConnection();
			PreparedStatement stmt=con.prepareStatement("select * from host_requests where request_id='"+requestId+"';");
			ResultSet rs=stmt.executeQuery();

			if(rs.next()) {
				req_details.put("Name", rs.getString("fname"));
				req_details.put("Mobile", rs.getString("mobile"));
				req_details.put("Mail", rs.getString("mail"));
				req_details.put("located_at", rs.getString("located_at"));
				req_details.put("TheatreName", rs.getString("theatre_name"));
				req_details.put("City", rs.getString("tcity"));
				req_details.put("State", rs.getString("tstate"));
				req_details.put("Pincode", rs.getString("tpincode"));
			}
			
			rs.close();
			stmt.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return req_details;
	}
	
	public static boolean checkAccountExist(String userId)
	{
		boolean isExist=false;
		try
		{
			Connection con=Database.getConnection();
			PreparedStatement st=con.prepareStatement("select userid from accounts where userid='"+userId+"';");
			ResultSet rs=st.executeQuery();
			if(rs.next())
			{
				isExist=true;
			}
			rs.close();
			st.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return isExist;
	}
	
	public static boolean checkDuplicateTheatre(String theatre_name,String pincode)
	{
		boolean isDuplicate=false;
		try
		{
			Connection con=Database.getConnection();
			PreparedStatement st=con.prepareStatement("select theatre_id from theatres where theatre_name ='"+theatre_name+"' and tpincode='"+pincode+"' and status!='denied';");
			ResultSet rs=st.executeQuery();
			if(rs.next())
			{
				isDuplicate=true;
			}
			rs.close();
			st.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return isDuplicate;
	}
	
	public static boolean checkDuplicateScreen(String user_id,String screen_name)
	{
		boolean isDuplicate=false;
		try
		{
			Connection con=Database.getConnection();
			PreparedStatement st=con.prepareStatement("select theatre_details.screen_id from theatre_details join theatres on theatre_details.theatre_id =theatres.theatre_id where theatre_details.screen_name='"+screen_name+"' and theatres.userid='"+user_id+"';");
			ResultSet rs=st.executeQuery();
			if(rs.next())
			{
				isDuplicate=true;
			}
			rs.close();
			st.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return isDuplicate;
	}
	
	public static boolean checkDuplicateHostAccount(String mail)
	{
		boolean isDuplicate=false;
		try
		{
			Connection con=Database.getConnection();
			PreparedStatement st=con.prepareStatement("select fname from accounts where mail='"+mail+"' and acc_type='Host';");
			ResultSet rs=st.executeQuery();
			if(rs.next())
			{
				isDuplicate=true;
			}
			rs.close();
			st.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return isDuplicate;
	}
	
	
	public static boolean checkDuplicateReq(String mail)
	{
		boolean isDuplicate=false;
		try
		{
			Connection con=Database.getConnection();
			PreparedStatement st=con.prepareStatement("select fname from host_requests where mail='"+mail+"';");
			ResultSet rs=st.executeQuery();
			if(rs.next())
			{
				isDuplicate=true;
			}
			rs.close();
			st.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return isDuplicate;
	}
	
	public static JSONObject getShowDetails(String history_id)throws Exception
	{
		JSONObject show_details=new JSONObject();
		String status="error",message="Error occurred while obtaining show details!..";
		
		try{
			Connection conn=Database.getConnection();
			PreparedStatement pstmt=conn.prepareStatement("select movies.poster_path, movies.movie_name,movies.certificate,theatres.theatre_name,theatres.located_at,show_history.show_startdate,theatre_details.screen_name,theatre_details.screen_id,theatre_details.platinum_seats,theatre_details.platinum_dimension,theatre_details.diamond_seats,theatre_details.diamond_dimension,theatre_details.gold_seats,shows.show_id,theatre_details.gold_dimension,theatre_details.silver_seats,theatre_details.silver_dimension,show_history.silver_tprice,show_history.gold_tprice,show_history.diamond_tprice,show_history.platinum_tprice,show_history.language,show_history.projection_type from show_history left join shows on show_history.show_id = shows.show_id left join movies on shows.movie_id=movies.movie_id left join theatres on  show_history.theatre_id=theatres.theatre_id LEFT JOIN theatre_details ON CAST(show_history.screen_id AS INTEGER) = theatre_details.screen_id where show_history.show_history_id='"+history_id+"' group by show_history.projection_type,theatre_details.screen_name,show_history.language,  movies.movie_name,show_history.silver_tprice,show_history.gold_tprice,show_history.diamond_tprice,show_history.platinum_tprice,movies.certificate,theatres.theatre_name,theatres.located_at,show_history.show_startdate,theatre_details.platinum_seats,theatre_details.screen_id,shows.show_id, theatre_details.platinum_dimension,theatre_details.diamond_seats,theatre_details.diamond_dimension,theatre_details.gold_seats,theatre_details.gold_dimension, theatre_details.silver_seats,theatre_details.silver_dimension,movies.poster_path;");
			ResultSet rs= pstmt.executeQuery();
			
			if(rs.next())
			{
				show_details.put("show_id",rs.getString("show_id"));
				show_details.put("movie_name",rs.getString("movie_name"));
				show_details.put("poster_path",rs.getString("poster_path"));
				show_details.put("screen_name",rs.getString("screen_name"));
				show_details.put("screen_id",rs.getString("screen_id"));
				show_details.put("movie_certificate",rs.getString("certificate"));
				show_details.put("theatre_name",rs.getString("theatre_name"));
				show_details.put("located_at",rs.getString("located_at"));
				show_details.put("language",rs.getString("language"));
				show_details.put("projection",rs.getString("projection_type"));
				show_details.put("show_startDate",rs.getString("show_startDate"));
				show_details.put("platinum_seats",arrayToJsonarray(rs.getArray("platinum_seats")));
				show_details.put("platinum_dimension",rs.getString("platinum_dimension"));
				show_details.put("platinum_price",rs.getString("platinum_tprice"));
				show_details.put("diamond_seats",arrayToJsonarray(rs.getArray("diamond_seats")));
				show_details.put("diamond_dimension",rs.getString("diamond_dimension"));
				show_details.put("diamond_price",rs.getString("diamond_tprice"));
				show_details.put("gold_seats",arrayToJsonarray(rs.getArray("gold_seats")));
				show_details.put("gold_dimension",rs.getString("gold_dimension"));
				show_details.put("gold_price",rs.getString("gold_tprice"));
				show_details.put("silver_seats",arrayToJsonarray(rs.getArray("silver_seats")));
				show_details.put("silver_dimension",rs.getString("silver_dimension"));
				show_details.put("silver_price",rs.getString("silver_tprice"));
			}	
			
			show_details.put("blocked_seats",getAllBlockedTickets(history_id));
			
			status="success";
			message="Show details obtained successfully";
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		show_details.put("status",status);
		show_details.put("message",message);
		
		return show_details;
	}
	
	public static JSONArray getAllBlockedTickets(String show_id)throws Exception
	{
		JSONArray seats=new JSONArray();
		
		try{
			Connection con=Database.getConnection();
			
			PreparedStatement pstmt=con.prepareStatement("SELECT coalesce(ARRAY_AGG(all_seats),'{}') as blocked_seats FROM (SELECT unnest(diamond_seats) AS all_seats FROM blocked_seats WHERE show_id = '"+show_id+"' AND ((CURRENT_TIMESTAMP  <= blocked_time + INTERVAL '7' MINUTE AND status='blocked') OR status='booked') UNION ALL SELECT unnest(platinum_seats) FROM blocked_seats WHERE show_id = '"+show_id+"' AND ((CURRENT_TIMESTAMP  <= blocked_time + INTERVAL '7' MINUTE AND status='blocked') OR status='booked') UNION ALL SELECT unnest(gold_seats) FROM blocked_seats WHERE show_id = '"+show_id+"' AND ((CURRENT_TIMESTAMP  <= blocked_time + INTERVAL '7' MINUTE AND status='blocked') OR status='booked') UNION ALL SELECT unnest(silver_seats) FROM blocked_seats WHERE show_id = '"+show_id+"' AND ((CURRENT_TIMESTAMP  <= blocked_time + INTERVAL '7' MINUTE AND status='blocked') OR status='booked')) AS unnested_seats WHERE all_seats IS NOT null ;");
			ResultSet rs=pstmt.executeQuery();
			
			while(rs.next())
			{
				seats=arrayToJsonarray(rs.getArray("blocked_seats"));
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return seats;
	}
	
	public static JSONArray getAllBookedTickets(String show_id)throws Exception
	{
		JSONArray seats=new JSONArray();
		
		try{
			Connection con=Database.getConnection();
			
			PreparedStatement pstmt=con.prepareStatement("SELECT coalesce(ARRAY_AGG(all_seats),'{}') as combined_seats FROM (SELECT unnest(diamond_seats) AS all_seats FROM blocked_seats WHERE show_id = '"+show_id+"' and status='booked' UNION ALL SELECT unnest(platinum_seats) FROM blocked_seats WHERE show_id = '"+show_id+"' AND status='booked' UNION ALL SELECT unnest(gold_seats) FROM blocked_seats WHERE show_id = '"+show_id+"' AND status='booked' UNION ALL SELECT unnest(silver_seats) FROM blocked_seats WHERE show_id = '"+show_id+"' and status='booked') AS unnested_seats WHERE all_seats IS NOT null ;");
			
			ResultSet rs=pstmt.executeQuery();
			
			if(rs.next())
			{
				seats=arrayToJsonarray(rs.getArray("combined_seats"));
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return seats;
	}
	
	public static JSONObject getBlockedTickets(String block_id)throws Exception
	{
		JSONObject seatings=new JSONObject();
		String status="error",message="Error Occurred while retrieve records!!." ;
		try{
			Connection con=Database.getConnection();
			PreparedStatement pstmt=con.prepareStatement("Select diamond_seats,platinum_seats,gold_seats,silver_seats from blocked_seats where blocking_id='"+block_id+"';");
			ResultSet rs=pstmt.executeQuery();
			
			if(rs.next())
			{
				seatings.put("diamond_seats",arrayToJsonarray(rs.getArray("diamond_seats")));
				seatings.put("platinum_seats",arrayToJsonarray(rs.getArray("platinum_seats")));
				seatings.put("gold_seats",arrayToJsonarray(rs.getArray("gold_seats")));
				seatings.put("silver_seats",arrayToJsonarray(rs.getArray("silver_seats")));
			}
			status="success";
			message="Blocked seats retrieved successfully!!..";
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		seatings.put("status",status);
		seatings.put("message",message);
		
		return seatings;
	}
	
	public static boolean isDuplicateTickets(int show_id,String diamond[],String platinum[],String gold[],String silver[])throws Exception
	{
		boolean isDuplicate=false;
		try{
			Connection con=Database.getConnection();
			PreparedStatement stmt=con.prepareStatement("select blocking_id from blocked_seats where show_id=? AND ( diamond_seats @> ?::text[] OR platinum_seats @> ?::text[] OR  gold_seats @> ?::text[] OR silver_seats @> ?::text[]) AND (CURRENT_TIMESTAMP  <= blocked_time + INTERVAL '7' MINUTE  OR status='booked');");
			stmt.setObject(1,show_id);
			stmt.setObject(2,diamond);
			stmt.setObject(3,platinum);
			stmt.setObject(4,gold);
			stmt.setObject(5,silver);
			ResultSet rs=stmt.executeQuery();
			
			if(rs.next())
			{
				isDuplicate=true;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return isDuplicate;
	}
	
	
	public static boolean isValidBlockId(String blocking_id)throws Exception
	{
		boolean isValid=false;
		try
		{
			Connection con=Database.getConnection();
			PreparedStatement stmt=con.prepareStatement("select status from blocked_seats where blocking_id = '"+blocking_id+"' and status='blocked' and  (CURRENT_TIMESTAMP  <= blocked_time + INTERVAL '7' MINUTE);");
			ResultSet rs= stmt.executeQuery();
			
			if(rs.next())
			{
				isValid=true;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return isValid;
	}
	
	public static JSONObject CheckShowClash(JSONObject params)throws Exception
	{
		JSONObject result=new JSONObject();
		String status="success";
		try
		{
			int theatre_id=Integer.parseInt(params.getString("theatre_id"));
			Date show_startDate = parseToDate(params.getString("show_startTime"),params.getString("show_startDate"),"yyyy-MM-dd HH:mm");
			Date show_endDate = parseToDate(params.getString("show_endTime"),params.getString("show_endDate"),"dd-MM-yyyy HH:mm");
        
			Connection con=Database.getConnection();
			PreparedStatement stmt=con.prepareStatement("SELECT show_id FROM show_history where show_startDate < ? AND  show_endDate > ? AND theatre_id=?  AND screen_id=?;	");	
			stmt.setObject(1,new Timestamp(show_endDate.getTime()));
			stmt.setObject(2,new Timestamp(show_startDate.getTime()));
			stmt.setObject(3,theatre_id);
			stmt.setObject(4,params.getString("screen_id"));
			ResultSet rs=stmt.executeQuery();
			
			if(rs.next())
			{
				status="error";
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		result.put("status",status);
		return result;
	}
	
	
	public static Properties getShows(String searchValue,String location,String genres,String languages)throws Exception
	{
		Properties shows=new Properties();
		try
		{
			Properties show;
			Connection conn=Database.getConnection();
			String query="select shows.show_id,shows.show_time,movies.movie_name,movies.poster_path,genres.all_genres  from shows LEFT JOIN movies on shows.movie_id = movies.movie_id LEFT JOIN (SELECT movie_genres.movie_id,STRING_AGG(genres.genre_name, '/') AS all_genres FROM movie_genres  JOIN genres ON movie_genres.genre_id = genres.genre_id  GROUP BY movie_genres.movie_id) genres ON movies.movie_id = genres.movie_id  LEFT JOIN show_history on shows.show_id=show_history.show_id LEFT JOIN theatres on show_history.theatre_id=theatres.theatre_id left join movie_genres on movies.movie_id=movie_genres.movie_id where theatres.tcity ILIKE '"+location+"' and shows.show_id IN (SELECT show_id FROM show_history WHERE status = 'Open')";
			if(!searchValue.equals(""))
			{
				query=query+" AND movies.movie_name ILIKE '%"+searchValue+"%'";
			}
			if(languages.length()!=0)
			{
				query=query+" AND show_history.language IN ("+languages+")";
			}
			
			query=query+"AND shows.show_time > '"+(new Timestamp(new Date().getTime()))+"' Group BY shows.show_id,shows.show_time,movies.movie_name, movies.poster_path, genres.all_genres";
			
			if(genres.length()!=0) 
			{
				query=query+" Having ARRAY["+genres+"] <@ string_to_array(genres.all_genres, '/')";
			}
			
			PreparedStatement stmt=conn.prepareStatement(query+";");
			ResultSet rs=stmt.executeQuery();
			
			while(rs.next())
			{
				show=new Properties();
				show.put("show_id",rs.getString("show_id"));
				show.put("movie_name",rs.getString("movie_name"));
				show.put("genre",rs.getString("all_genres"));
				show.put("poster_path",rs.getString("poster_path"));
				shows.put(rs.getString("show_id"),show);
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return shows;
	}
	
	public static ArrayList getShowDates(String show_id)throws Exception
	{
		ArrayList<Date> show_dates =new ArrayList<>();
		try{
			Connection con=Database.getConnection();
			PreparedStatement pstmt=con.prepareStatement("select Distinct cast(show_history.show_startdate as DATE) as unique_dates from show_history join shows on shows.show_id=show_history.show_id where show_history.show_id='"+show_id+"' ORDER BY unique_dates ASC ;");
			ResultSet rs= pstmt.executeQuery();
			
			while(rs.next())
			{
				show_dates.add(rs.getDate("unique_dates"));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return show_dates;
	}
	
	
	
	public static Properties getMovieDetails(String show_id)throws Exception
	{
		Properties movie_details=new Properties();
		try
		{
			Connection conn=Database.getConnection();
			PreparedStatement pstmt=conn.prepareStatement("SELECT   movies.movie_name,  movies.overview,  movies.certificate,  movies.poster_path,  movies.run_time,  movies.release_date,  genres.all_genres,  json_agg(json_build_object('cast_name', cast_details.cast_name, 'profile_path', cast_details.profile_path, 'department', cast_details.department)) AS cast_details FROM shows LEFT JOIN movies ON shows.movie_id = movies.movie_id LEFT JOIN (SELECT movie_genres.movie_id, STRING_AGG(genres.genre_name, '/') AS all_genres  FROM   movie_genres  JOIN genres ON movie_genres.genre_id = genres.genre_id  GROUP BY movie_genres.movie_id) genres ON movies.movie_id = genres.movie_id JOIN LATERAL unnest(movies.cast_ids) castId ON TRUE JOIN cast_details ON cast_details.cast_id = castId WHERE shows.show_id ='"+show_id+"' GROUP BY movies.movie_name,  movies.overview,movies.certificate,  movies.poster_path,  movies.run_time,  movies.release_date,  genres.all_genres;");
			ResultSet rs=pstmt.executeQuery();
			
			if(rs.next())
			{
				movie_details.put("movie_name",rs.getString("movie_name"));
				movie_details.put("movie_overview",rs.getString("overview"));
				movie_details.put("movie_certificate",rs.getString("certificate"));
				movie_details.put("poster_path",rs.getString("poster_path"));
				movie_details.put("movie_runtime",rs.getString("run_time"));
				movie_details.put("movie_releaseDate",rs.getString("release_date"));
				movie_details.put("movie_genres",rs.getString("all_genres"));
				String castDetailsJson = rs.getString("cast_details");
				JSONArray cast_details = new JSONArray(castDetailsJson);
				
				movie_details.put("cast_details",cast_details);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return movie_details;
	}
	
	public static JSONArray getAllshows(String show_id,Date show_date,JSONArray showTimeFilter)throws Exception
	{
		JSONArray show_details=new JSONArray();
		try
		{
			Properties shows;
			Connection conn=Database.getConnection();
			
			String query="SELECT theatres.theatre_id,theatres.theatre_name,theatres.located_at,jsonb_agg(json_build_object('screen_id', show_history.screen_id,'screen_name', theatre_details.screen_name,'show_date', show_history.show_startdate,'show_history_id', show_history.show_history_id,'language', show_history.language,'projection_type', show_history.projection_type) ORDER BY show_history.show_startdate ASC) AS shows FROM shows LEFT JOIN show_history ON shows.show_id = show_history.show_id LEFT JOIN theatres ON theatres.theatre_id = show_history.theatre_id LEFT JOIN theatre_details ON cast(show_history.screen_id as integer) = theatre_details.screen_id Where shows.show_id = '"+show_id+"' AND DATE(show_history.show_startdate) = '"+show_date+"'";
			
			if(showTimeFilter.length()!=0)
			{
				String filterquery =" AND( ";
				int length=showTimeFilter.length();
				for(int i=0;i<length;i++)
				{
					String[] time = showTimeFilter.getString(i).split("-");
					filterquery=filterquery+"(EXTRACT(HOUR FROM show_history.show_startdate) >= "+time[0]+" AND EXTRACT(MINUTE FROM show_history.show_startdate) >= 0 AND EXTRACT(HOUR FROM show_history.show_startdate) <= "+time[1]+" AND EXTRACT(MINUTE FROM show_history.show_startdate) <= 59)";
					
					if(i+1!=length)
						filterquery=filterquery+" OR ";
				}
				query=query+filterquery+" )";
			}
			
			query=query+" GROUP BY theatres.theatre_id, theatres.theatre_name, theatres.located_at;";
			
			PreparedStatement pstmt=conn.prepareStatement(query);
			ResultSet rs=pstmt.executeQuery();
			
			while(rs.next())
			{
				shows=new Properties();
				shows.put("theatre_name",rs.getString("theatre_name"));
				shows.put("theatre_locatedAt",rs.getString("located_at"));
				// String show_detailsString = ;
				JSONArray show_detailsJSON = new JSONArray(rs.getString("shows"));
				shows.put("show_details",show_detailsJSON);
				show_details.put(shows);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			
		}
		return show_details;
	}
	
	public static JSONObject getScreenDetails(String screen_id,String user_id)throws Exception
	{
		JSONObject result=new JSONObject();
		String status="error",message="Error occurred...";
		try{
			Connection con=Database.getConnection();
			
			String query="select theatres.theatre_name,theatres.located_at,theatre_details.screen_id ,theatre_details.screen_name,theatre_details.platinum_seats,theatre_details.platinum_dimension,theatre_details.diamond_seats,theatre_details.diamond_dimension ,theatre_details.gold_seats,theatre_details.gold_dimension,theatre_details.silver_seats,theatre_details.silver_dimension from theatre_details left join theatres on theatre_details.theatre_id=theatres.theatre_id where screen_id='"+screen_id+"' and theatres.userid='"+user_id+"'";

			
			PreparedStatement pstmt=con.prepareStatement(query);
			ResultSet rs=pstmt.executeQuery();
			
			if(rs.next())
			{
				result.put("theatre_name",rs.getString("theatre_name"));
				result.put("located_at",rs.getString("located_at"));
				result.put("screen_id",rs.getString("screen_id"));
				result.put("screen_name",rs.getString("screen_name"));
				result.put("platinum_seats",arrayToJsonarray(rs.getArray("platinum_seats")));
				result.put("platinum_dimension",rs.getString("platinum_dimension"));
				result.put("diamond_seats",arrayToJsonarray(rs.getArray("diamond_seats")));
				result.put("diamond_dimension",rs.getString("diamond_dimension"));
				result.put("gold_seats",arrayToJsonarray(rs.getArray("gold_seats")));
				result.put("gold_dimension",rs.getString("gold_dimension"));
				result.put("silver_seats",arrayToJsonarray(rs.getArray("silver_seats")));
				result.put("silver_dimension",rs.getString("silver_dimension"));
				status="success";
				message="Screen details retrieved successfully";
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		result.put("status",status);
		result.put("message",message);
		
		return result;
	}
	
	public static JSONObject getBookingHistories(String user_id,String date1,String date2)throws Exception
	{
		JSONObject histories=new JSONObject();
		try
		{
			JSONObject history;
			JSONArray valid_tickets=new JSONArray();
			JSONArray expired_tickets=new JSONArray();
			Connection con=Database.getConnection();
			String query="select booking_history.booking_id,booking_history.price, booking_history.encodedqr, COALESCE(NULLIF(blocked_seats.diamond_seats, '{}'),NULLIF(blocked_seats.platinum_seats, '{}'),NULLIF(blocked_seats.gold_seats, '{}'),NULLIF(blocked_seats.silver_seats, '{}')) AS seats,movies.movie_name, movies.certificate,  movies.poster_path, show_history.language, show_history.projection_type, show_history.show_startdate, theatres.theatre_name,theatres.located_at,theatre_details.screen_name from booking_history left join blocked_seats on booking_history.block_id = blocked_seats.blocking_id left join show_history on booking_history.show_id = show_history.show_history_id left join shows on shows.show_id = show_history.show_id left join movies on movies.movie_id = shows.movie_id  left join theatres on show_history.theatre_id=theatres.theatre_id left join theatre_details on theatre_details.theatre_id=theatres.theatre_id where user_id = ? ";
			
			if(!date1.equals("") && date1!=null && !date2.equals("") && date2!=null)
			{
				query+="And show_history.show_startdate >= '"+date1+" 00:00:00.000' and show_history.show_startdate <= '"+date2+" 00:00:00.000' ";
			}
			
			query+=" order by show_history.show_startdate asc ;";
			PreparedStatement pstmt=con.prepareStatement(query+";");
			pstmt.setString(1,user_id);
			ResultSet rs=pstmt.executeQuery();
			
			while(rs.next())
			{
				history=new JSONObject();
				history.put("booking_id",rs.getString("booking_id"));
				history.put("price",rs.getString("price"));
				history.put("encodedqr",rs.getString("encodedqr"));
				history.put("movie_name",rs.getString("movie_name"));
				history.put("poster_path",rs.getString("poster_path"));
				history.put("certificate",rs.getString("certificate"));
				history.put("language",rs.getString("language"));
				history.put("projection_type",rs.getString("projection_type"));
				String date=rs.getString("show_startdate");
				history.put("show_startDate",date);
				history.put("theatre_name",rs.getString("theatre_name"));
				history.put("located_at",rs.getString("located_at"));
				history.put("screen_name",rs.getString("screen_name"));
				history.put("seats",arrayToJsonarray(rs.getArray("seats")));
				
				if(rs.getDate("show_startDate").before(new Date()))
				{
					expired_tickets.put(history);
				}else {
					valid_tickets.put(history);
				}
			}
			
			histories.put("valid_tickets",valid_tickets);
			histories.put("expired_tickets",expired_tickets);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return histories;
	}
	
	public static JSONArray getShowHistories(String user_id)throws Exception
	{
		JSONArray histories=new JSONArray();
		try
		{
			Properties movie_details=new Properties(),history,theatre_details=new Properties();
			JSONArray show_details=new JSONArray(),theatres=new JSONArray();
			String theatre_id="",movie_id="";
			
			String query = "SELECT show_history.show_history_id,show_history.show_id,show_history.theatre_id,show_history.show_startdate, show_history.status,show_history.screen_id,ARRAY_LENGTH(theatre_details.diamond_seats,1) + ARRAY_LENGTH(theatre_details.platinum_seats, 1) + ARRAY_LENGTH(theatre_details.gold_seats, 1) + ARRAY_LENGTH(theatre_details.silver_seats, 1) AS total_seats,theatres.theatre_name,    theatres.located_at, theatre_details.screen_id,theatre_details.screen_name,movies.movie_id,movies.movie_name,movies.overview,movies.certificate, movies.poster_path,movies.release_date,movies.run_time,movies.backdrop_path,show_history.show_startdate,STRING_AGG(genres.genre_name, ',') AS all_genres FROM show_history LEFT JOIN theatres ON show_history.theatre_id = theatres.theatre_id LEFT JOIN theatre_details ON CAST(show_history.screen_id AS INTEGER) = theatre_details.screen_id LEFT JOIN shows ON show_history.show_id = shows.show_id LEFT JOIN movies ON shows.movie_id = movies.movie_id LEFT JOIN movie_genres ON movies.movie_id = movie_genres.movie_id LEFT JOIN genres ON genres.genre_id = movie_genres.genre_id where theatres.userid = ? GROUP BY show_history.show_history_id,show_history.show_id,show_history.theatre_id, show_history.screen_id,theatres.theatre_name,theatres.located_at,theatre_details.screen_id,theatre_details.screen_name, movies.movie_id,movies.movie_name,movies.overview,movies.certificate, movies.poster_path,movies.release_date,movies.run_time, movies.backdrop_path,shows.movie_id,show_history.status ORDER BY shows.movie_id;";
			
			Connection con=Database.getConnection();
			PreparedStatement pstmt=con.prepareStatement(query);
			pstmt.setString(1,user_id);
			ResultSet rs=pstmt.executeQuery();
			
			while(rs.next())
			{
				if(!theatre_id.equals(rs.getString("theatre_id")) || !movie_id.equals(rs.getString("movie_id"))) 
				{
					if(show_details.length() > 0)
						theatre_details.put("show_details",show_details);
					if(theatre_details.size() > 0)
						theatres.put(theatre_details);
					
					
					theatre_id=rs.getString("theatre_id");
					theatre_details=new Properties();
					theatre_details.put("theatre_name",rs.getString("theatre_name"));
					theatre_details.put("located_at",rs.getString("located_at"));
					
					show_details=new JSONArray();
				}
				
				if(!movie_id.equals(rs.getString("movie_id")))
				{
					if(theatres.length() > 0)
						movie_details.put("theatres",theatres);
					if(movie_details.size() > 0)
						histories.put(movie_details);
					
					movie_id=rs.getString("movie_id");
					movie_details=new Properties();
					movie_details.put("movie_id",movie_id);
					movie_details.put("title",rs.getString("movie_name"));
					movie_details.put("genres",rs.getString("all_genres"));
					movie_details.put("overview",rs.getString("overview"));
					movie_details.put("poster_path",rs.getString("poster_path"));
					movie_details.put("backdrop_path",rs.getString("backdrop_path"));
					movie_details.put("certificate",rs.getString("certificate"));
					movie_details.put("run_time",rs.getString("run_time"));
					movie_details.put("release_date",rs.getString("release_date"));
					theatres=new JSONArray();
				}
				
				history=new Properties();
				history.put("Screen Name",rs.getString("screen_name"));
				
				Date date=rs.getDate("show_startDate");
				String history_id=rs.getString("show_history_id"),status=rs.getString("status");
				
				int total_seats=rs.getInt("total_seats"),
				    avail_tickets=calculateAvailableSeats(history_id,total_seats),
					earnings=getTotalEarnings(history_id);
					
				history.put("Show Date&Time",parseToString(date,"hh:mmaa, dd-MM-yyyy"));	
				history.put("history_id",history_id);
				history.put("Available Tickets",avail_tickets);
				history.put("Total Earnings",earnings);
				
				if(date.before(new Date()))
					status="Expired";
					
				history.put("Status",status);
				
				show_details.put(history);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return histories;
	}
	
	public static int calculateAvailableSeats(String history_id,int total_seats)throws Exception
	{
		int avail_tickets=0;
		try
		{
			avail_tickets=total_seats - getAllBookedTickets(history_id).length();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return avail_tickets;
	}
	
	public static int getTotalEarnings(String history_id)throws Exception
	{
		int earnings=0;
		try
		{
			Connection con=Database.getConnection();
			PreparedStatement pstmt=con.prepareStatement("SELECT COALESCE(SUM(price), 0) AS total_price FROM booking_history WHERE show_id = ?;");
			pstmt.setInt(1,Integer.parseInt(history_id));
			ResultSet rs=pstmt.executeQuery();
			
			if(rs.next())
			{
				earnings=rs.getInt("total_price");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return earnings;
	}
	
}