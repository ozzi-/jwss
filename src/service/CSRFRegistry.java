package service;

import java.security.MessageDigest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import helpers.Authenticate;
import helpers.AuthenticationFilter;
import helpers.Crypto;
import helpers.DoLog;
import pojo.CSRFCheckResult;
import pojo.CSRFToken;

@Path("/csrf")
public class CSRFRegistry {
		
	private static Map<String, CSRFToken> registry = new ConcurrentHashMap<String, CSRFToken>();
	private static final int csrfTokenLength = 10;
	
	@Authenticate
   	 @DoLog
	@GET
	public Response getCSRF(@Context HttpHeaders headers) throws Exception {
		CSRFToken csrfToken = getToken(AuthenticationFilter.getAuthHeader(headers));
		String json = "{\"csrfToken\" : \""+csrfToken.getToken()+"\", \"lifetime\": \""+csrfToken.getLifeTime()+"\"}";
		return Response.status(200).entity(json).type("application/json").build();
	}
	
	@Authenticate
	@DoLog
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/check")
	public Response checkCSRF(@Context HttpHeaders headers, String csrfToken) throws Exception {
		CSRFCheckResult res = checkCSRF(csrfToken, AuthenticationFilter.getAuthHeader(headers));		
		return Response.status(200).entity(res.asJSON()).type("application/json").build();
	}

	public static CSRFCheckResult checkCSRF(String csrfToken, String subjectUsername)  {
		CSRFToken registryToken = registry.get(subjectUsername);
		if(registryToken==null) {
			return new CSRFCheckResult(false,false,false);
		}
		boolean isStale = registryToken.isStale();
		if(MessageDigest.isEqual(registryToken.getToken().getBytes(),csrfToken.getBytes())) {
			return new CSRFCheckResult(true,true,isStale);
		}
		return new CSRFCheckResult(true,false,isStale);
	}
	
	public static CSRFToken getToken(String username) {
		CSRFToken currentToken = registry.get(username);
		if(currentToken==null || currentToken.isStale()) {
			String token = Crypto.generateCSRF(csrfTokenLength);
			CSRFToken csrfToken = new CSRFToken(token);
			registry.put(username, csrfToken);
			return csrfToken;
		}
		return currentToken;
	}
}
