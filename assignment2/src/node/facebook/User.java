package node.facebook;

public class User {
	
	public User(String login, String password) {
		super();
		this.m_login = login;
		this.m_password = password;
	}
	
	public String getLogin() {
		return m_login;
	}
	
	public void setLogin(String name) {
		this.m_login = name;
	}
	
	public String getPassword() {
		return m_password;
	}
	
	public void setPassword(String password) {
		this.m_password = password;
	}
	
	private String m_login;
	private String m_password;
}