package node.facebook;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import util.NodeUtility;
import edu.washington.cs.cse490h.lib.Utility;
import node.rpc.IFacebookServer;
import edu.washington.cs.cse490h.lib.PersistentStorageReader;
import edu.washington.cs.cse490h.lib.PersistentStorageWriter;


public class FacebookShardSystem extends BaseFacebookSystem implements IFacebookServer {

	private FacebookShardState m_state = new FacebookShardState();
	private FacebookPendingState m_pendingState = null;
	private UUID m_activeTxn = null;
	private boolean m_stateImmutable = false;
	
	private static final String FILE_NAME = "facebookstate.txt";
	private static final String UNCOMMITED_STATE_FILE = "pendingstate.txt";
	
	public void recoverFromCrash() {
		this.m_state = (FacebookShardState) NodeUtility.deserializeFromFile(this.m_node, FILE_NAME, FacebookShardState.class);
		if (this.m_state == null) {
			this.m_state = new FacebookShardState();
		}
		
		this.m_pendingState = (FacebookPendingState) NodeUtility.deserializeFromFile(this.m_node, UNCOMMITED_STATE_FILE, FacebookPendingState.class);
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
		if (m_stateImmutable) {
			throw new FacebookException(FacebookException.STATE_IMMUTABLE);
		}
		
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
		if (m_stateImmutable) {
			throw new FacebookException(FacebookException.STATE_IMMUTABLE);
		}
		
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
		if (m_stateImmutable) {
			throw new FacebookException(FacebookException.STATE_IMMUTABLE);
		}
		
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
		if (m_stateImmutable) {
			throw new FacebookException(FacebookException.STATE_IMMUTABLE);
		}
		
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
		if (m_stateImmutable) {
			throw new FacebookException(FacebookException.STATE_IMMUTABLE);
		}
		
		List<String> requestList;
		requestList = this.m_state.getPendingRequest(receiverLogin);
		
		if (!requestList.contains(adderLogin)) {
			// Can't accept friendship of somebody who hasn't requested it
			throw new FacebookException(FacebookException.INVALID_REQUEST);
		}
		
		requestList.remove(adderLogin);
		this.m_state.addFriendToList(receiverLogin, adderLogin);
		
		this.saveState();
		this.user_info("(Receiver) User: " + receiverLogin + " accepted to be friends of user " + adderLogin);
		
		return null;
	}
	
	public String acceptFriendAdder(String token, String adderLogin) throws FacebookException {
		if (m_stateImmutable) {
			throw new FacebookException(FacebookException.STATE_IMMUTABLE);
		}
		
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
	public String writeMessageAll(String from, String transactionId, String message) throws FacebookException {
		if (m_stateImmutable) {
			throw new FacebookException(FacebookException.STATE_IMMUTABLE);
		}
		
		if (this.m_pendingState != null) {
			throw new FacebookException(FacebookException.CONCURRENT_TRANSACTIONS_NOT_ALLOWED, transactionId);
		}
		
		if (this.m_activeTxn != null) {
			throw new FacebookException(FacebookException.CONCURRENT_TRANSACTIONS_NOT_ALLOWED, transactionId);
		}
		
		Set<String> logins = this.m_state.getUserLogins();
		
		this.m_activeTxn = UUID.fromString(transactionId);
		this.m_pendingState = new FacebookPendingState();
		
		for (String toLogin: logins) {
			// Just add the message if the users are friends
			
			// TODO: Accessing the state which could change between writeMessageAll. Probably
			// The right way would be create a snapshot just to check the friendships
			if (this.m_state.isFriendOf(toLogin, from)) {
				this.m_pendingState.getPendingMessages().add(new Message(from, toLogin, message));
			}
		}
		
		return transactionId;
	}
	
	private boolean saveState() {
		return NodeUtility.saveStateInFile(this.m_node, this.m_state, FILE_NAME);
	}
	
	// Sequence of events
	// 1) Prepare
	// 2) Commit or Abort
	private boolean writeMessageAllPrepare() {
		// Save the pending state to disk
		boolean saved = this.savePendingState();
		
		this.m_stateImmutable = true;
		return saved;
	}
	
	public void writeMessageAllCommit() {
		if (this.m_pendingState != null) {
			// Apply pending state to real state
			for (Message m : this.m_pendingState.getPendingMessages()) {
				this.m_state.addMessage(m.getToLogin(), m);
			}
		}
		
		this.commitOrAbortCommon();
	}

	public void writeMessageAllAbort() {
		if (this.m_pendingState != null) {
			// Remove message from state
			for (Message m : this.m_pendingState.getPendingMessages()) {
				List<Message> messages = this.m_state.getUserMessages(m.getToLogin());
				
				// Remove will be a no-op if it had been removed before...
				messages.remove(m);
			}
		}
		
		this.commitOrAbortCommon();
	}
	
	private void commitOrAbortCommon() {
		if (!this.saveState()) {
			throw new RuntimeException("Cannot save state dude!");
		}
		if (!deleteTempFile()) {
			throw new RuntimeException("Cannot delete temp file dude!");
		}
		
		this.m_pendingState = null;
		this.m_stateImmutable = false;
	}
	
	private boolean savePendingState() {
		return NodeUtility.saveStateInFile(this.m_node, this.m_pendingState, UNCOMMITED_STATE_FILE);
	}
	
	private boolean deleteTempFile() {
		try {
			if (Utility.fileExists(this.m_node, UNCOMMITED_STATE_FILE)) {
				PersistentStorageWriter f = m_node.getWriter(UNCOMMITED_STATE_FILE, false);
				f.delete();
			}
			
			return true;
		} catch (IOException e) {
			return false;
		}
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
	
	/**
	 * Called by 2pc to inform the node that it must abort the 
	 * active transaction.
	 */
	public void abort(UUID transactionId)
	{
		if (m_activeTxn != null && transactionId.compareTo(m_activeTxn) == 0)
		{
			this.writeMessageAllAbort();
			m_activeTxn = null;
		}
		else 
		{
			// Unknown transaction, safe to ignore
		}
	}
	
	/**
	 * Called by 2pc to inform the node that it must commit the 
	 * active transaction.
	 */
	public void commit(UUID transactionId)
	{
		if (m_activeTxn != null && transactionId.compareTo(m_activeTxn) == 0)
		{	
			this.writeMessageAllCommit();
			m_activeTxn = null;
		}
		else
		{
			// TODO: handle this case (not sure how :p) 
		}
	}
	
	/**
	 * Called by 2pc to inform the node that it should prepare to commit
	 * the active transaction (i.e. save state in durable storage).
	 * Returns true if the state is properly saved, false otherwise.
	 */
	public boolean prepare(UUID transactionId)
	{
		if (m_activeTxn != null && transactionId.compareTo(m_activeTxn) == 0)
		{
			return this.writeMessageAllPrepare();
		}
		else
		{
			// Unknown transaction, return failure
			return false;
		}
	}



	@Override
	public String dump() {
		System.out.println(this.m_state.toString());
		
		if (this.m_pendingState != null) {
			System.out.println("PENDING");
			System.out.println(this.m_pendingState.toString());			
		}
		
		
		return null;
	}	
}
