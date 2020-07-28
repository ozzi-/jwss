package errorhandling;

import javax.security.auth.login.FailedLoginException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import helpers.Log;

@Provider
public class UnauthorizedMapper implements ExceptionMapper<FailedLoginException> {
	@Override
	public Response toResponse(FailedLoginException exception) {
		Log.logWarning("401 unauthorized", this);
		return Response.status(401).entity("unauthorized - "+exception.getMessage()).type("text/plain").build();
	}
}