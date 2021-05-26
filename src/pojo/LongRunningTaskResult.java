package pojo;

import java.util.Date;

import com.google.gson.JsonObject;

public class LongRunningTaskResult {
	private String jsonResult;
	private long registeredTimestamp;
	private boolean done;
	
	public LongRunningTaskResult() {
		Date date = new Date();
		this.registeredTimestamp = date.getTime();
		this.done=false;
	}
	
	public String getJsonResult() {
		return jsonResult;
	}
	public void setJsonResult(String jsonResult) {
		this.jsonResult = jsonResult;
	}
	public long getRegisteredTimestamp() {
		return registeredTimestamp;
	}

	public boolean isDone() {
		return done;
	}
	public void setDone() {
		this.done = true;
	}
	
	public String toJsonString() {
		JsonObject res = new JsonObject();
		res.addProperty("result", jsonResult);
		res.addProperty("registeredTimestamp", registeredTimestamp);
		res.addProperty("done", done);
		return res.toString();
	}
}
