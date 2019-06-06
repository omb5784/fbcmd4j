package org;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Properties;
import java.util.Scanner;

import org.apache.log4j.*;
import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.Post;
import facebook4j.ResponseList;

public class Main {
	static final Logger logger = LogManager.getLogger(Main.class);
	// directorio llamado config para guardar las configuraciones
	private static final String CONFIG_DIR = "config";
	private static final String CONFIG_FILE = "fbcmd4j.properties";
	private static final String APP_VERSION = "v1.0";
	
	public static void main(String[] args) {
		Facebook fb =  null;
		Properties props = null;
		try {
			props = Utils.loadConfigFile(CONFIG_DIR, CONFIG_FILE);
			} catch (IOException ex) 
		      {
				logger.error(ex);
		      }
		int seleccion = 1;		
		try (Scanner scanner = new Scanner(System.in)){
			while(true) {
				// Inicio Menu
				System.out.format("Simple Facebook client %s\n\n", APP_VERSION);
				System.out.println("Opciones: ");
				System.out.println("(0) Cargar configuracion");
				System.out.println("(1) NewsFeed");
				System.out.println("(2) Wall");
				System.out.println("(3) Publicar Estado");
				System.out.println("(4) Publicar Link");
				System.out.println("(5) Salir");
				System.out.println("\nPor favor ingrese una opción: ");
				// Fin de Menu
				fb = Utils.confFb(props);
				try {
					seleccion = scanner.nextInt();
					scanner.nextLine();
					switch (seleccion) {
					case 0:
						Utils.confTokens(CONFIG_DIR, CONFIG_FILE, props, scanner);
						props = Utils.loadConfigFile(CONFIG_DIR, CONFIG_FILE);
						break;
					case 1:
						System.out.println("NewsFeed:");
						ResponseList<Post> NewsFeed = fb.getFeed();
						for (Post news : NewsFeed) {
							Utils.printPosts(news);
						}
//						SaveFile("NewsFeed", newsFeed, scan);
						break;
					case 2:
						System.out.println("Wall:");
						ResponseList<Post> wall = fb.getPosts();
						for (Post Wall : wall) {
							Utils.printPosts(Wall);
						}	
						break;
					case 3:

						break;
					case 4:

						break;
					case 5:

						break;
					default:
						break;
					}
				} catch (InputMismatchException ex)
				        {
							System.out.println("Ocurrió un errror, consulte el log.");
							logger.error("La opción no es válida. %s. \n", ex.getClass());
							scanner.next();
				        } catch (FacebookException ex)
								{
				        			System.out.println("Ocurrió un errror, consulte el log.");
				        			logger.error(ex.getErrorMessage());
								} catch (Exception ex)
									{
										System.out.println("Ocurrió un errror, consulte el log.");
										logger.error(ex);
									}
				System.out.println();			
			}
		} catch (Exception e) 
			{
				logger.error(e);
			}
	}
}
