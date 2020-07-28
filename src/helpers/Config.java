package helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.JSONObject;

import pojo.MailAccount;

public class Config {
	
	public static String appName = "jwss";

	private static MailAccount mailAccount;
	private static String baseURL;
	private static String dbDriver;
	private static String dbURL;
	private static String dbUser;
	private static String dbPW;
	private static int dbPoolSize;
	private static int dbBackupIntervalInMinutes;
	private static int dbBackupKeepForXDays;
	private static String dbBackupDumpBinary;
	private static String dbName;
	private static String uidHeaderName;
	
	public static String getBasePath() throws Exception {
		String basePath = "";
		String osName = System.getProperty("os.name").toLowerCase();
		if(osName.contains("windows")) {
			basePath = System.getenv("PROGRAMDATA")+File.separator+appName+File.separator;
		}else if(osName.contains("mac os")) {
			basePath = "/var/lib/"+appName+"/";
		}else {
			basePath = "/opt/"+appName+"/";
		}
		createFolderPath(basePath);
		return basePath;
	}
	
	public static void createFolderPath(String pathString) throws Exception {
		Path path = Paths.get(pathString);
		try {
			Files.createDirectories(path);
		} catch (IOException e) {
			throw new Exception("Could not greate path '"+pathString+"' - due to: "+e.getMessage());
		}
	}
	
	public static void loadAppConfig() {
		String json = "";
		try {
			json = Helpers.readFile(getBasePath()+"app.json");
		} catch (Exception e) {
			Log.logException(e, Config.class);
			System.err.println("Cannot load app config json file!");
			System.err.println(e.toString());
			System.exit(1);
		}
		String curItem = "null";
		try {
			JSONObject configJSON = new JSONObject(json);
			baseURL = configJSON.getString("baseURL");
			dbDriver = configJSON.getString("dbDriver");
			dbURL = configJSON.getString("dbURL");
			dbUser = configJSON.getString("dbUser");
			dbPW = configJSON.getString("dbPW");
			dbPoolSize = configJSON.getInt("dbPoolSize");
			dbBackupIntervalInMinutes = configJSON.getInt("dbBackupIntervalInMinutes");
			dbBackupDumpBinary = configJSON.getString("dbBackupDumpBinary");
			dbBackupKeepForXDays = configJSON.getInt("dbBackupKeepForXDays");
			dbName = configJSON.getString("dbName");
			uidHeaderName = configJSON.getString("uidHeaderName");			
		} catch (Exception e) {
			System.err.println("Error parsing app config json file at object \""+curItem+"\"");
			System.err.println(e.toString());
			System.exit(2);
		}
	}
	
	public static void loadMailConfig() {
		String json = "";
		try {
			json = Helpers.readFile(getBasePath()+"mail.json");
		} catch (Exception e) {
			Log.logException(e, MailAccount.class);
			System.err.println("Cannot load mail config json file!");
			System.err.println(e.toString());
			System.exit(1);
		}
		String curItem = "null";
		try {
			JSONObject mailAccountJSON = new JSONObject(json);
			String address = mailAccountJSON.getString("address");
			String login = mailAccountJSON.getString("login");
			String pw = mailAccountJSON.getString("pw");
			String inboxFolderName = mailAccountJSON.getString("inbox_folder_name");
			String hostSmtp;
			String hostImap;
			if(exists(mailAccountJSON, "host")) {
				String host = mailAccountJSON.getString("host");
				hostSmtp = host;
				hostImap = host;
			}else {
				hostSmtp = mailAccountJSON.getString("host_smtp");
				hostImap = mailAccountJSON.getString("host_imap");
			}
			int portSmtp = mailAccountJSON.getInt("port_smtp");
			int portImap = mailAccountJSON.getInt("port_imap");
			boolean secureSmtp;
			boolean secureImap;
			if(exists(mailAccountJSON, "secure")) {
				boolean secure = mailAccountJSON.getBoolean("secure");
				secureSmtp = secure;
				secureImap = secure;
			}else {
				secureSmtp = mailAccountJSON.getBoolean("secure_smtp");
				secureImap = mailAccountJSON.getBoolean("secure_imap");						
			}

			boolean trustAllCerts = exists(mailAccountJSON, "trust_all_certs")?mailAccountJSON.getBoolean("trust_all_certs"):false;
			mailAccount = new MailAccount("mailer", address, login, pw, inboxFolderName, hostSmtp, portSmtp, secureSmtp, hostImap, portImap, secureImap, trustAllCerts);
		} catch (Exception e) {
			System.err.println("Error parsing mail config json file at object \""+curItem+"\"");
			System.err.println(e.toString());
			System.exit(2);
		}
	}

	private static boolean exists(JSONObject obj, String key) {
		String[] items = JSONObject.getNames(obj);
		for (String item : items) {
			if(item.equals(key)) {
				return true;
			}
		}
		return false;
	}

	public static MailAccount getMailAccount() {
		return mailAccount;
	}

	public static String getBaseURL() {
		return baseURL;
	}

	public static String getDbDriver() {
		return dbDriver;
	}

	public static String getDbURL() {
		return dbURL;
	}

	public static String getDbUser() {
		return dbUser;
	}

	public static String getDbPW() {
		return dbPW;
	}

	public static int getDbPoolSize() {
		return dbPoolSize;
	}

	public static String getDbName() {
		return dbName;
	}

	public static String getUidHeaderName() {
		return uidHeaderName;
	}

	public static void setUidHeaderName(String uidHeaderName) {
		Config.uidHeaderName = uidHeaderName;
	}

	public static int getDbBackupIntervalInMinutes() {
		return dbBackupIntervalInMinutes;
	}

	public static void setDbBackupIntervalInMinutes(int dbBackupIntervalInMinutes) {
		Config.dbBackupIntervalInMinutes = dbBackupIntervalInMinutes;
	}

	public static String getDbBackupDumpBinary() {
		return dbBackupDumpBinary;
	}

	public static void setDbBackupDumpBinary(String dbBackupDumpBinary) {
		Config.dbBackupDumpBinary = dbBackupDumpBinary;
	}

	public static int getDbBackupKeepForXDays() {
		return dbBackupKeepForXDays;
	}

	public static void setDbBackupKeepForXDays(int dbBackupKeepForXDays) {
		Config.dbBackupKeepForXDays = dbBackupKeepForXDays;
	}
}
