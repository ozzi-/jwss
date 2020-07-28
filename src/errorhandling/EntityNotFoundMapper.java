package errorhandling;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import helpers.Log;

@Provider
public class EntityNotFoundMapper implements ExceptionMapper<NotFoundException> {
	@Override
	public Response toResponse(NotFoundException ex) {
		Log.logWarning("404 not found - "+ex.getMessage()+" "+ex.getCause(), this);
		return Response.status(404).entity(ex.getMessage()).type("text/plain").build();
	}
}