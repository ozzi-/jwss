package service;

import java.util.Date;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import helpers.Log;
import pojo.LongRunningTaskResult;

public class LongRunningTaskRegistry {
	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    static ConcurrentHashMap<String, LongRunningTaskResult> longRunningTaskRegistryMap = new ConcurrentHashMap<>();
    
    public static LongRunningTaskResult getTaskResult(String longRunningTaskID) {
    	return longRunningTaskRegistryMap.get(longRunningTaskID);
    }
    
    public static void removeTask(String longRunningTaskID) {
    	longRunningTaskRegistryMap.remove(longRunningTaskID);
    	Log.logInfo("LongRunningTask "+longRunningTaskID+" removed", LongRunningTaskRegistry.class);
    }
    
    public static void registerTask(String longRunningTaskID) {
    	Log.logInfo("LongRunningTask "+longRunningTaskID+" registered", LongRunningTaskRegistry.class);
    	LongRunningTaskResult lrtr = new LongRunningTaskResult();
    	longRunningTaskRegistryMap.put(longRunningTaskID, lrtr);
    }
    
    public static void completeTask(String longRunningTaskID, String jsonResult) {
    	Log.logInfo("LongRunningTask "+longRunningTaskID+" completed", LongRunningTaskRegistry.class);
    	LongRunningTaskResult lrtr = longRunningTaskRegistryMap.get(longRunningTaskID);
    	lrtr.setDone();
    	lrtr.setJsonResult(jsonResult);
    	longRunningTaskRegistryMap.put(longRunningTaskID, lrtr);
    }
	
    public static void completeTask(String longRunningTaskID, Exception e) {
    	Log.logWarning("LongRunningTask "+longRunningTaskID+" completed with exception", LongRunningTaskRegistry.class);
    	LongRunningTaskResult lrtr = longRunningTaskRegistryMap.get(longRunningTaskID);
    	lrtr.setDone();
    	lrtr.setError(e.getClass().getName()+" - "+e.getMessage()+"-"+e.getCause());
    	longRunningTaskRegistryMap.put(longRunningTaskID, lrtr);
    }
    
	public static void scheduleCleanup()  {
		Runnable task = () -> {
			for (Entry<String, LongRunningTaskResult> res : longRunningTaskRegistryMap.entrySet()) {
				Date date = new Date();
				long timeMilli = date.getTime();
			    if(res.getValue().getRegisteredTimestamp()+10000<timeMilli) {
			    	longRunningTaskRegistryMap.remove(res.getKey());
			    	Log.logInfo("Cleaned up long running task "+res.getKey()+" result as it was never requested", LongRunningTaskRegistry.class);
			    }
			}
		};
		scheduler.scheduleWithFixedDelay(task, 0, 30, TimeUnit.SECONDS);
	}
}
