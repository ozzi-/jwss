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
    static ConcurrentHashMap<String, LongRunningTaskResult> longRunningTaskRegistryMap = new ConcurrentHashMap<>();
    
    public static LongRunningTaskResult getTaskResult(String longRunningTaskID) {
    	return longRunningTaskRegistryMap.get(longRunningTaskID);
    }
    
    public static void removeTask(String longRunningTaskID) {
    	longRunningTaskRegistryMap.remove(longRunningTaskID);
    }
    
    public static void registerTask(String longRunningTaskID) {
    	LongRunningTaskResult lrtr = new LongRunningTaskResult();
    	longRunningTaskRegistryMap.put(longRunningTaskID, lrtr);
    }
    
    public static void completeTask(String longRunningTaskID, String jsonResult) {
    	LongRunningTaskResult lrtr = longRunningTaskRegistryMap.get(longRunningTaskID);
    	lrtr.setDone();
    	lrtr.setJsonResult(jsonResult);
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
