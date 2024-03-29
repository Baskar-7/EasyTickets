
package servletApi;

import utils.Database;

import java.lang.reflect.Method;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

public class ServletAPIController {

	volatile private static ServletAPIController instance = null;
	private Hashtable<String, Hashtable<String, Object>> apiMapping = new Hashtable<String, Hashtable<String, Object>>();
	private final static String CLASS_INSTANCE = "CLASS_INSTANCE";
	private final static String METHOD_INSTANCE = "METHOD_INSTANCE";
	private final static String GET_INSTANCE_METHOD = "getInstance";
	
	public static ServletAPIController getInstance() {
		if (instance == null) {
			synchronized (ServletAPIController.class) {
				if (instance == null) {
					instance = new ServletAPIController();
				}
			}
		}
		return instance;
	}

	private ServletAPIController() {
		try {
			initializeAPIMap();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void initializeAPIMap() {
		try 
		{
			Connection con=Database.getConnection();
			PreparedStatement stmt=null;
			
			stmt=con.prepareStatement("select * from servlet_apimapping");
			ResultSet rs=stmt.executeQuery();
			
			while(rs.next())
			{
				String urlPath = rs.getString("url_path");
				String className = rs.getString("class_name");
				String methodName = rs.getString("method_name");
				String apiType=rs.getString("api_type");
				
				Class<?> caller = Class.forName(className);
				Class<?>[] argTypes = new Class<?>[] {};
				Method getInstanceMethod = caller.getMethod(GET_INSTANCE_METHOD, argTypes);
				Object obj = getInstanceMethod.invoke(caller);

				argTypes = new Class<?>[] { HttpServletRequest.class, HttpServletResponse.class };
				Method executionMethod = caller.getMethod(methodName, argTypes);

				Hashtable<String, Object> mappingInfo = new Hashtable<String, Object>();
				mappingInfo.put(CLASS_INSTANCE, obj);
				mappingInfo.put(METHOD_INSTANCE, executionMethod);
				mappingInfo.put("api_type", apiType);
				apiMapping.put(urlPath, mappingInfo);
			}
			rs.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public int execute(HttpServletRequest request, HttpServletResponse response) {
		
		String urlPath = (request.getRequestURI()).replaceFirst("/EasyTickets/", "");
		int errCode = 200;
		HttpSession session = request.getSession(false);
		
		try {
			Hashtable<String, Object> mappingInfo = apiMapping.get(urlPath);
			//if(mappingInfo != null && (request.getRequestedSessionId() != null && !request.isRequestedSessionIdValid() && validateCSRFToken(session,(String)request.getParameter("csrfToken"))) || (mappingInfo.get("api_type")).equals("OpenSource"))
			if(mappingInfo != null)
			{
				Object obj = mappingInfo.get(CLASS_INSTANCE);				
				Method executionMethod = (Method) mappingInfo.get(METHOD_INSTANCE);
				executionMethod.invoke(obj, request, response);
			} else {
				errCode = 404;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return errCode;
	}

	public static boolean validateCSRFToken(HttpSession session,String userToken)
	{
         String sessionToken = (String) session.getAttribute("csrfToken");
		 try {
            Claims claims = Jwts.parser()
                    .setSigningKey((String) session.getAttribute("csrf_key"))
                    .parseClaimsJws(userToken)
                    .getBody();
					
             return userToken.equals(sessionToken) && !claims.getExpiration().before(new Date());
        } catch (Exception e) {
			e.printStackTrace();
            return false;
        }		
	}
}

