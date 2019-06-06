package org;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.apache.log4j.*;

import facebook4j.Facebook;
import facebook4j.FacebookFactory;
import facebook4j.Post;
import facebook4j.auth.AccessToken;
import facebook4j.internal.org.json.JSONObject;

public class Utils {
	private static final Logger logger = LogManager.getLogger(Utils.class);	
	public static Properties loadConfigFile(String folderName, String fileName) throws IOException {
		Properties props = new Properties();
		Path configFolder = Paths.get(folderName);
		Path configFile = Paths.get(folderName, fileName);
		if (!Files.exists(configFile)) {
			logger.info("Creando nuevo archivo de configuración.");
			
			if (!Files.exists(configFolder))
				Files.createDirectory(configFolder);
			
			Files.copy(Utils.class.getResourceAsStream("fbcmd4j.properties"), configFile);
		}

		props.load(Files.newInputStream(configFile));
		BiConsumer<Object, Object> emptyProperty = (k, v) -> {
			if(((String)v).isEmpty())
				logger.info("La propiedad '" + k + "' está vacía");
		};
		props.forEach(emptyProperty);

		return props;
	}
	
	public static Facebook confFb(Properties props) {
		Facebook fb = new FacebookFactory().getInstance();
		fb.setOAuthAppId(props.getProperty("oauth.appId"), props.getProperty("oauth.appSecret"));
		fb.setOAuthPermissions(props.getProperty("oauth.permissions"));
		if(props.getProperty("oauth.accessToken") != null)
			fb.setOAuthAccessToken(new AccessToken(props.getProperty("oauth.accessToken"), null));
		
		return fb;
	}
	
	public static void confTokens(String folderName, String fileName, Properties props, Scanner scanner) {
		if (props.getProperty("oauth.appId").isEmpty() || props.getProperty("oauth.appSecret").isEmpty()) {
			System.out.println("Por favor ingrese appId:");
			props.setProperty("oauth.appId", scanner.nextLine());
			System.out.println("Por favor ingrese appSecret:");
			props.setProperty("oauth.appSecret", scanner.nextLine());
		}

		try {
			URL url = new URL("https://graph.facebook.com/v2.6/device/login");
	        Map<String,Object> params = new LinkedHashMap<>();
	        params.put("access_token", "falta");
	        params.put("scope", props.getProperty("oauth.permissions"));

	        StringBuilder postData = new StringBuilder();
	        for (Map.Entry<String,Object> param : params.entrySet()) {
	            if (postData.length() != 0) postData.append('&');
	            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
	            postData.append('=');
	            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
	        }
	        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

	        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
	        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
	        conn.setDoOutput(true);
	        conn.getOutputStream().write(postDataBytes);

	        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	        StringBuilder sb = new StringBuilder();
	        for (int c; (c = in.read()) >= 0;)
	            sb.append((char)c);
	        String response = sb.toString();
	        
	        JSONObject obj = new JSONObject(response);
	        String code = obj.getString("code");
	        String userCode = obj.getString("user_code");
	        
			System.out.println("Ingresa a la página https://www.facebook.com/device con el código: " + userCode);

			String accessToken = "";
			while(accessToken.isEmpty()) {
		        try {
		            TimeUnit.SECONDS.sleep(5);
		        } catch (InterruptedException e) {
					logger.error(e);
		        }

		        URL url1 = new URL("https://graph.facebook.com/v2.6/device/login_status");
		        params = new LinkedHashMap<>();
		        params.put("access_token", "falta");
		        params.put("code", code);
	
		        postData = new StringBuilder();
		        for (Map.Entry<String,Object> param : params.entrySet()) {
		            if (postData.length() != 0) postData.append('&');
		            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
		            postData.append('=');
		            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
		        }
		        postDataBytes = postData.toString().getBytes("UTF-8");
	
		        HttpURLConnection conn1 = (HttpURLConnection)url1.openConnection();
		        conn1.setRequestMethod("POST");
		        conn1.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		        conn1.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		        conn1.setDoOutput(true);
		        conn1.getOutputStream().write(postDataBytes);

		        try {
		        	in = new BufferedReader(new InputStreamReader(conn1.getInputStream(), "UTF-8"));
			        sb = new StringBuilder();
			        for (int c; (c = in.read()) >= 0;)
			            sb.append((char)c);		        
			        response = sb.toString();
			        
			        obj = new JSONObject(response);
			        accessToken = obj.getString("access_token");
		        } catch(IOException ignore) {
		        }
		    }
			
	        props.setProperty("oauth.accessToken", accessToken);   
			Path configFile = Paths.get(folderName, fileName);
			props.store(Files.newOutputStream(configFile), "Generado por org.fbcmd4j.configTokens");
			System.out.println("Configuración guardada exitosamente.");
			logger.info("Configuración guardada exitosamente.");
		} catch(Exception e) {
			logger.error(e);
		}
	}
	
	public static void printPosts(Post p) {
		if (p.getStory() != null)
			System.out.println("Story: " + p.getStory());
		if (p.getMessage() != null)
			System.out.println("Mensaje: " + p.getMessage());
	}

}	
