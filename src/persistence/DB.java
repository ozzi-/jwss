package persistence;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;

import org.apache.commons.dbcp.BasicDataSource;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import com.mysql.cj.jdbc.exceptions.MysqlDataTruncation;

import helpers.Config;
import helpers.Log;
import pojo.RS;
import pojo.Val;
import pojo.Vals;

public class DB {

	static final String JDBC_DRIVER = Config.getDbDriver();
	static final String JDBC_DB_URL = Config.getDbURL();	
	static final String JDBC_USER = Config.getDbUser();
	static final String JDBC_PASS = Config.getDbPW();
	static final int JDBC_POOL_SIZE = Config.getDbPoolSize();
	private static BasicDataSource dataSource;

	private static BasicDataSource getDataSource() {
		if (dataSource == null) {
			BasicDataSource ds = new BasicDataSource();
			ds.setDriverClassName(JDBC_DRIVER);
			ds.setUrl(JDBC_DB_URL);
			ds.setUsername(JDBC_USER);
			ds.setPassword(JDBC_PASS);
			ds.setValidationQuery("SELECT 1");
			ds.setTestOnBorrow(true);
			ds.setMaxActive(JDBC_POOL_SIZE);
			ds.setMaxOpenPreparedStatements(100);
			dataSource = ds;
		}
		return dataSource;
	}
	
	public static void testDB() throws Exception {
		try {
			RS testDB = doSelect("use "+Config.getDbName(), new Vals());
			testDB.close();
		} catch (SQLException e) {
			throw new Exception("Database error:"+e.getMessage()+" - cause:"+e.getCause());
		}
	}

	public static int getRowCount(ResultSet resultSet) {
		if (resultSet == null) {
			return 0;
		}
		try {
			resultSet.last();
			return resultSet.getRow();
		} catch (SQLException exp) {
			exp.printStackTrace();
		} finally {
			try {
				resultSet.beforeFirst();
			} catch (SQLException exp) {
				exp.printStackTrace();
			}
		}
		return 0;
	}

	public static RS doSelect(String query, Vals vals) throws SQLException {
		Connection con = getDataSource().getConnection();
		PreparedStatement stmt = con.prepareStatement(query);
		bindStatementParams(vals.getVals(), stmt);
		ResultSet rs = stmt.executeQuery();
		
		return new RS(rs, con, query);
	}

	public static void exit() {
		try {
			getDataSource().close();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		
        Enumeration<java.sql.Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException e) {
            	e.printStackTrace();
            }
        }
        AbandonedConnectionCleanupThread.checkedShutdown();
	}
	
	public static int doInsert(String query, Vals vals) throws SQLException {
		Connection con = getDataSource().getConnection();
		int candidateId = 0;
		PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
		bindStatementParams(vals.getVals(), stmt);
		int rowAffected = 0;
		try {
			rowAffected = stmt.executeUpdate();
		}catch (MysqlDataTruncation e) {
			stmt.close();
			con.close();
			throw new SQLException("Incorrect value received");
		}
		if (rowAffected == 1) {
			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				candidateId = rs.getInt(1);				
			}
		}
		stmt.close();
		con.close();
		return candidateId;
	}

	public static int doUpdate(String query, Vals vals) throws SQLException {
		Connection con = getDataSource().getConnection();
		PreparedStatement stmt = con.prepareStatement(query);
		bindStatementParams(vals.getVals(), stmt);
		int i = stmt.executeUpdate();
		stmt.close();
		con.close();
		return i;
	}
	
	public static void checkIfExistsWithSpecificID(Vals vals , String tableName, String field, String idField) throws Exception {
		RS rs = DB.doSelect("SELECT count(*) FROM "+tableName+" WHERE " + field + " = ? AND "+idField+" = ?", vals);
		
		int numberRow = 0;
		while (rs.getRs().next()) {
			numberRow = rs.getRs().getInt("count(*)");
		}
		rs.close();
		if (numberRow > 0) {
			throw new Exception(field + " with the value '" + vals.getVals().get(0).getValue() + "' exists already for this element");
		}
	}
	
	
	public static int getCount(String table) throws SQLException {
		RS rs = DB.doSelect("SELECT COUNT(*) FROM " + table + ";", new Vals());
		int count = 0;
		if (rs.getRs().next()) {
			count = rs.getRs().getInt("COUNT(*)");
		}
		rs.close();
		return count;
	}
	
	public static void dumpDatabase() {
		String basePath = "";
		try {
			basePath = Config.getBasePath();
		} catch (Exception e) {
			Log.logException(e, DB.class);
			return;
		}
		String backupFolderPath = basePath+"backup";
		try {
			new File(backupFolderPath).mkdirs();
		}catch (Exception e) {
			Log.logException(new Exception("Could not create backup folder '"+backupFolderPath+"'. Due to "+e.getMessage()), DB.class);
			return;
		}
		String backupFileName = new SimpleDateFormat("yyyyMMddHHmm'_backup.sql'").format(new Date());
        File backupFile = new File(backupFolderPath+File.separator+backupFileName);
        
        String[] command = new String[]{Config.getDbBackupDumpBinary(), "-u"+JDBC_USER, "-p"+JDBC_PASS, Config.getDbName()};
        ProcessBuilder processBuilder = new ProcessBuilder(Arrays.asList(command));
        processBuilder.redirectError(Redirect.INHERIT);
        processBuilder.redirectOutput(Redirect.to(backupFile));

        Process process;
		try {
			process = processBuilder.start();
			process.waitFor();
		} catch (Exception e) {
			Log.logException(new Exception("Could not backup database '"+backupFileName+"'. Due to "+e.getMessage()), DB.class);
		}
		Log.logInfo("Backed up database "+backupFileName, DB.class);
		
		int deletedBackupCount = 0;
		File dir = new File(backupFolderPath);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File file : directoryListing) {
				if(file.getName().endsWith(".sql")) {
					long diff = new Date().getTime() - file.lastModified();
					if (diff > Config.getDbBackupKeepForXDays() * 24 * 60 * 60 * 1000) {
						file.delete();
						deletedBackupCount++;
					}					
				}
			}
		}
		if(deletedBackupCount!=0) {
			Log.logInfo("Deleted "+deletedBackupCount+" old database backups as they are older than "+Config.getDbBackupKeepForXDays()+" days", DB.class);
		}
	}
	
	private static void bindStatementParams(ArrayList<Val> values, PreparedStatement stmt) throws SQLException {
		int i = 0;
		if (values != null) {
			for (Val val : values) {
				if (val.isString()) {
					stmt.setString(++i, org.apache.commons.text.StringEscapeUtils.escapeJson(val.getValue()));
				} else {
					stmt.setInt(++i, Integer.valueOf(val.getValue()));
				}
			}
		}
	}
}