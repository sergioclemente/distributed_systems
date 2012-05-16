package node.facebook;

public class Message {
	
	public Message(String fromLogin, String toLogin, String message) {
		super();
		this.setToLogin(toLogin);
		this.fromLogin = fromLogin;
		this.message = message;
	}
	
	public String getFromLogin() {
		return fromLogin;
	}
	
	public void setFromLogin(String fromLogin) {
		this.fromLogin = fromLogin;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getToLogin() {
		return toLogin;
	}

	public void setToLogin(String toLogin) {
		this.toLogin = toLogin;
	}

	private String fromLogin;
	private String message;
	private String toLogin;
}