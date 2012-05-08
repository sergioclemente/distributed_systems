package node.facebook;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.google.gson.Gson;

public class FacebookShardState {
	private Hashtable<String, User> users = new Hashtable<String, User>();
	private Set<String> sessions = new HashSet<String>();
	private Hashtable<String, List<String>> friends = new Hashtable<String, List<String>>();
	private Hashtable<String, List<String>> pendingFriendRequests = new Hashtable<String, List<String>>();
	private Hashtable<String, Vector<Message>> messages = new Hashtable<String, Vector<Message>>();
		
	// Session management
	public boolean containsSession(String token) {
		return this.sessions.contains(token);
	}
	
	public void addSession(String token) {
		this.sessions.add(token);
	}
	
	public void removeSession(String token) {
		this.sessions.remove(token);
	}
	
	// User management
	public User getUser(String userName) {
		return this.users.get(userName);
	}
	
	public void addUser(String userName, User user) {
		assert !this.users.containsKey(userName);
		
		this.users.put(userName, user);
		
		// Create auxiliary data structures now.
		// Makes the code cleaner than lazy creation.
		this.friends.put(userName, new Vector<String>());
		this.pendingFriendRequests.put(userName, new Vector<String>());
		this.messages.put(userName, new Vector<Message>());
	}

	public boolean containsUser(String userName) {
		return this.users.containsKey(userName);
	}
	
	public Set<String> getUserLogins() {
		return users.keySet();
	}
	
	public boolean isFriendOf(String userName1, String userName2) {
		return this.friends.get(userName1).contains(userName2);
	}
	
	// Pending request
	public List<String> getPendingRequest(String userName) {
		return this.pendingFriendRequests.get(userName);
	}
	
	public void addPendingRequest(User userName, String userNameFriend) {
		this.pendingFriendRequests.get(userName).add(userNameFriend);
	}
	
	// Friend Management
	public void addFriendToList(String login, String friendLogin) throws FacebookException {
		List<String> listFriends;
		listFriends = this.friends.get(login);
		
		if (!listFriends.contains(friendLogin)) {
			listFriends.add(friendLogin);
		} else {
			this.user_info("Friend " + friendLogin + " already in " + login + "'s friend list");
		}
	}
	
	// Message Management
	public void addMessage(String userName, Message m) {
		Vector<Message> messages = this.messages.get(userName);
		if (messages != null) {
			messages.add(m);
		}
	}
	
	public Vector<Message> getUserMessages(String userName) {
		return this.messages.get(userName);
	}
	
	protected void user_info(String s) {
		// Use a different prefix to be easy to distinguish
		System.out.println(">>>> " + s);
	}
	
	// Serialization
	public static String serialize(FacebookShardState state) {
		Gson g = new Gson();
		return g.toJson(state);
	}
	
	public static FacebookShardState deserialize(String str) {
		Gson g = new Gson();
		return g.fromJson(str, FacebookShardState.class);
	}
	
	// Clone
	public FacebookShardState clone() {
		// Dumb way to implement the clone, but lets get things done then optimize later
		String content = this.serialize(this);
		return deserialize(content);
	}
}
