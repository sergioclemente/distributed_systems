package node.facebook;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import node.rpc.RPCNode;

public class FacebookRPCNode extends RPCNode {
	private class User {
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
	private class Message {
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
	
	private static final String FILE_NAME = "facebookdb.txt";
	
	private Hashtable<String, User> m_users = new Hashtable<String, User>();
	private Hashtable<String, List<User>> m_friends = new Hashtable<String, List<User>>();
	private Hashtable<String, List<User>> m_friendRequestss = new Hashtable<String, List<User>>();
	private Hashtable<String, List<Message>> m_messages = new Hashtable<String, List<Message>>();
	private Hashtable<String, String> m_activeSessions = new Hashtable<String, String>();
	private Random m_random = new Random();
	
	

	
	
	private void createUser(String username, String password) {
		if (this.m_users.containsKey(username)) {
			// TODO: throw error
		} else {
			this.appendToLog("create_user " + username + password);
			this.m_users.put(username, new User(username, password));
		}
	}
	
	private String login(String username, String password) {
		if (this.m_users.containsKey(username)) {
			//String token = nextSessionId();
			// todo: just to make testing easier
			String token = username;
			this.m_activeSessions.put(token, username);
			info("User: " + username + " logged in, token: " + token);
			return token;
		} else {
			return "";
		}
	}
	
	private void logout(String token) {
		if (this.m_activeSessions.containsKey(token)) {
			this.m_activeSessions.remove(token);
			info("Token: " + token + " logged out");
		} else {
			// TODO: throw error
		}
	}
	
	private void addFriend(String token, String friendLogin) {
		User user = getUserFromToken(token);
		this.appendToLog("add_friend " + user.getLogin() + " " + friendLogin);
		
		String login = user.getLogin();
		
		internalAddFriend(login, friendLogin);
	}
	
	private void internalAddFriend(String login, String friendLogin) {
		List<User> listFriends;
		if (this.m_friendRequestss.containsKey(login)) {
			listFriends = this.m_friends.get(login);
		} else {
			listFriends = new Vector<User>();
			this.m_friendRequestss.put(login, listFriends);
		}
		// TODO: check for invalid login
		User friendUser = this.m_users.get(friendLogin);
		listFriends.add(friendUser);
		info("User: " + login + " requested to be friends of user " + friendLogin);
	}
	
	private void acceptFriend(String token, String friendLogin) {
		User user = getUserFromToken(token);
		this.appendToLog("accept_friend " + user.getLogin() + " " + friendLogin);
		
		String login = user.getLogin();
		
		internalAcceptFriend(login, friendLogin);
	}
	
	private void internalAcceptFriend(String login, String friendLogin) {
		addFriendToList(login, friendLogin);
		addFriendToList(friendLogin, login);
		info("User: " + login + " accepted to be friends of user " + friendLogin);
	}
	
	private void addFriendToList(String login, String friendLogin) {
		List<User> listFriends;
		if (this.m_friends.containsKey(login)) {
			listFriends = this.m_friends.get(login);
		} else {
			listFriends = new Vector<User>();
			this.m_friends.put(login, listFriends);
		}
		// TODO: check for invalid login
		User friendUser = this.m_users.get(friendLogin);
		listFriends.add(friendUser);
	}

	private User getUserFromToken(String token) {
		if (this.m_activeSessions.containsKey(token)) {
			String login = this.m_activeSessions.get(token);
			return this.m_users.get(login);
		} else {
			// todo: error handling
			return null;
		}
	}

	private void writeMessagesAll(String token, String message) {
		User user = getUserFromToken(token);
		this.appendToLog("write_message_all " + user.getLogin() + " " + message);
		String login = user.getLogin();
		internalPostMessageToAllFriends(login, message);
		
		info("User: " + login + " posted the following message to all users " + message);
	}
	
	private void internalPostMessageToAllFriends(String login, String message) {
		if (this.m_friends.containsKey(login)) {
			List<User> friends = this.m_friends.get(login);
			Message m = new Message(login, message);
			for (User friend : friends) {
				List<Message> messages;
				if (this.m_messages.containsKey(friend.getLogin())) {
					messages = this.m_messages.get(friend.getLogin());
				} else {
					messages = new Vector<Message>();
					this.m_messages.put(friend.getLogin(), messages);
				}
				messages.add(m);
			}
		}
	}

	private String readMessagesAll(String token) {
		User user = getUserFromToken(token);
		String login = user.getLogin();
		
		StringBuffer sb = new StringBuffer();
		if (this.m_messages.containsKey(login)) {
			List<Message> listOfMessages = this.m_messages.get(login);
			for (Message message : listOfMessages) {
				sb.append(message.getMessage());
				sb.append('\n');
			}
		}
		return sb.toString();
	}
	
	private void appendToLog(String content) {
		try {
			this.appendFileContents(FILE_NAME, content + "\n");
		} catch (IOException e) {
			// TODO: return the proper error
			e.printStackTrace();
		}
	}

	private String nextSessionId()
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0 ; i < 10 ; i++) {
			sb.append(Character.toChars('0' + m_random.nextInt(10)));
		}
		return sb.toString();
	}
	  
	@Override
	public void onCommand(String command) {
		String[] parts = command.split("\\s+");
		
		String verb = parts[0];
		
		if (verb.startsWith("create_user")) {
			this.createUser(parts[1], parts[2]);
		} else if (verb.startsWith("login")) {
			System.out.println(this.login(parts[1], parts[2]));
		} else if (verb.startsWith("logout")) {
			this.logout(parts[1]);
		} else if (verb.startsWith("add_friend")) {
			this.addFriend(parts[1], parts[2]);
		} else if (verb.startsWith("accept_friend")) {
			this.acceptFriend(parts[1], parts[2]); 
		} else if (verb.startsWith("write_message_all")) {
			this.writeMessagesAll(parts[1], parts[2]);
		} else if (verb.startsWith("read_message_all")) {
			System.out.println(this.readMessagesAll(parts[1]));
		}
	}
	
	@Override
	protected void onMethodCalled (int from, String methodName, Vector<String> params) {
		Vector<String> returnParams = new Vector();
		String returnMethodName = "status_call";
		
		try {
			String returnValue = null;
			
			if (methodName == "create_user") {
				this.createUser(params.get(0), params.get(1));
			} else if (methodName == "login") {
				returnValue = this.login(params.get(0), params.get(1));
			} else if (methodName == "logout") {
				this.logout(params.get(0));
			} else if (methodName == "add_friend") {
				this.addFriend(params.get(0), params.get(1));
			} else if (methodName == "accept_friend") {
				this.acceptFriend(params.get(0), params.get(1));
			} else if (methodName == "write_message_all") {
				this.writeMessagesAll(params.get(0), params.get(1));
			} else if (methodName == "read_message_all"){
				returnValue = this.readMessagesAll(params.get(0));
			}
			
			returnParams.add("ok");
			if (returnValue != null) {
				returnParams.add(returnValue);
			}
		} catch (Exception e) {
			returnParams.add("error");
			returnParams.add(e.getMessage());
		}
		
		this.callMethod(from, returnMethodName, returnParams);
	}
}
