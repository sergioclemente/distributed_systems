package node.facebook;

import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import edu.washington.cs.cse490h.lib.Utility;
import node.rpc.IFacebookServer;

public class FacebookShardSystem extends BaseFacebookSystem implements IFacebookServer {

	private Hashtable<String, User> m_users = new Hashtable<String, User>();
	private Hashtable<String, String> m_activeSessions = new Hashtable<String, String>();
	private Hashtable<String, List<String>> m_friends = new Hashtable<String, List<String>>();
	private Hashtable<String, List<String>> m_friendRequests = new Hashtable<String, List<String>>();
	
	// Distributed nodes
	private Hashtable<String, Vector<Message>> m_messages = new Hashtable<String, Vector<Message>>();
	
	/**
	 * FacebookShardSystem()
	 * @param node
	 */
	public FacebookShardSystem(FacebookRPCNode node) {
		super(node);
	}

	/**
	 * API: IFacebookServer.createUser
	 */
	public String createUser(String username, String password) throws FacebookException {
		if (this.isValidUser(username)) {
			throw new FacebookException(FacebookException.USER_ALREADY_EXISTS);
		} else {
			this.appendToLog("create_user " + username + " " + password);
			this.m_users.put(username, new User(username, password));
			
			// Create ancillary data structures now.
			// Makes the code cleaner than lazy creation.
			this.m_friends.put(username, new Vector<String>());
			this.m_friendRequests.put(username, new Vector<String>());
		}
		
		return null;
	}
	
	/**
	 * API: IFacebookServer.login
	 */
	public String login(String username, String password) throws FacebookException {
		if (this.isValidUser(username)) {
			String token = createNewSessionId();
			this.m_activeSessions.put(token, username);
			this.user_info("User: " + username + " logged in, token: " + token);
			return token;
		} else {
			throw new FacebookException(FacebookException.USER_DONT_EXIST);
		}
	}
	
	/**
	 * API: IFacebookServer.logout
	 */
	public String logout(String token) throws FacebookException {
		if (this.m_activeSessions.containsKey(token)) {
			this.m_activeSessions.remove(token);
			this.user_info("Token: " + token + " logged out");
		} else {
			throw new FacebookException(FacebookException.SESSION_DONT_EXIST);
		}
		
		return null;
	}
	
	/**
	 * API: IFacebookServer.addFriend
	 */
	public String addFriend(String token, String friendLogin) throws FacebookException {
		String login = this.getUser(token).getLogin();
		
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
		this.user_info("User: " + login + " requested to be friends of user " + friendLogin);
		
		return null;
	}
	
	/**
	 * API: IFacebookServer.acceptFriend
	 */
	public String acceptFriend(String token, String friendLogin) throws FacebookException {
		String login = this.getUser(token).getLogin();
		
		if (!this.isValidUser(friendLogin)) {
			throw new FacebookException(FacebookException.USER_DONT_EXIST);
		}
		
		List<String> requestList;
		requestList = this.m_friendRequests.get(login);
		
		User friendUser;
		friendUser = this.m_users.get(friendLogin);
		
		if (!requestList.contains(friendLogin)) {
			// Can't accept friendship of somebody who hasn't requested it
			throw new FacebookException(FacebookException.INVALID_REQUEST);
		}
		
		this.appendToLog("accept_friend " + login + " " + friendLogin);
		
		requestList.remove(friendUser);
		addFriendToList(login, friendLogin);
		addFriendToList(friendLogin, login);
		this.user_info("User: " + login + " accepted to be friends of user " + friendLogin);
		
		return null;
	}
	
	private void addFriendToList(String login, String friendLogin) throws FacebookException {
		List<String> listFriends;
		listFriends = this.m_friends.get(login);
		
		if (!listFriends.contains(friendLogin)) {
			listFriends.add(friendLogin);
		} else {
			this.user_info("Friend " + friendLogin + " already in " + login + "'s friend list");
		}
	}

	private String createNewSessionId()
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0 ; i < 10 ; i++) {
			sb.append(Character.toChars('0' + Utility.getRNG().nextInt(10)));
		}
		return sb.toString();
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
		// In recovery the parameter actually will be the login
		if (this.m_inRecovery) {
			return this.m_users.get(token); 
		}
		
		if (this.isLoggedIn(token)) {
			String login = this.m_activeSessions.get(token);
			return this.m_users.get(login);
		} else {
			throw new FacebookException(FacebookException.SESSION_DONT_EXIST);
		}
	}
	
	/**
	 * API: IFacebookServer.writeMessageOne
	 */
	
	public String writeMessageOne(String from, String to, String message) throws FacebookException {
		// TODO: implement
		return null;
	}
	
	/**
	 * API: IFacebookServer.writeMessageAll
	 */
	public String writeMessageAll(String from, String message) throws FacebookException {
		
		Set<String> logins = m_messages.keySet();
		
		Message m = new Message(from, message);
		for (String login : logins) {
			this.m_messages.get(login).add(m);
		}

		this.appendToLog("write_message_all " + from + " " + message);
		
		// Nothing to return
		return null;
	}
	
	/**
	 * API: IFacebookServer.readMessageAll
	 */
	public String readMessageAll(String login) throws FacebookException {
		if (!this.m_messages.containsKey(login)) {
			return null;
		}
		Vector<Message> messages = this.m_messages.get(login);
		StringBuffer sb = new StringBuffer();

		for (Message message : messages) {
			sb.append("From:");
			sb.append(message.getFromLogin());
		 	sb.append('\n');
		 	sb.append("Content:");
		 	sb.append(message.getMessage());
		 	sb.append('\n');
		}

		return sb.toString();
	}
}
