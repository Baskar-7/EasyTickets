
package easytickets;

import utils.Database;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.io.*;
import java.time.LocalTime;
import java.time.ZoneId;
import org.json.JSONObject;
import org.json.JSONArray;

import utils.Util;

public class UpdateHandler extends Util
{
	public static String addUser(Properties props)
	{
		String  userId="";
		try
		{
			String query="select userId from accounts where mail ='"+props.get("Mail")+"';";
			Connection con=Database.getConnection();
			//sessionValidity varchar(25)
			
			userId=Database.getRecord(query);
			if(userId==null)
			{
				userId=(UUID.randomUUID()).toString();
				PreparedStatement st=con.prepareStatement("insert into accounts(fname, mail, mobile,acc_type,userid,pincode,city,state,lname,dob,gender,profile_pic) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
				st.setString(1,getStringProperty(props,"Name"));
				st.setString(2,getStringProperty(props,"Mail"));
				st.setString(3,getStringProperty(props,"Mobile"));
				st.setString(4,getStringProperty(props,"acc_type"));
				st.setString(5,userId);
				st.setString(6,"");
				st.setString(7,"");
				st.setString(8,"");
				st.setString(9,"");
				st.setString(10,"");
				st.setString(11,"");
				st.setBytes(12,new byte[0]);
				st.executeUpdate();
				st.close();
			}
			else if((props.getProperty("acc_type"))=="Host")
			{
				toggleAccount(userId,"Host");
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return userId;
	}
	
	public static void removeOtpDetails(String mail)
	{
		try{
			String tableName="otp";
			Connection con=Database.getConnection();
			PreparedStatement st=con.prepareStatement("delete from"+tableName+" where mail='"+mail+"';");
			st.executeUpdate();
			st.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
		// add user otp based on the mail and its validity
	
	public static void addOtpDetails(String mail,String OTP)
	{
		try{
			String tableName="otp";
			Connection con=Database.getConnection();

			ZoneId istZone = ZoneId.of("Asia/Kolkata");
			LocalTime currentTime = LocalTime.now(istZone);
			LocalTime localtime = currentTime.plusMinutes(5);
			Time otpValidity = Time.valueOf(localtime);
			PreparedStatement st=null;
			if(Database.getRecord("select * from "+tableName+" where mail='"+mail+"';")!=null)
			{
				st=con.prepareStatement("update "+tableName+" set oneTimePassword='"+OTP+"',validity='"+otpValidity+"' where mail='"+mail+"';");
				st.executeUpdate();
			}
			else{
				st=con.prepareStatement("insert into "+tableName+" values(?,?,?)");
				st.setString(1,mail);
				st.setString(2,OTP);
				st.setTime(3,otpValidity);
				st.executeUpdate();
			}
			st.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	     // Update user details based on given attribute
	
	// public static void updateUserAttribute(String attribute,String value,String userID)
	// {
		// try
		// {
			// Connection con=Database.getConnection();
			// PreparedStatement st=con.prepareStatement("update users set "+attribute+"=? where userID='"+userID+"';");
			// st.setString(1,value);
			// st.executeUpdate();
		// }
		// catch(Exception e){
			// e.printStackTrace();
		// }
	// }
	
	public static JSONObject updateProfilePicture(byte[] fileBytes,String user_id)throws Exception
	{
		JSONObject result=new JSONObject();
		String status="error",message="Cannot Update Profile Picture...";
		try
		{
			Connection con=Database.getConnection();
			PreparedStatement stmt=con.prepareStatement("update accounts set profile_pic=? where userId=?;");
			stmt.setBytes(1, fileBytes);
			stmt.setString(2, user_id);
			stmt.executeUpdate();
			
			status="success";
			message="Profile Picture Updated Successfully!..";
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		result.put("status",status);
		result.put("message",message);
		return result;
	}
	
	public static JSONObject toggleAccount(String userId,String acc_type)throws Exception
	{
		JSONObject result=new JSONObject();
		String status="error",message="An error occurred while changing the Account Type!..";
		try{
			if(ViewHandler.checkAccountExist(userId))
			{
				Connection con=Database.getConnection();
				PreparedStatement st=con.prepareStatement("update accounts set acc_type='"+acc_type+"' where userid='"+userId+"';");
				st.executeUpdate();
				st.close();
				status="success";
				message="Successfully updated to "+acc_type+" account!..";
			}
			else
			{
				message="Account doesn't Exist";
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		result.put("status",status);
		result.put("message",message);
		return result;
	}
	
	public static JSONObject addNewhostRequest(JSONObject params)throws Exception
	{
		JSONObject result=new JSONObject();
		String status="error",message="A host account already exists for this Credentials!..",mail=params.getString("mail");
		try{			
			String query="select userId from accounts where mail ='"+mail+"';";
			Connection con=Database.getConnection();
			
			String userId=Database.getRecord(query);
			if(!ViewHandler.checkDuplicateHostAccount(mail))
			{
				if(!ViewHandler.checkDuplicateReq(mail))
				{
					if(!ViewHandler.checkDuplicateTheatre(params.getString("theatre_name"),params.getString("pincode")))
					{
						PreparedStatement st=con.prepareStatement("insert into host_requests (fname, mail, mobile,theatre_name,located_at, tpincode, tcity, tstate) values(?,?,?,?,?,?,?,?)");
						st.setString(1,params.getString("name"));
						st.setString(2,mail);
						st.setString(3,params.getString("mobile"));
						st.setString(4,params.getString("theatre_name"));
						st.setString(5,params.getString("located_at"));
						st.setString(6,params.getString("pincode"));
						st.setString(7,params.getString("city"));
						st.setString(8,params.getString("state"));
						st.executeUpdate();
						st.close();
						status="success";
						message="Your form submitted successfully!! We will connect back you shortly...";
					}
					else
					{
						status="info";
						message="Theatre Details already exist with this pincode!!..";
					}
				}
				else{
					message="Your previous form is still in the waiting state";
				}
			}
		}
		catch(Exception e)
		{
			status="error";
			message="Error occurred while submitting the form";
			e.printStackTrace();
		}
		result.put("status",status);
		result.put("message",message);
		
		return result;
	}
	
	
	public static JSONObject deleteAccount(String userId)throws Exception
	{
		JSONObject result=new JSONObject();
		String status="error",message="Error Occurred while removing this account!!";
		try{
			Connection con=Database.getConnection();
			PreparedStatement stmt=con.prepareStatement("delete from accounts where userId='"+userId+"';");
			stmt.executeUpdate();
			status="success";
			message="Account Removed successfully!..";
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		result.put("status",status);
		result.put("message",message);
		return result;
	}
	
	public static JSONObject deleteHostRequest(String requestId)throws Exception
	{
		JSONObject result=new JSONObject();
		String status="error",message="Error Occurred while deny the Request!!";
		try{
			Connection con=Database.getConnection();
			PreparedStatement stmt=con.prepareStatement("delete from host_requests where request_id='"+requestId+"';");
			stmt.executeUpdate();
			status="success";
			message="Request Denied successfully!..";
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		result.put("status",status);
		result.put("message",message);
		return result;
	}
	
	public static String toggleTheatreStatus(String theatre_id,String status)throws Exception
	{
		String toggleStatus="error";
		try{
			Connection con=Database.getConnection();
			PreparedStatement stmt=con.prepareStatement("update theatres set status='"+status+"' where theatre_id='"+theatre_id+"';");
			stmt.executeUpdate();
			toggleStatus="success";
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return toggleStatus;
	}
	
	public static JSONObject deleteTheatre(String theatre_id)throws Exception
	{
		JSONObject result=new JSONObject();
		String status="error",message="Error Occurred while remove the Theatre Details!!";
		try{
			Connection con=Database.getConnection();
			PreparedStatement stmt=con.prepareStatement("delete from theatres where theatre_id='"+theatre_id+"';");
			stmt.executeUpdate();
			status="success";
			message="Theatre details removed successfully!..";
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		result.put("status",status);
		result.put("message",message);
		return result;
	}
	
	public static JSONObject deleteScreen(String screen_id)throws Exception
	{
		JSONObject result=new JSONObject();
		String status="error",message="Error Occurred while remove the Screen Details!!";
		try{
			Connection con=Database.getConnection();
			PreparedStatement stmt=con.prepareStatement("delete from theatre_details where screen_id='"+screen_id+"';");
			stmt.executeUpdate();
			status="success";
			message="Screen details removed successfully!..";
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		result.put("status",status);
		result.put("message",message);
		return result;
	}
	
	public static JSONObject addNewTheatre(Properties theatre)throws Exception
	{
		JSONObject resultObj=new JSONObject();
		String status="error",message="Theatre Details already exist with this pincode!!..";
		try
		{
			String theatrename=theatre.getProperty("TheatreName"),
				   pincode=theatre.getProperty("Pincode");
				   
			Connection con=Database.getConnection();

			if(!ViewHandler.checkDuplicateTheatre(theatrename,pincode))
			{
				PreparedStatement pstmt = con.prepareStatement("INSERT INTO theatres (theatre_name,tcity,userId,tstate,tpincode,status,located_at) values(?,?,?,?,?,?,?)");
				pstmt.setString(1,theatrename);
				pstmt.setString(2,getStringProperty(theatre,"City"));
				pstmt.setString(3,getStringProperty(theatre,"userId"));
				pstmt.setString(4,getStringProperty(theatre,"State"));
				pstmt.setString(5,getStringProperty(theatre,"Pincode"));
				pstmt.setString(6,getStringProperty(theatre,"status"));
				pstmt.setString(7,getStringProperty(theatre,"located_at"));
				pstmt.executeUpdate();
				
				status="success";
				message="Theatre Details are added Successfully";
			}

		}catch(Exception e)
		{
			e.printStackTrace();
		}
		resultObj.put("status",status);
		resultObj.put("message",message);
		
		return resultObj;
	}
	
	
	public static String addTheatreDetails(Properties theatre_details)throws Exception
	{
		String showId="";
		try
		{
			String tableName="theatre_details";
				   
			Connection con=Database.getConnection();

			PreparedStatement pstmt = con.prepareStatement("INSERT INTO "+tableName+"(theatre_id,silver_seats,gold_seats,platinum_seats,diamond_seats) values(?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
			pstmt.setInt(1,Integer.parseInt(theatre_details.getProperty("theatre_id")));
			pstmt.setString(2,theatre_details.getProperty("silver_seats"));
			pstmt.setString(3,theatre_details.getProperty("gold_seats"));
			pstmt.setString(4,theatre_details.getProperty("platinum_seats"));
			pstmt.setString(5,theatre_details.getProperty("diamond_seats"));
			pstmt.executeUpdate();
			ResultSet rs = pstmt.getGeneratedKeys();
			if (rs.next()) {
					showId=rs.getString("theatre_id");
				}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return showId;
	}
	
	public static JSONObject addScreen(Properties screen_details)throws Exception
	{
		JSONObject resultObj=new JSONObject();
		String status="error",message="Error occurred while adding screen details!!..";
		try
		{
			String theatre_id=screen_details.getProperty("theatre_id"),
				   screen_name=screen_details.getProperty("screen_name"),
				   user_id=screen_details.getProperty("user_id");
				   
			Connection con=Database.getConnection();

			if(!ViewHandler.checkDuplicateScreen(user_id,screen_name))
			{
				PreparedStatement pstmt = con.prepareStatement("INSERT INTO theatre_details(theatre_id,screen_name,silver_seats,gold_seats,platinum_seats,diamond_seats,silver_dimension,gold_dimension,diamond_dimension,platinum_dimension) values(?,?,?,?,?,?,?,?,?,?)");
				pstmt.setInt(1,Integer.parseInt(theatre_id));
				pstmt.setObject(2,screen_details.get("screen_name"));
				pstmt.setObject(3,screen_details.get("silver_seats"));
				pstmt.setObject(4,screen_details.get("gold_seats"));
				pstmt.setObject(5,screen_details.get("platinum_seats"));
				pstmt.setObject(6,screen_details.get("diamond_seats"));
				pstmt.setObject(7,screen_details.get("silver_dimension"));
				pstmt.setObject(8,screen_details.get("gold_dimension"));
				pstmt.setObject(9,screen_details.get("diamond_dimension"));
				pstmt.setObject(10,screen_details.get("platinum_dimension"));
				pstmt.executeUpdate();
				
				status="success";
				message="Screen details are added Successfully";
			}
			else
			{
				message="Screen already exists with same name";
			}

		}catch(Exception e)
		{
			e.printStackTrace();
		}
		resultObj.put("status",status);
		resultObj.put("message",message);
		
		return resultObj;
	}
	
	
	public static JSONObject updateProfileDetails(Properties profile_details)throws Exception
	{
		JSONObject resultObj=new JSONObject();
		String status="error",message="Error occurred while updating profile details!!..";
		try
		{
			String userId=profile_details.getProperty("userId");
				   
			Connection con=Database.getConnection();

			PreparedStatement pstmt = con.prepareStatement("UPDATE accounts SET fname=?, lname=?, mobile=?, gender=?, dob=?, pincode=?, city=?, state=? WHERE userId ='"+userId+"'");
			pstmt.setString(1,getStringProperty(profile_details,"fname"));
			pstmt.setString(2,getStringProperty(profile_details,"lname"));
			pstmt.setString(3,getStringProperty(profile_details,"mobile"));
			pstmt.setString(4,getStringProperty(profile_details,"gender"));
			pstmt.setString(5,getStringProperty(profile_details,"dob"));
			pstmt.setString(6,getStringProperty(profile_details,"pincode"));
			pstmt.setString(7,getStringProperty(profile_details,"city"));
			pstmt.setString(8,getStringProperty(profile_details,"state"));
			pstmt.executeUpdate();
				
			status="success";
			message="Profile details are Updated Successfully!!..";

		}catch(Exception e)
		{
			e.printStackTrace();
		}
		resultObj.put("status",status);
		resultObj.put("message",message);
		
		return resultObj;
	}
	
	public static JSONObject updateTheatreDetails(Properties screen_details)throws Exception
	{
		 JSONObject resultObj=new JSONObject();
		String status="error",message="Error occurred while updating Theatre details!!..";
		try
		{
			String screen_id=screen_details.getProperty("screen_id"),user_id=screen_details.getProperty("user_id");
				   
			Connection con=Database.getConnection();

			PreparedStatement pstmt = con.prepareStatement("UPDATE theatre_details SET screen_name=?, silver_seats=?, gold_seats=?, platinum_seats=?, diamond_seats=?,silver_dimension=?,gold_dimension=?,diamond_dimension=?,platinum_dimension=? FROM theatres WHERE theatre_details.theatre_id = theatres.theatre_id AND theatres.userid = '"+user_id+"' AND theatre_details.screen_id = '"+screen_id+"'");
			pstmt.setObject(1,screen_details.get("screen_name"));
			pstmt.setObject(2,screen_details.get("silver_seats"));
			pstmt.setObject(3,screen_details.get("gold_seats"));
			pstmt.setObject(4,screen_details.get("platinum_seats"));
			pstmt.setObject(5,screen_details.get("diamond_seats"));
			pstmt.setObject(6,screen_details.get("silver_dimension"));
			pstmt.setObject(7,screen_details.get("gold_dimension"));
			pstmt.setObject(8,screen_details.get("diamond_dimension"));
			pstmt.setObject(9,screen_details.get("platinum_dimension"));
			pstmt.executeUpdate();
				
			status="success";
			message="Theatre details are Updated Successfully!!..";

		}catch(Exception e)
		{
			e.printStackTrace();
		}
		resultObj.put("status",status);
		resultObj.put("message",message);
		
		return resultObj;
	}
	
	public static JSONObject addNewShows(JSONArray details)throws Exception
	{
		JSONObject resultObj=new JSONObject();
		String status="error",message="Error occurred while adding shows!!..";
		
		try{
			JSONObject obj1 = details.getJSONObject(0);
			int show_id=getOrCreateShowId(obj1.getString("movie_id"));
			Date showTime=null;
			
			Connection conn=Database.getConnection();
            String insertSQL = "INSERT INTO show_history (show_id, theatre_id, screen_id, language, projection_type,show_startDate,show_endDate,silver_tprice,gold_tprice,platinum_tprice,diamond_tprice) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement preparedStatement = conn.prepareStatement(insertSQL);

            List<Object[]> batchData = new ArrayList<>();

			for (int i=0;i<details.length();i++) {
				JSONObject obj = details.getJSONObject(i);
				
				Date newTime=parseToDate(obj.getString("show_startTime"),obj.getString("show_startDate"),"yyyy-MM-dd HH:mm");
				if(showTime==null || showTime.after(newTime))
				{
					showTime=newTime;
				}
				
				Date endDate=parseToDate(obj.getString("show_endTime"),obj.getString("show_endDate"),"dd-MM-yyyy HH:mm");
				
				batchData.add(new Object[]{
					show_id,
					obj.getInt("theatre_id"),
					obj.getString("screen_id"),
					obj.getString("show_language"),
					obj.getString("show_projection"),
					// obj.getString("show_startTime"),
					// obj.getString("show_endTime"),
					// obj.getString("show_startDate"),
					// obj.getString("show_endDate"),
					new Timestamp(newTime.getTime()),
					new Timestamp(endDate.getTime()),
					obj.getString("silver"),
					obj.getString("gold"),
					obj.getString("platinum"),
					obj.getString("diamond"),
				});
			}

			for (Object[] rowData : batchData) {
				for (int i = 0; i < rowData.length; i++) {
					preparedStatement.setObject(i + 1, rowData[i]);
				}
				preparedStatement.addBatch();
			}
            preparedStatement.executeBatch();
			
			updateShowTime(showTime,show_id);
			
			status="success";
			message="Shows are added Successfully!!..";

		}catch(Exception e)
		{
			e.printStackTrace();
		}
		resultObj.put("status",status);
		resultObj.put("message",message);
		
		return resultObj;
	}
	
	public static int getOrCreateShowId(String movie_id)
	{
		String show_id=null;
		try{
			show_id=Database.getRecord("Select show_id from shows where movie_id ='"+movie_id+"';");
			if(show_id==null)
			{
				Connection con=Database.getConnection();
				PreparedStatement pstmt=con.prepareStatement("Insert into shows(movie_id)values(?)",Statement.RETURN_GENERATED_KEYS);
				pstmt.setString(1,movie_id);
				pstmt.executeUpdate();
				
				ResultSet rs = pstmt.getGeneratedKeys();
				if (rs.next()) {
					show_id=rs.getString("show_id");
				}
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return Integer.parseInt(show_id);
	}
	
	public static void updateShowTime(Date showTime,int show_id)throws Exception
	{
		try{
			String exTime=Database.getRecord("select show_time from shows where show_id='"+show_id+"';");
			
			Date parsed_date=parseToDate(null,exTime,"EEE MMM dd HH:mm:ss zzz yyyy");
			if(parsed_date==null || parsed_date.before(showTime))
			{
				Connection con=Database.getConnection();
				PreparedStatement pstmt=con.prepareStatement("update shows set show_time='"+new Timestamp(showTime.getTime())+"' where show_id='"+show_id+"';");
				pstmt.executeUpdate();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}		
	}
	
	public static JSONObject insertMovieDetails(JSONObject movie_details)throws Exception
	{
		JSONObject resultObj=new JSONObject();
		String status="success",message="Shows are added successfully!!..";
		boolean genreInsertionStatus=true,castInsertionStatus=false;
		Connection conn=null;
		
		try{
			String movie_id=movie_details.getString("imdb_id");
			if((Database.getRecord("select movie_id from movies where movie_id='"+movie_id+"';"))==null)
			{
				String[] castIds={};
				JSONObject cast_details=insertCastDetails(movie_details.getJSONArray("cast"));
				if(!(cast_details.get("status")).equals("error"))
				{
					castInsertionStatus=true;
					castIds=(cast_details.getString("castIds")).split(",");
				}
				conn=Database.getConnection();
				conn.setAutoCommit(false);
				String query="INSERT INTO movies(movie_id,movie_name,overview,run_time,certificate,poster_path,release_date,cast_ids,backdrop_path) values(?,?,?,?,?,?,?,?,?)";
				PreparedStatement pstmt=conn.prepareStatement(query);
				pstmt.setString(1,movie_details.getString("imdb_id"));
				pstmt.setString(2,movie_details.getString("title"));
				pstmt.setString(3,movie_details.getString("overview"));
				pstmt.setString(4,movie_details.getString("runtime"));
				pstmt.setString(5,movie_details.getString("certification"));
				pstmt.setString(6,movie_details.getString("poster_path"));
				pstmt.setString(7,movie_details.getString("release_date"));
				pstmt.setObject(8,castIds);
				pstmt.setString(9,movie_details.getString("backdrop_path"));
				int rowsInserted = pstmt.executeUpdate();

				if (rowsInserted > 0) {
					JSONArray genres=movie_details.getJSONArray("genres");
					pstmt=conn.prepareStatement("Insert into movie_genres(movie_id,genre_id) values(?,?)");
					
					List<Object[]> batchData = new ArrayList<>();
					for(int i=0;i<genres.length();i++)
					{
						String genre_name=String.valueOf(genres.get(i));
						int genre_id=getorInsertGenreId(genre_name);
						batchData.add(new Object[]{
							movie_id,
							genre_id
						});
					}
					for (Object[] rowData : batchData) {
						for (int i = 0; i < rowData.length; i++) {
							pstmt.setObject(i + 1, rowData[i]);
						}
						pstmt.addBatch();
					}
					
					int[] updateCounts = pstmt.executeBatch(); 
					
					for (int i=0;i<updateCounts.length;i++) {
						if (updateCounts[i] == pstmt.EXECUTE_FAILED) {
							i=updateCounts.length;
							genreInsertionStatus=false;
						}
					}
					conn.commit();
					conn.setAutoCommit(true); 
				}
				else
				{
					status="error";
					message="Error occurred while adding shows!!..";
					conn.rollback();
				}
			}
		}
		catch(Exception e)
		{
			 if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException rollbackException) {
					rollbackException.printStackTrace();
				}
			}
			e.printStackTrace();
			message="Error occurred while adding movie details!!..";
			status="error";
			}
		resultObj.put("genreInsertionStatus",genreInsertionStatus);
		resultObj.put("castInsertionStatus",castInsertionStatus);
		resultObj.put("status",status);
		resultObj.put("message",message);
		return resultObj;
	}
	
	public static int getorInsertGenreId(String genre)
	{
		String genre_id=null;
		try
		{
			Connection con=Database.getConnection();
			String query="select genre_id from genres where genre_name ='"+genre+"';";
			genre_id=Database.getRecord(query);
			if(genre_id==null)
			{
				PreparedStatement pstmt=con.prepareStatement("Insert into genres(genre_name)values(?)",Statement.RETURN_GENERATED_KEYS);
				pstmt.setString(1,genre);
				pstmt.executeUpdate();
				
				ResultSet rs = pstmt.getGeneratedKeys();
				if (rs.next()) {
					genre_id=rs.getString("genre_id");
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return Integer.parseInt(genre_id);
	}
	
	public static JSONObject insertCastDetails(JSONArray cast_details)throws Exception
	{
		JSONObject result=new JSONObject();
		String status="success",message="Unable to update cast_details..",castIds="";
		try
		{
			Connection con=Database.getConnection();
			String query="INSERT INTO cast_details(cast_id,cast_name,profile_path,department)values(?,?,?,?);";
			PreparedStatement preparedStatement = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			List<Object[]> batchData = new ArrayList<>();
			
			for (int i=0;i<cast_details.length();i++) {
				JSONObject obj = cast_details.getJSONObject(i);
				castIds+=obj.getInt("id")+",";
				String cast_id=Database.getRecord("select cast_id from cast_details where cast_id='"+obj.getInt("id")+"';");
				if(cast_id==null) {
					batchData.add(new Object[]{
							obj.getInt("id"),
							obj.getString("name"),
							obj.optString("profile_path", ""),
							obj.getString("known_for_department"),
					});
				}
			}

			for (Object[] rowData : batchData) {
				for (int i = 0; i < rowData.length; i++) {
					preparedStatement.setObject(i + 1, rowData[i]);
				}
				preparedStatement.addBatch();
			}
			int[] updateCounts = preparedStatement.executeBatch();

			for (int i=0;i<updateCounts.length;i++) {
				if (updateCounts[i] == PreparedStatement.EXECUTE_FAILED) {
					status = "error";
					i=updateCounts.length;
				}
			}
		}
		catch(Exception e)
		{
			status="error";
			e.printStackTrace();
		}
		result.put("status",status);
		result.put("message",message);
		result.put("castIds",castIds);
		return result;
	}
	
	public static synchronized JSONObject blockTickets(int show_id,String diamond[],String platinum[],String gold[],String silver[])throws Exception
	{
		JSONObject result=new JSONObject();
		String status="error",message="Error occurred while confirm seating details!..",blocking_id=null;
		try
		{				   
			// if(!ViewHandler.isDuplicateTickets(show_id,diamond,platinum,gold,silver))
			{
				Connection con=Database.getConnection();
				PreparedStatement pstmt=con.prepareStatement("insert into blocked_seats(diamond_seats,platinum_seats,gold_seats,silver_seats,show_id, blocked_time,status) VALUES (?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
				pstmt.setObject(1,diamond);
				pstmt.setObject(2,platinum);
				pstmt.setObject(3,gold);
				pstmt.setObject(4,silver);
				pstmt.setInt(5,show_id);
				pstmt.setObject(6,new Timestamp(new Date().getTime()));
				pstmt.setString(7,"blocked");
				pstmt.executeUpdate();
				ResultSet rs = pstmt.getGeneratedKeys();
				if (rs.next()) {
						blocking_id=rs.getString("blocking_id");
					}
					
				result.put("blocking_id",blocking_id);
				status="success";
				message="Seats Blocked Successfully!..";
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
	
	public static String bookTickets(String booking_id,String user_id,String block_id,String show_id,double price,String encodedQr)throws Exception
	{
		String status="error";
		try
		{
			if(ViewHandler.isValidBlockId(block_id))
			{
				Connection con=Database.getConnection();
				PreparedStatement pstmt = con.prepareStatement("Insert into booking_history(booking_id,block_id,show_id,user_id,price,encodedQr) values(?,?,?,?,?,?)");
				pstmt.setString(1,booking_id);
				pstmt.setInt(2,Integer.parseInt(block_id));
				pstmt.setInt(3,Integer.parseInt(show_id));
				pstmt.setString(4,user_id);
				pstmt.setObject(5,price);
				pstmt.setString(6,encodedQr);
				pstmt.executeUpdate();
				
				updateBlockingStatus(block_id);
				status="success";
			}
			else
			{
				status="timeOut";
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return status;
	}
	
	public static void updateBlockingStatus(String blocking_id)throws Exception
	{
		try
		{
			Connection con=Database.getConnection();
			PreparedStatement stmt=con.prepareStatement("update blocked_seats set status='booked' where blocking_id= '"+blocking_id+"';");
			stmt.executeUpdate();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static JSONObject toggleShowStatus(String history_id,String toggle_status)throws Exception
	{
		JSONObject result=new JSONObject();
		String status="error",message="Error Occurred while toggle Show status!! Please try again after some time...";
		
		try
		{
			Connection con=Database.getConnection();
			PreparedStatement stmt=con.prepareStatement("update show_history set status=? where show_history_id = ?;");
			stmt.setString(1,toggle_status);
			stmt.setInt(2,Integer.parseInt(history_id));
			stmt.executeUpdate();
			
			status="success";
			message="Show toggled Successfully!!";
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		result.put("status",status);
		result.put("message",message);
		return result;
	}
	
	
	public static JSONObject deleteShowHistory(String history_id)throws Exception
	{
		JSONObject result=new JSONObject();
		String message="Error occurred while removing show_details!! Please try again after sometime",status="error";
		
		try
		{
			Connection con=Database.getConnection();
			PreparedStatement pstmt=con.prepareStatement("delete from show_history where show_history_id = ?;");
			pstmt.setInt(1,Integer.parseInt(history_id));
			pstmt.executeUpdate();
			
			status="success";
			message="Details removed successfully!!";
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		result.put("status",status);
		result.put("message",message);
		return result;
	}
	
}