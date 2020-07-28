package pojo;

public class CSRFToken {
	private static long lifetimeInSeconds = 3600;
	private String token;
	private long created;
	
	public CSRFToken(String token) {
		this.token = token;
		this.created = System.currentTimeMillis() / 1000L;
	}
	
	public boolean isStale() {
		long currentUnixTime = System.currentTimeMillis() / 1000L;
		return currentUnixTime>created+lifetimeInSeconds;
	}
	
	public long validUntil() {
		return created+lifetimeInSeconds-1;
	}
	
	public long getLifeTime() {
		return lifetimeInSeconds;
	}

	public String getToken() {
		return token;
	}
}
