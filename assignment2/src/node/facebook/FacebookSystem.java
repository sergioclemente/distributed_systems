package node.facebook;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import edu.washington.cs.cse490h.lib.PersistentStorageReader;
import edu.washington.cs.cse490h.lib.PersistentStorageWriter;
import edu.washington.cs.cse490h.lib.Utility;


public class FacebookSystem {
	private static final String FILE_NAME = "facebookdb.txt";
	
	// Distributed nodes
	private Hashtable<String, List<Message>> m_messages = new Hashtable<String, List<Message>>();
	
	// Central node
	private Hashtable<String, User> m_users = new Hashtable<String, User>();
	private Hashtable<String, String> m_activeSessions = new Hashtable<String, String>();
	private Hashtable<String, List<String>> m_friends = new Hashtable<String, List<String>>();
	private Hashtable<String, List<String>> m_friendRequests = new Hashtable<String, List<String>>();
	
	private boolean m_inRecovery = false;
	private FacebookRPCNode m_node;
	
	public FacebookSystem(FacebookRPCNode node) {
		this.m_node = node;
	}
	
	public void createUser(String username, String password) throws FacebookException {
		if (this.isValidUser(username)) {
			throw new FacebookException(FacebookException.USER_ALREADY_EXISTS);
		} else {
			this.appendToLog("create_user " + username + " " + password);
			this.m_users.put(username, new User(username, password));
			
			// Create ancillary data structures now.
			// Makes the code cleaner than lazy creation.
			this.m_friends.put(username, new Vector<String>());
			this.m_friendRequests.put(username, new Vector<String>());
			this.m_messages.put(username,  new Vector<Message>());
		}
	}
	
	public String login(String username, String password) throws FacebookException {
		if (this.isValidUser(username)) {
			String token = createSessionId();
			this.m_activeSessions.put(token, username);
			this.info("User: " + username + " logged in, token: " + token);
			return token;
		} else {
			throw new FacebookException(FacebookException.USER_DONT_EXIST);
		}
	}
	
	public void logout(String token) throws FacebookException {
		if (this.m_activeSessions.containsKey(token)) {
			this.m_activeSessions.remove(token);
			this.info("Token: " + token + " logged out");
		} else {
			throw new FacebookException(FacebookException.SESSION_DONT_EXIST);
		}
	}
	
	private boolean isLoggedIn(String token)
	{
		return this.m_activeSessions.containsKey(token);
	}

	private boolean isValidUser(String username)
	{
		return this.m_users.containsKey(username);
	}
	
	public User getUser(String token) throws FacebookException {
		if (this.isLoggedIn(token)) {
			String login = this.m_activeSessions.get(token);
			return this.m_users.get(login);
		} else {
			throw new FacebookException(FacebookException.SESSION_DONT_EXIST);
		}
	}
	
	public void addFriend(String login, String friendLogin) throws FacebookException {		
		if (!this.isValidUser(friendLogin)) {
			throw new FacebookException(FacebookException.USER_DONT_EXIST);
		}
		
		// Only add to log valid friend requests 
		this.appendToLog("add_friend " + login + " " + friendLogin);

		// Get the *friend's* request list
		List<String> listFriends;
		listFriends = this.m_friendRequests.get(friendLogin);
		
		// Add the user to the friend's request list
		listFriends.add(login);
		this.info("User: " + login + " requested to be friends of user " + friendLogin);
	}
	

	public void acceptFriend(String login, String friendLogin) throws FacebookException {
		if (!this.isValidUser(friendLogin)) {
			throw new FacebookException(FacebookException.USER_DONT_EXIST);
		}
		
		List<String> requestList;
		requestList = this.m_friendRequests.get(login);
		
		User friendUser;
		friendUser = this.m_users.get(friendLogin);
		
		if (!requestList.contains(friendUser)) {
			// Can't accept friendship of somebody who hasn't requested it
			throw new FacebookException(FacebookException.INVALID_REQUEST);
		}
		
		this.appendToLog("accept_friend " + login + " " + friendLogin);
		
		requestList.remove(friendUser);
		addFriendToList(login, friendLogin);
		addFriendToList(friendLogin, login);
		this.info("User: " + login + " accepted to be friends of user " + friendLogin);
	}
	
	private void addFriendToList(String login, String friendLogin) throws FacebookException {
		List<String> listFriends;
		listFriends = this.m_friends.get(login);
		
		if (!listFriends.contains(friendLogin)) {
			listFriends.add(friendLogin);
		} else {
			this.info("Friend " + friendLogin + " already in " + login + "'s friend list");
		}
	}

	public void writeMessagesAll(String token, String message) throws FacebookException {
//		User user = getUserFromToken(token);
//		String login = user.getLogin();
//
//		assert this.m_friends.containsKey(login);
//		List<User> friends = this.m_friends.get(login);
//		Message m = new Message(login, message);
//		for (User friend : friends) {
//			List<Message> msglist;
//			msglist = this.m_messages.get(friend.getLogin());
//			msglist.add(m);
//			this.info("Added to user " + friend.getLogin() + " message: [" + m.getMessage() + "]");
//		}
//
//		this.appendToLog("write_message_all " + user.getLogin() + " " + message);
//	
//		this.info("User: " + login + " posted the following message to all users " + message);
	}
	
	public String readMessagesAll(String token) throws FacebookException {
//		User user = getUserFromToken(token);
//		String login = user.getLogin();
//		
//		StringBuffer sb = new StringBuffer();
//		assert this.m_messages.containsKey(login);
//		
//		List<Message> listOfMessages = this.m_messages.get(login);
//		for (Message message : listOfMessages) {
//			sb.append("From:");
//			sb.append(message.getFromLogin());
//			sb.append('\n');
//			sb.append("Content:");
//			sb.append(message.getMessage());
//			sb.append('\n');
//		}
//
//		return sb.toString();
		return "";
	}
	
	private void appendToLog(String content) {
		// Don't append to the log if in recovery mode
		if (!m_inRecovery) {
			try {
				this.m_node.appendFileContents(FILE_NAME, content + "\n");
			} catch (IOException e) {
				// TODO: return the proper error
				e.printStackTrace();
			}
		}
	}

	private String createSessionId()
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0 ; i < 10 ; i++) {
			sb.append(Character.toChars('0' + Utility.getRNG().nextInt(10)));
		}
		return sb.toString();
	}
	
	private void info(String s) {
		// Use a different prefix to be easy to distinguish
		System.out.println(">>>> " + s);
	}

	public void recoverFromCrash() {
		try {
			m_inRecovery = true;
			if (Utility.fileExists(this.m_node, FILE_NAME)) {
				PersistentStorageReader psr = this.m_node.getReader(FILE_NAME);
				String line;
				while ((line = psr.readLine()) != null) {
					info("Recovery: replaying command from log: " + line);
					executeCommand(line);
				}
				psr.close();				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			m_inRecovery = false;
		}
	}

	private void executeCommand(String command) throws FacebookException {
		String[] parts = command.split("\\s+");
		String methodName = parts[0];
		
		if (methodName.startsWith("create_user")) {
			this.createUser(parts[1], parts[2]);
		} else if (methodName.startsWith("add_friend")) {
			this.addFriend(parts[1], parts[2]);
		} else if (methodName.startsWith("accept_friend")) {
			this.acceptFriend(parts[1], parts[2]);
		} else if (methodName.startsWith("write_message_all")) {
			this.writeMessagesAll(parts[1], parts[2]);
		} else {
			throw new FacebookException(FacebookException.INVALID_FACEBOOK_METHOD);
		}
		
	}
}
