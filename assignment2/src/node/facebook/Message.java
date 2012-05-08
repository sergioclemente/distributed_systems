package node.facebook;

public class Message {
	
	public Message(String fromLogin, String message) {
		super();
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
	
	private String fromLogin;
	private String message;
}