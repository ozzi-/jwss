package errorhandling;

import javax.management.InstanceAlreadyExistsException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.google.gson.JsonObject;

import helpers.Log;

@Provider
public class AlreadyExistsMapper implements ExceptionMapper<InstanceAlreadyExistsException> {

	@Override
	public Response toResponse(InstanceAlreadyExistsException ex) {
	
		JsonObject error = new JsonObject();
		error.addProperty("error", ex.getMessage());
		Log.logInfo(ex.getMessage(), this);

		return Response.status(409).entity(error.toString()).type("application/json").build();
	}
}