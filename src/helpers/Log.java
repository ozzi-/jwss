package helpers;

import java.util.logging.Level;
import java.util.logging.Logger;


public class Log {
	public static <T> void logInfo(String msg, String userName, Object classObj) {
		Logger.getLogger(classObj.getClass().getName()).log(Level.INFO, Config.appName+" - "+userName+" - "+msg);		
	}
	
	public static <T> void logInfo(String msg, Object classObj) {
		Logger.getLogger(classObj.getClass().getName()).log(Level.INFO, Config.appName+" - "+msg);		
	}
	
	public static void logWarning(String msg, Object classObj) {
		Logger.getLogger(classObj.getClass().getName()).log(Level.WARNING, Config.appName+" - "+msg);		
	}
	
	public static void logWarning(String msg, String userName, Object classObj) {
		Logger.getLogger(classObj.getClass().getName()).log(Level.WARNING, Config.appName+" - "+userName+" - "+msg);		
	}
	
	public static void logException(Exception e, Object classObj) {
		Logger.getLogger(classObj.getClass().getName()).log(Level.WARNING, Config.appName+" - "+e.getMessage(), e);	
	}
}
