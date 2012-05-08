package node.facebook;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class FacebookShardState {
	private Hashtable<String, User> m_users = new Hashtable<String, User>();
	private Set<String> m_activeSessions = new HashSet<String>();
	private Hashtable<String, List<String>> m_friends = new Hashtable<String, List<String>>();
	private Hashtable<String, List<String>> m_pendingFriendRequests = new Hashtable<String, List<String>>();
	private Hashtable<String, Vector<Message>> m_messages = new Hashtable<String, Vector<Message>>();
		
	// Session management
	public boolean containsSession(String token) {
		return this.m_activeSessions.contains(token);
	}
	
	public void addSession(String token) {
		this.m_activeSessions.add(token);
	}
	
	public void removeSession(String token) {
		this.m_activeSessions.remove(token);
	}
	
	// User management
	public User getUser(String userName) {
		return this.m_users.get(userName);
	}
	
	public void addUser(String userName, User user) {
		assert !this.m_users.containsKey(userName);
		
		this.m_users.put(userName, user);
		
		// Create auxiliary data structures now.
		// Makes the code cleaner than lazy creation.
		this.m_friends.put(userName, new Vector<String>());
		this.m_pendingFriendRequests.put(userName, new Vector<String>());
		this.m_messages.put(userName, new Vector<Message>());
	}

	public boolean containsUser(String userName) {
		return this.m_users.containsKey(userName);
	}
	
	public Set<String> getUserLogins() {
		return m_users.keySet();
	}
	
	public boolean isFriendOf(String userName1, String userName2) {
		return this.m_friends.get(userName1).contains(userName2);
	}
	
	// Pending request
	public List<String> getPendingRequest(String userName) {
		return this.m_pendingFriendRequests.get(userName);
	}
	
	public void addPendingRequest(User userName, String userNameFriend) {
		this.m_pendingFriendRequests.get(userName).add(userNameFriend);
	}
	
	// Friend Management
	public void addFriendToList(String login, String friendLogin) throws FacebookException {
		List<String> listFriends;
		listFriends = this.m_friends.get(login);
		
		if (!listFriends.contains(friendLogin)) {
			listFriends.add(friendLogin);
		} else {
			this.user_info("Friend " + friendLogin + " already in " + login + "'s friend list");
		}
	}
	
	// Message Management
	public void addMessage(String userName, Message m) {
		Vector<Message> messages = this.m_messages.get(userName);
		if (messages != null) {
			messages.add(m);
		}
	}
	
	public Vector<Message> getUserMessages(String userName) {
		return this.m_messages.get(userName);
	}
	
	protected void user_info(String s) {
		// Use a different prefix to be easy to distinguish
		System.out.println(">>>> " + s);
	}
}
