package node.facebook;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import edu.washington.cs.cse490h.lib.Utility;
import node.rpc.IFacebookServer;

public class FacebookShardSystem extends BaseFacebookSystem implements IFacebookServer {

	private Hashtable<String, User> m_users = new Hashtable<String, User>();
	private Set<String> m_activeSessions = new HashSet<String>();
	private Hashtable<String, List<String>> m_friends = new Hashtable<String, List<String>>();
	private Hashtable<String, List<String>> m_pendingFriendRequests = new Hashtable<String, List<String>>();
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
			
			// Create auxiliary data structures now.
			// Makes the code cleaner than lazy creation.
			this.m_friends.put(username, new Vector<String>());
			this.m_pendingFriendRequests.put(username, new Vector<String>());
		}
		
		return null;
	}
	
	/**
	 * API: IFacebookServer.login
	 */
	public String login(String username, String password) throws FacebookException {
		if (this.isValidUser(username)) {
			String token = new SessionToken(username, createNewSessionSeed()).toString();
			this.m_activeSessions.add(token);
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
		if (this.m_activeSessions.contains(token)) {
			this.m_activeSessions.remove(token);
			this.user_info("Token: " + token + " logged out");
		} else {
			throw new FacebookException(FacebookException.SESSION_DONT_EXIST);
		}
		
		return null;
	}
	
	public String addFriend_receiver(String adderLogin, String receiverLogin) throws FacebookException {
		if (!this.isValidUser(receiverLogin)) {
			throw new FacebookException(FacebookException.USER_DONT_EXIST);
		}
		
		// Only add to log valid friend requests 
		this.appendToLog("add_friend " + adderLogin + " " + receiverLogin);

		// Get the *friend's* request list
		List<String> listFriends;
		listFriends = this.m_pendingFriendRequests.get(receiverLogin);
		
		// Add the user to the friend's request list
		listFriends.add(adderLogin);
		this.user_info("User: " + adderLogin + " requested to be friends of user " + receiverLogin);
		
		return null;
	}
	
	/**
	 * API: IFacebookServer.acceptFriend
	 */
	public String acceptFriend_receiver(String adderLogin, String receiverLogin) throws FacebookException {
		List<String> requestList;
		requestList = this.m_pendingFriendRequests.get(receiverLogin);
		
		if (!requestList.contains(adderLogin)) {
			// Can't accept friendship of somebody who hasn't requested it
			throw new FacebookException(FacebookException.INVALID_REQUEST);
		}
		
		this.appendToLog("accept_friend_receiver " + adderLogin + " " + receiverLogin);
		
		requestList.remove(adderLogin);
		addFriendToList(receiverLogin, adderLogin);
		this.user_info("(Receiver) User: " + receiverLogin + " accepted to be friends of user " + adderLogin);
		
		return null;
	}
	
	public String acceptFriend_adder(String token, String adderLogin) throws FacebookException {
		String receiverLogin = extractUserLogin(token);
				
		this.appendToLog("accept_friend_adder " + receiverLogin + " " + adderLogin);
		
		addFriendToList(adderLogin, receiverLogin);
		this.user_info("(Adder) User: " + adderLogin + " accepted to be friends of user " + receiverLogin);
		
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

	private String createNewSessionSeed()
	{
		// TODO: temporary just to make debugging easier
		return "123";
//		StringBuffer sb = new StringBuffer();
//		for (int i = 0 ; i < 10 ; i++) {
//			sb.append(Character.toChars('0' + Utility.getRNG().nextInt(10)));
//		}
//		return sb.toString();
	}

	private String extractUserLogin(String token) throws FacebookException
	{
		if (!this.m_activeSessions.contains(token)) {
			throw new FacebookException(FacebookException.SESSION_DONT_EXIST);
		}
		
		return SessionToken.createFromString(token).getUser();
	}

	private boolean isValidUser(String login)
	{
		return this.m_users.containsKey(login);
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
	public String readMessageAll(String token) throws FacebookException {
		String login = extractUserLogin(token);

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
