package helpers;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

@Authenticate
@Provider
@Priority(Priorities.USER)
public class LogFilter implements ContainerRequestFilter {

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		String userNameFromHeader = requestContext.getHeaderString(Config.getUidHeaderName());
		String method = requestContext.getMethod();
		String uri = "/"+requestContext.getUriInfo().getPath();

		Log.logInfo(method+" "+uri, userNameFromHeader, this);
	}
}
