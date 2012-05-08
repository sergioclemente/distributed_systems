package node.facebook;

import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import edu.washington.cs.cse490h.lib.Utility;
import node.rpc.IFacebookServer;
import node.rpc.RPCMethodCall;
import edu.washington.cs.cse490h.lib.PersistentStorageReader;
import edu.washington.cs.cse490h.lib.PersistentStorageWriter;


public class FacebookShardSystem extends BaseFacebookSystem implements IFacebookServer {

	private FacebookShardState m_state = new FacebookShardState();
	
	private static final String FILE_NAME = "facebookstate.txt";
	
	private void saveState() {
		try {
			String content = FacebookShardState.serialize(m_state);
			
			PersistentStorageWriter psw = m_node.getWriter(FILE_NAME, false);
			psw.write(content);
			psw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void recoverFromCrash() {
		try {
			if (Utility.fileExists(m_node, FILE_NAME)) {
				PersistentStorageReader psr = m_node.getReader(FILE_NAME);
				char[] buffer = new char[1024];
				StringBuffer sb = new StringBuffer();
				do {
					int len = psr.read(buffer, 0, 1024);
					if (len > 0) {
						sb.append(buffer, 0, len);
					} else {
						break;
					}
				} while (true);
				
				this.m_state = FacebookShardState.deserialize(sb.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
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
	public String createUser(String userName, String password) throws FacebookException {
		if (this.m_state.containsUser(userName)) {
			throw new FacebookException(FacebookException.USER_ALREADY_EXISTS);
		} else {
			this.m_state.addUser(userName, new User(userName, password));
			this.saveState();		
			this.user_info("created user " + userName + " " + password);
		}
		
		return null;
	}
	
	/**
	 * API: IFacebookServer.login
	 */
	public String login(String userName, String password) throws FacebookException {
		if (this.m_state.containsUser(userName)) {
			String token = new SessionToken(userName, createNewSessionSeed()).toString();
			this.m_state.addSession(token);
			
			this.saveState();
			this.user_info("User: " + userName + " logged in, token: " + token);
			return token;
		} else {
			throw new FacebookException(FacebookException.USER_DONT_EXIST);
		}
	}
	
	/**
	 * API: IFacebookServer.logout
	 */
	public String logout(String token) throws FacebookException {
		if (this.m_state.containsSession(token)) {
			this.m_state.removeSession(token);
			
			this.saveState();
			this.user_info("Token: " + token + " logged out");
		} else {
			throw new FacebookException(FacebookException.SESSION_DONT_EXIST);
		}
		
		return null;
	}
	
	public String addFriendReceiver(String adderLogin, String receiverLogin) throws FacebookException {
		if (!this.m_state.containsUser(receiverLogin)) {
			throw new FacebookException(FacebookException.USER_DONT_EXIST);
		}

		// Get the *friend's* request list
		List<String> listFriends;
		listFriends = this.m_state.getPendingRequest(receiverLogin);
		
		// Add the user to the friend's request list
		listFriends.add(adderLogin);
		
		this.saveState();
		this.user_info("User: " + adderLogin + " requested to be friends of user " + receiverLogin);
		
		return null;
	}
	
	/**
	 * API: IFacebookServer.acceptFriend
	 */
	public String acceptFriendReceiver(String adderLogin, String receiverLogin) throws FacebookException {
		List<String> requestList;
		requestList = this.m_state.getPendingRequest(receiverLogin);
		
		if (!requestList.contains(adderLogin)) {
			// Can't accept friendship of somebody who hasn't requested it
			throw new FacebookException(FacebookException.INVALID_REQUEST);
		}
		
		requestList.remove(adderLogin);
		this.m_state.addFriendToList(receiverLogin, adderLogin);
		
//		this.saveState();
		this.user_info("(Receiver) User: " + receiverLogin + " accepted to be friends of user " + adderLogin);
		
		return null;
	}
	
	public String acceptFriendAdder(String token, String adderLogin) throws FacebookException {
		// TODO: either remove session tokens, or auto-add session token to 'receiverLogin'
		String receiverLogin = token;
		
		if (!this.m_state.containsUser(receiverLogin)) {
			throw new FacebookException(FacebookException.SESSION_DONT_EXIST);
		}
		
		this.m_state.addFriendToList(receiverLogin, adderLogin);
		
		this.saveState();
		this.user_info("(Adder) User: " + receiverLogin + " was auto-added as friends of user " + adderLogin);
		
		return null;
	}
	


	private String createNewSessionSeed()
	{
		// Let's return a fixed value to make our life easier 
		return "1234";
	}

	private String extractUserLogin(String token) throws FacebookException
	{
		if (!this.m_state.containsSession(token)) {
			throw new FacebookException(FacebookException.SESSION_DONT_EXIST);
		}
		
		return SessionToken.createFromString(token).getUser();
	}
	
	/**
	 * API: IFacebookServer.writeMessageAll
	 */
	public String writeMessageAll(String from, String message) throws FacebookException {
		
		Set<String> logins = this.m_state.getUserLogins();
		
		Message m = new Message(from, message);
		
		for (String login: logins) {
			// Just add the message if the users are friends
			if (this.m_state.isFriendOf(login, from)) {
				this.m_state.addMessage(login, m);
			}
		}

		this.saveState();
		
		// Nothing to return
		return null;
	}
	
	/**
	 * API: IFacebookServer.readMessageAll
	 */
	public String readMessageAll(String token) throws FacebookException {
		String login = extractUserLogin(token);

		Vector<Message> messages = this.m_state.getUserMessages(login);
		StringBuffer sb = new StringBuffer();

		if (messages != null) {
			for (Message message : messages) {
				sb.append("From:");
				sb.append(message.getFromLogin());
			 	sb.append('\n');
			 	sb.append("Content:");
			 	sb.append(message.getMessage());
			 	sb.append('\n');
			}
		}

		return sb.toString();
	}
}
