package service;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.JsonObject;

import helpers.Authenticate;
import helpers.DoLog;
import helpers.JSONRef;
import persistence.DB;
import pojo.RS;
import pojo.Vals;

@Path("/todo")
@Singleton
public class TodoService {

	@Authenticate
	@DoLog
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getTodos(@Context HttpHeaders headers) throws Exception {
		RS rs = DB.doSelect("SELECT * FROM todo;", new Vals());
		String json = JSONRef.getAsJSONFromRS(rs.getRs());
		rs.close(); 
		return Response.status(200).entity(json).type("application/json").build();
	}

	@Authenticate
	@DoLog
	@GET
	@Path("/{id}")
	public Response getTodo(@Context HttpHeaders headers, @PathParam("id") int id) throws Exception {
		Vals vals =  new Vals().addVal(id);
		RS rs = DB.doSelect("SELECT * FROM todo WHERE id = ?;",vals);
		String json = JSONRef.getAsJSONFromRS_Single(rs.getRs());
		rs.close(); 

		return Response.status(200).entity(json).type("application/json").build();
	}
	
	@Authenticate
	@DoLog	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createTodo(@Context HttpHeaders headers, String body) throws Exception {		
		JsonObject jO = JSONRef.stringToJsonElem(body).getAsJsonObject();
		String content = jO.get("content").getAsString();
		
		Vals vals = new Vals().addVal(content);
		int id = DB.doInsert("INSERT INTO todo (content) VALUES (?);", vals);

		return Response.status(200).entity("{\"id\":"+id+"}").type("application/json").build();
	}
	
	@Authenticate
	@DoLog
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response editTodo(@Context HttpHeaders headers, String body, @PathParam("id") int id) throws Exception {
		JsonObject jO = JSONRef.stringToJsonElem(body).getAsJsonObject();
		String content = jO.get("content").getAsString();
		
		Vals vals = new Vals().addVal(content).addVal(id);		
		int s = DB.doUpdate("UPDATE todo SET content = ? WHERE id = ?;", vals);

		return Response.status(200).entity("{\"res\":"+s+"}").type("application/json").build();
	}
	
	@DELETE
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteTodo(@Context HttpHeaders headers, @PathParam("id") int id) throws Exception {				
		Vals vals = new Vals().addVal(id);
		int s = DB.doUpdate("DELETE FROM todo WHERE id = ?;", vals);
		return Response.status(200).entity("{\"res\":"+s+"}").type("application/json").build();
	}
	
	@GET
	@Path("/intensivecall")
	// this will demonstrate the concept of long running calls
	public Response getCountByDay() throws Exception {
		UUID uuid = UUID.randomUUID();
		String longRunningTaskID = uuid.toString();
		JsonObject jo = new JsonObject();
		jo.addProperty("longRunningTaskID",longRunningTaskID);
		LongRunningTaskRegistry.registerTask(longRunningTaskID);
		
		new Thread(
			new Runnable() {
				public void run() {
					// do something very time consuming 
					LongRunningTaskRegistry.completeTask(longRunningTaskID, result.toString());
				}
			}
		).start();
		return Response.status(200).entity(jo.toString()).type(MediaType.APPLICATION_JSON).build();
	}
	
}
