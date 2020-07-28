package helpers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.Priority;
import javax.security.auth.login.FailedLoginException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import persistence.DB;
import pojo.CSRFCheckResult;
import pojo.RS;
import pojo.Vals;
import service.CSRFRegistry;

@Authenticate
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		boolean isGet = requestContext.getMethod().equals(javax.ws.rs.HttpMethod.GET);

		String userNameFromHeader = requestContext.getHeaderString(Config.getUidHeaderName());
		Vals vals = new Vals();
		vals.addVal(userNameFromHeader);
		RS rs;
		try {
			rs = DB.doSelect("SELECT * FROM user WHERE name LIKE ?;", vals);
			int found = DB.getRowCount(rs.getRs());
			rs.close();
			if (found > 0) {
				if (!isGet) {
					String xsrf = requestContext.getHeaderString("X-XSRF");
					CSRFCheckResult xsrfres = CSRFRegistry.checkCSRF(xsrf, userNameFromHeader);
					if (!xsrfres.tokenMatches()) {
						abortWithXSRF(requestContext, userNameFromHeader);
					}
				}
			}
			return;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		abortWithUnauthorized(requestContext, userNameFromHeader);
	}

	private void abortWithXSRF(ContainerRequestContext requestContext, String userNameFromHeader) {
		Log.logInfo("Invalid XSRF Token provided by " + userNameFromHeader, AuthenticationFilter.class);
		requestContext.abortWith(Response.status(Response.Status.PRECONDITION_FAILED).entity("XSRF check failed").build());		
	}

	private void abortWithUnauthorized(ContainerRequestContext requestContext, String userNameFromHeader) {
		Log.logInfo("unknown user " + userNameFromHeader + " in auth header", AuthenticationFilter.class);
		requestContext.abortWith(Response.status(401).entity("unauthorized").build());
	}
	
	public static String getAuthHeader(HttpHeaders headers) throws FailedLoginException {
		List<String> uidHeaders = headers.getRequestHeader(Config.getUidHeaderName());
		if(uidHeaders == null || uidHeaders.size()!=1) {
			throw new FailedLoginException("No auth header provided");
		}
		return uidHeaders.get(0);
	}

}
