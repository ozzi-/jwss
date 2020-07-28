package errorhandling;

import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.google.gson.JsonObject;

import helpers.Log;

@Provider
public class UncaughtException extends Throwable implements ExceptionMapper<Throwable> {
	private static final long serialVersionUID = 1L;

	@Context private ResourceInfo resourceInfo;
	public Response toResponse(Throwable exception) {
		JsonObject error = new JsonObject();
		error.addProperty("error", exception.getMessage());
		error.addProperty("method", resourceInfo.getResourceMethod().getName());
		Log.logException(new Exception(exception), this);
		return Response.status(500).entity(error.toString()).type("application/json").build();
	}
}