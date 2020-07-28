package service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import helpers.Config;
import persistence.DB;
import pojo.RS;

public class Scheduler   {
	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	public static void exit() {
		scheduler.shutdown();
	}

	public static void schedule() {
		Runnable task = () -> {
			// Example Scheduler Task
		};
		scheduler.scheduleWithFixedDelay(task, 0, 120, TimeUnit.SECONDS);
		
		Runnable cleanUpRS = () -> {
			RS.cleanUpRS();
		};
		scheduler.scheduleWithFixedDelay(cleanUpRS, 0, 5, TimeUnit.SECONDS);
		
		Runnable db = () -> {
			DB.dumpDatabase();
		};
		scheduler.scheduleWithFixedDelay(db, 0, Config.getDbBackupIntervalInMinutes(), TimeUnit.MINUTES);
	}
	
}

