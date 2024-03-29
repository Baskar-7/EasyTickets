package utils;

import java.io.*;
import java.util.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.sql.Array;

import org.json.JSONObject;
import org.json.JSONArray;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.Part;

public class Util
{
	public static String getStringProperty(Properties props, String key) {
		Object value = props.getProperty(key);
		return (value != null) ? value.toString() : null;
	}
	
	 public static String[] jsonArrayToStringArray(JSONArray jsonArray) throws Exception {
        int length = jsonArray.length();
        String[] stringArray = new String[length];

        for (int i = 0; i < length; i++) {
            stringArray[i] = jsonArray.getString(i);
        }

        return stringArray;
    }
	
	public static JSONArray arrayToJsonarray(Array array)throws Exception  {
		 String[] arrayData = (String[]) array.getArray();
		 return new JSONArray(arrayData);
	}
	
	public static Date parseToDate(String time,String datestr,String format)throws Exception
	{
		Date date=null;
		if(datestr!=null && !datestr.equals(""))
		{
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			if(time!=null && !time.equals(""))
			{
				date = sdf.parse(datestr + " " + time);
			}
			else
			{
				date = sdf.parse(datestr);
			}
		}
		return date;
	}
	
	public static String formatDate(String date)
    {
        int d=Integer.parseInt(date)%10;
        switch(d)  
        {
            case 1:
                return date+"ST";
            case 2:
                return date+"ND";
            case 3:
                return date+"RD";
            default:
                return date+"TH";
        }
    }
	
	 public static BufferedImage generateQRCode(String content) {
        int width = 300;
        int height = 300;

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.setColor(Color.BLACK);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (bitMatrix.get(x, y)) {
                        graphics.fillRect(x, y, 1, 1);
                    }
                }
            }
            return image;

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
	
	public static String formatCurrency(double currency)
	{		
		int rupee =(int) Math.floor(currency);
		int paise =(int)Math.ceil((currency * 100) % 100);
		System.out.println( String.valueOf(paise).length());
		if( String.valueOf(paise).length()==1)
		{
		    return "Rs."+rupee+".0"+paise;
		}
        return "Rs."+rupee+"."+paise;
	}
	
	public static String parseToString(Date date,String format)throws Exception
	{
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}
	
	public static String getAsciiSubstring(String input, int length) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length && i < input.length(); i++) {
            result.append((int) input.charAt(i));
        }
        return result.toString();
    }
	
	public static String convertBase64(Part imagePart)throws Exception
	{
		InputStream inputStream = imagePart.getInputStream();
		 ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        byte[] imageBytes = outputStream.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
	}
	
	public static JSONObject checkApacheDirectory()throws Exception
	{
		JSONObject result=new JSONObject();
		String status="error",message="Apache Software Foundation Tomcat 9.0 installation details not found in environment variables.";
		try
		{
			String softwareHome = System.getenv("APACHE_SOFTWARE_FOUNDATION_HOME");
			String tomcatVersion = "9.0";
			String possiblePath="";

			if (softwareHome != null) {
				possiblePath = softwareHome + File.separator + "Tomcat " + tomcatVersion + File.separator + "temp";
				File file = new File(possiblePath);
				if(createDirectoryIfNotExist(file))
					status="success";
					message="Directory found successfully!!..";
			} else {
				String programFiles = System.getenv("ProgramFiles");
				if (programFiles != null) {
					possiblePath="";
					possiblePath = programFiles + File.separator + "Apache Software Foundation" + File.separator + "Tomcat " + tomcatVersion + File.separator + "temp";
					File file = new File(possiblePath);
					if(createDirectoryIfNotExist(file))
						status="success";
						message="Directory found successfully!!..";
				} 
			}	
			result.put("path",possiblePath);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		result.put("status",status);
		result.put("message",message);
		
		return result;
	}
	
	 private static boolean createDirectoryIfNotExist(File file) {
        if (!file.exists() && !file.isDirectory()) {
            if (!file.mkdirs()) {
                return false;
            }
        } 
		
		return true;
    }
	
	
	public static ArrayList getAllLanguages()
	{
		 ArrayList<String> languageList = new ArrayList<>(Arrays.asList(
                "English", "Spanish", "French", "German", "Italian", "Portuguese", "Russian",
                "Chinese", "Japanese", "Korean", "Arabic", "Hindi", "Bengali", "Urdu", "Tamil",
                "Telugu", "Gujarati", "Kannada", "Malayalam", "Punjabi", "Persian", "Turkish",
                "Thai", "Vietnamese", "Dutch", "Swedish", "Danish", "Norwegian", "Finnish", "Greek",
                "Polish", "Hungarian", "Czech", "Slovak", "Romanian", "Ukrainian", "Hebrew",
                "Indonesian", "Malay", "Filipino", "Swahili", "Afrikaans", "Icelandic", "Estonian",
                "Latvian", "Lithuanian", "Croatian", "Serbian", "Slovenian", "Bulgarian",
                "Macedonian", "Albanian", "Armenian", "Georgian", "Uzbek", "Kazakh", "Kyrgyz",
                "Tajik", "Turkmen", "Mongolian", "Welsh", "Irish", "Scottish Gaelic", "Maori"
        ));
		return languageList;
	}
	
	public static ArrayList getGenres()
	{
		ArrayList <String> genresList =new ArrayList<>(Arrays.asList(
			"Action", "Adventure", "Animation", "Comedy", "Crime", "Drama", "Family", "Fantasy", "History", 
            "Horror", "Music", "Mystery", "Romance", "Sci-fi", "Thriller", "War", "Western", 
            "Documentary", "Biography", "Sport", "Musical", "Film Noir", "Supernatural", "Disaster", "Mockumentary"
		));
		return genresList;
	}


}