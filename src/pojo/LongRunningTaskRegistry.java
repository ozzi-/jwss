package service;

import java.util.concurrent.ConcurrentHashMap;

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
    
    // TODO interval to automatically purge stale results
}
