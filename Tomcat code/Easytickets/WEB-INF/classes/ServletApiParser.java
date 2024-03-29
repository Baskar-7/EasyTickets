
package initialize;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import utils.Database;

public class ServletApiParser {

    public static void parseServletApi() 
	{
        try {
			File xmlFile = new File("C:\\Program Files\\Apache Software Foundation\\Tomcat 9.0\\webapps\\EasyTickets\\WEB-INF\\classes\\initialize\\servlet-api.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document document = dBuilder.parse(xmlFile);
			document.getDocumentElement().normalize();
		
            Connection connection = Database.getConnection();
            
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO servlet_apimapping (url_path, class_name, method_name,api_type) VALUES ( ?, ?, ?, ?)");

            NodeList nodeList = document.getElementsByTagName("ServletAPIMapping");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                
                String urlPath = element.getAttribute("URL_PATH");
                String className = element.getAttribute("CLASS_NAME");
                String methodName = element.getAttribute("METHOD_NAME");
                String apiType = element.getAttribute("API_TYPE");
                
                preparedStatement.setString(1, urlPath);
                preparedStatement.setString(2, className);
                preparedStatement.setString(3, methodName);
                preparedStatement.setString(4, apiType);
                
                preparedStatement.executeUpdate();
            }
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
