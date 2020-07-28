package errorhandling;

import javax.ws.rs.NotAllowedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import helpers.Log;

@Provider
public class NotAllowedMapper implements ExceptionMapper<NotAllowedException> {
	@Override
	public Response toResponse(NotAllowedException ex) {
		Log.logWarning("405 not found - "+ex.getMessage()+" "+ex.getCause(), this);
		return Response.status(405).entity("not allowed").type("text/plain").build();
	}
}