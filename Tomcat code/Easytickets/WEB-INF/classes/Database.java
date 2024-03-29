
package utils;

import java.sql.*;
import java.util.*;
import java.io.*;

public class Database
{
	private static Connection connection;
    
    private Database() {
    }
    
    public static Connection getConnection() 
	{
        if (connection == null) 
		{
            synchronized (Database.class) 
			{
                if (connection == null) 
				{
                    try {
                        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/easytickets","postgres","12345");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return connection;
    }
	
	public static boolean checkTable(String tableName)
	{
		boolean isTableExists=false;
		try {
			Connection connection =getConnection();
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet resultSet = metaData.getTables(null, null, tableName, new String[] { "TABLE" });

            if (resultSet.next()) {
				isTableExists = true;
			}
        } catch (SQLException e) {
            e.printStackTrace();
        }
		return isTableExists;
	}
	
	public static String getRecord(String query)
	{
		String record=null;
		try{
			Connection con=getConnection();
			PreparedStatement st=con.prepareStatement(query);
			ResultSet rs=st.executeQuery();
			if(rs.next())
			{
				record=rs.getString(1);
			}
			rs.close();
			st.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return record;
	}
}