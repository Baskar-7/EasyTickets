
package initialize;

import java.io.*;
import java.sql.*;

import utils.Database;

public class Initializer
{
	public static void main(String[] args)throws Exception
	{
		try
		{
			Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/","postgres", "12345");            
            Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = 'easytickets'");
			
			if (!rs.next()) {
				stmt.executeUpdate("CREATE DATABASE easytickets");
	
				DataDictionaryParser.parseDataDictionary();
				
				Connection con = Database.getConnection();
				PreparedStatement st = con.prepareStatement("INSERT INTO accounts (mail,userId,fname,acc_type,mobile,pincode,city,state) VALUES (?,?,?,?,?,?,?,?)");
				st.setString(1,"baskar97917@gmail.com");
				st.setString(2,"Baskar_@7");
				st.setString(3,"Baskar");
				st.setString(4,"Admin");
				st.setString(5,"");
				st.setString(6,"");
				st.setString(7,"");
				st.setString(8,"");
				st.executeUpdate();
				
				st.close();
				ServletApiParser.parseServletApi();
			}
			rs.close();
			stmt.close();
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}		
	}


}