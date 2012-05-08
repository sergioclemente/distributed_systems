package node.facebook;

public class SessionToken {
	private String user;
	private String seed;
	
	public SessionToken(String user, String seed) {
		super();
		this.user = user;
		this.seed = seed;
	}
	
	public static SessionToken createFromString(String token) {
		String[] parts = token.split(";");
		SessionToken s = new SessionToken(parts[0], parts[1]);
		return s;
	}

	@Override
	public String toString() {
		return user + ";" + seed;
	}
	
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getToken() {
		return seed;
	}

	public void setToken(String token) {
		this.seed = token;
	}
}
