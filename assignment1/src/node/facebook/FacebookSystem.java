package node.facebook;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class FacebookSystem {
	private static final String FILE_NAME = "facebookdb.txt";
	
	private Hashtable<String, User> m_users = new Hashtable<String, User>();
	private Hashtable<String, List<User>> m_friends = new Hashtable<String, List<User>>();
	private Hashtable<String, List<User>> m_friendRequestss = new Hashtable<String, List<User>>();
	private Hashtable<String, List<Message>> m_messages = new Hashtable<String, List<Message>>();
	private Hashtable<String, String> m_activeSessions = new Hashtable<String, String>();
	private Random m_random = new Random();
	
	private INode m_node;
	
	public FacebookSystem(INode node) {
		this.m_node = node;
	}
	
	public void createUser(String username, String password) throws FacebookException {
		if (this.m_users.containsKey(username)) {
			throw new FacebookException(FacebookException.USER_ALREADY_EXISTS);
		} else {
			this.appendToLog("create_user " + username + password);
			this.m_users.put(username, new User(username, password));
		}
	}
	
	public String login(String username, String password) throws FacebookException {
		if (this.m_users.containsKey(username)) {
			//String token = nextSessionId();
			// todo: just to make testing easier
			String token = username;
			this.m_activeSessions.put(token, username);
			this.m_node.info("User: " + username + " logged in, token: " + token);
			return token;
		} else {
			throw new FacebookException(FacebookException.USER_DONT_EXIST);
		}
	}
	
	public void logout(String token) throws FacebookException {
		if (this.m_activeSessions.containsKey(token)) {
			this.m_activeSessions.remove(token);
			this.m_node.info("Token: " + token + " logged out");
		} else {
			throw new FacebookException(FacebookException.SESSION_DONT_EXIST);
		}
	}
	
	public void addFriend(String token, String friendLogin) throws FacebookException {
		User user = getUserFromToken(token);
		this.appendToLog("add_friend " + user.getLogin() + " " + friendLogin);
		
		String login = user.getLogin();
		
		internalAddFriend(login, friendLogin);
	}
	
	private void internalAddFriend(String login, String friendLogin) throws FacebookException {
		List<User> listFriends;
		if (this.m_friendRequestss.containsKey(login)) {
			listFriends = this.m_friends.get(login);
		} else {
			listFriends = new Vector<User>();
			this.m_friendRequestss.put(login, listFriends);
		}
		if (!this.m_users.containsKey(friendLogin)) {
			throw new FacebookException(FacebookException.USER_DONT_EXIST);
		}
		User friendUser = this.m_users.get(friendLogin);
		listFriends.add(friendUser);
		this.m_node.info("User: " + login + " requested to be friends of user " + friendLogin);
	}
	
	public void acceptFriend(String token, String friendLogin) throws FacebookException {
		User user = getUserFromToken(token);
		this.appendToLog("accept_friend " + user.getLogin() + " " + friendLogin);
		
		String login = user.getLogin();
		
		internalAcceptFriend(login, friendLogin);
	}
	
	private void internalAcceptFriend(String login, String friendLogin) throws FacebookException {
		addFriendToList(login, friendLogin);
		addFriendToList(friendLogin, login);
		this.m_node.info("User: " + login + " accepted to be friends of user " + friendLogin);
	}
	
	private void addFriendToList(String login, String friendLogin) throws FacebookException {
		List<User> listFriends;
		if (this.m_friends.containsKey(login)) {
			listFriends = this.m_friends.get(login);
		} else {
			listFriends = new Vector<User>();
			this.m_friends.put(login, listFriends);
		}
		if (!this.m_users.containsKey(friendLogin)) {
			throw new FacebookException(FacebookException.USER_DONT_EXIST);
		}
		User friendUser = this.m_users.get(friendLogin);
		listFriends.add(friendUser);
	}

	private User getUserFromToken(String token) throws FacebookException {
		if (this.m_activeSessions.containsKey(token)) {
			String login = this.m_activeSessions.get(token);
			return this.m_users.get(login);
		} else {
			throw new FacebookException(FacebookException.SESSION_DONT_EXIST);
		}
	}

	public void writeMessagesAll(String token, String message) throws FacebookException {
		User user = getUserFromToken(token);
		this.appendToLog("write_message_all " + user.getLogin() + " " + message);
		String login = user.getLogin();
		internalPostMessageToAllFriends(login, message);
		
		this.m_node.info("User: " + login + " posted the following message to all users " + message);
	}
	
	public void internalPostMessageToAllFriends(String login, String message) {
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

	public String readMessagesAll(String token) throws FacebookException {
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
			this.m_node.appendFileContents(FILE_NAME, content + "\n");
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
}
