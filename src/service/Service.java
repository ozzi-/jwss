
package service;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.json.JSONObject;

import helpers.Authenticate;
import helpers.AuthenticationFilter;
import helpers.Config;
import helpers.DoLog;
import helpers.Log;
import persistence.DB;

@Path("/")
@Singleton
public class Service extends ResourceConfig implements ContainerLifecycleListener {
	
	@Override
	public void onReload(Container container) {
		Log.logInfo("Reloading", this);
		Scheduler.exit();
		DB.exit();
	}

	@Override
	public void onShutdown(Container container) {
		Log.logInfo("Shutting down", this);
		Scheduler.exit();
		DB.exit();
	}

	@Override
	public void onStartup(Container container) {		
		Log.logInfo("Starting "+Config.appName, this);
		
	    Config.loadMailConfig();
	    Config.loadAppConfig();
	    try {
	    	DB.testDB();
	    }catch (Exception e) {
			Log.logException(e, Service.class);
	    	System.exit(1);
	    }
	    Scheduler.schedule();
	}
	
	@Authenticate
	@DoLog
	@GET
	@Path("/whoami")
	public Response getMyself(@Context HttpHeaders headers) throws Exception {
		String userName = AuthenticationFilter.getAuthHeader(headers);
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("name", userName);
		return Response.status(200).entity(jsonObj.toString()).type("application/json").build();
	}
    
	@GET
	@Path("/longrunning/{id}")
	public Response getLongRunningResult(@PathParam("id") String id) throws Exception {
		LongRunningTaskResult lrtr = LongRunningTaskRegistry.getTaskResult(id);
		if(lrtr.isDone()) {
			LongRunningTaskRegistry.removeTask(id);			
		}
		return Response.status(200).entity(lrtr.toJsonString()).type(MediaType.APPLICATION_JSON).build(); 
	}

}
