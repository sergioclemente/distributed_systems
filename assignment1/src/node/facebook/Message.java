package node.facebook;

public class Message {
	public Message(String fromLogin, String message) {
		super();
		this.m_fromLogin = fromLogin;
		this.m_message = message;
	}
	public String getFromLogin() {
		return m_fromLogin;
	}
	public void setFromLogin(String fromLogin) {
		this.m_fromLogin = fromLogin;
	}
	public String getMessage() {
		return m_message;
	}
	public void setMessage(String message) {
		this.m_message = message;
	}
	private String m_fromLogin;
	private String m_message;
}