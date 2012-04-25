package node.facebook;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import node.rpc.RPCMethodCall;

import edu.washington.cs.cse490h.lib.PersistentStorageReader;
import edu.washington.cs.cse490h.lib.PersistentStorageWriter;
import edu.washington.cs.cse490h.lib.Utility;


public class FacebookFrontendSystem extends BaseFacebookSystem {
	private Hashtable<String, User> m_users = new Hashtable<String, User>();
	private Hashtable<String, String> m_activeSessions = new Hashtable<String, String>();
	private Hashtable<String, List<String>> m_friends = new Hashtable<String, List<String>>();
	private Hashtable<String, List<String>> m_friendRequests = new Hashtable<String, List<String>>();
	
	public FacebookFrontendSystem(FacebookRPCNode node) {
		super(node);
	}
	
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
	
	public String login(String username, String password) throws FacebookException {
		if (this.isValidUser(username)) {
			String token = createNewSessionId();
			this.m_activeSessions.put(token, username);
			this.info("User: " + username + " logged in, token: " + token);
			return token;
		} else {
			throw new FacebookException(FacebookException.USER_DONT_EXIST);
		}
	}
	
	public String logout(String token) throws FacebookException {
		if (this.m_activeSessions.containsKey(token)) {
			this.m_activeSessions.remove(token);
			this.info("Token: " + token + " logged out");
		} else {
			throw new FacebookException(FacebookException.SESSION_DONT_EXIST);
		}
		
		return null;
	}
	
	public String addFriend(String login, String friendLogin) throws FacebookException {		
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
		
		return null;
	}
	

	public String acceptFriend(String login, String friendLogin) throws FacebookException {
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
		
		return null;
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
		if (this.isLoggedIn(token)) {
			String login = this.m_activeSessions.get(token);
			return this.m_users.get(login);
		} else {
			throw new FacebookException(FacebookException.SESSION_DONT_EXIST);
		}
	}

	protected String callLocalMethod(String methodCall, Vector<String> params) throws FacebookException {		
		if (methodCall.startsWith("create_user")) {
			return this.createUser(params.get(0), params.get(1));
		} else if (methodCall.startsWith("add_friend")) {
			return this.addFriend(params.get(0), params.get(1));
		} else if (methodCall.startsWith("accept_friend")) {
			return this.acceptFriend(params.get(0), params.get(1));
		} else {
			return null;
		}
		
	}

	@Override
	protected boolean canCallLocalMethod(String methodCall, Vector<String> params) {
		return methodCall.equals("create_user") || 
				methodCall.equals("login") || 
				methodCall.equals("logout") ||
				methodCall.equals("add_friend") || 
				methodCall.equals("accept_friend");
	}
}
