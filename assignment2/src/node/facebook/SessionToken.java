package node.facebook;

public class SessionToken {
	
	public SessionToken(String user, String seed) {
		super();
		this.m_user = user;
		this.m_seed = seed;
	}

	private String m_user;
	private String m_seed;
	
	public static SessionToken createFromString(String token) {
		String[] parts = token.split(";");
		SessionToken s = new SessionToken(parts[0], parts[1]);
		return s;
	}

	@Override
	public String toString() {
		return m_user + ";" + m_seed;
	}
	
	public String getUser() {
		return m_user;
	}

	public void setUser(String user) {
		this.m_user = user;
	}

	public String getToken() {
		return m_seed;
	}

	public void setToken(String token) {
		this.m_seed = token;
	}
}
