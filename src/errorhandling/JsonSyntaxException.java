package errorhandling;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import helpers.Log;

@Provider
public class JsonSyntaxException implements ExceptionMapper<com.google.gson.JsonSyntaxException> {
	@Override
	public Response toResponse(com.google.gson.JsonSyntaxException ex) {
		Log.logWarning("400 - "+ex.getMessage()+" "+ex.getCause(), this);
		return Response.status(400).entity("invalid json received").type("text/plain").build();
	}
}