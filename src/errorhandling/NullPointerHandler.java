package errorhandling;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import helpers.Log;

@Provider
public class NullPointerHandler implements ExceptionMapper<NullPointerException> {
	@Override
	public Response toResponse(NullPointerException ex) {
		Log.logWarning("400 - Bad Request - Missing required data (NPE) - "+ex.getMessage()+" "+ex.getCause(), this);
		return Response.status(400).entity("Invalid Request - NPE").type("text/plain").build();
	}
}