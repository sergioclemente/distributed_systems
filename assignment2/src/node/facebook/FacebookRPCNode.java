package node.facebook;

import java.util.Vector;
import node.rpc.RPCNode;


public class FacebookRPCNode extends RPCNode {

	private FacebookSystem m_system;
	
	private final static int FRONTEND_ADDRESS = 0;
	private final static int NUMBER_OF_SHARDS = 5;
	
	private static final String ERROR_MESSAGE_FORMAT = 
			"Error: %s on facebook server %d. Returned error code was %s";
	
	public FacebookRPCNode() {
	}
	
	private boolean isFrontEnd() {
		return this.addr == FRONTEND_ADDRESS;
	}
	
	@Override
	public void start() {
		super.start();
		
		this.m_system = new FacebookSystem(this);
		this.m_system.recoverFromCrash();
	}
	 
	@Override
	protected void onMethodCalled(int from, String methodName, Vector<String> params) {
		
	}
	
	// Server part due to limitation of the framework
	private void executeServerCommand(int from, String methodName,
			Vector<String> params) {
		Vector<String> returnParams = new Vector<String>();
		String returnMethodName = "status_call";
		
		try {
			String returnValue = null;
			
			if (methodName.startsWith("create_user")) {
				this.m_system.createUser(params.get(0), params.get(1));
			} else if (methodName.startsWith("login")) {
				returnValue = this.m_system.login(params.get(0), params.get(1));
			} else if (methodName.startsWith("logout")) {
				this.m_system.logout(params.get(0));
			} else if (methodName.startsWith("add_friend")) {
				this.m_system.addFriend(params.get(0), params.get(1));
			} else if (methodName.startsWith("accept_friend")) {
				this.m_system.acceptFriend(params.get(0), params.get(1));
			} else if (methodName.startsWith("write_message_all")) { 
				this.m_system.writeMessagesAll(params.get(0), params.get(1));
			} else if (methodName.startsWith("read_message_all")) {
				returnValue = this.m_system.readMessagesAll(params.get(0));
			} else {
				throw new FacebookException(FacebookException.INVALID_FACEBOOK_METHOD);
			}
			
			returnParams.add("ok");
			if (returnValue != null) {
				returnParams.add(returnValue);
			}
		} catch (FacebookException e) {
			returnParams.add("error");
			returnParams.add(String.format(ERROR_MESSAGE_FORMAT, methodName, this.addr, e.getExceptionCode()));
		}
		
		this.callMethod(from, returnMethodName, returnParams);		
	}


	// Client part due to limitation of the framework
	@Override
	public void onCommand(String command)
	{
		if (this.isFrontEnd()) {
			String[] parts = command.split("\\s+");
			
			String methodName = parts[0];
			
			Vector<String> params = new Vector<String>();
			
			if (methodName.startsWith("write_message_all")) {
				int idx = command.indexOf(' ');
				int nextIdx = command.indexOf(' ', idx+1);
				
				if (idx != -1 && nextIdx != -1) {
					String token = command.substring(idx, nextIdx).trim();
					String msg = command.substring(nextIdx, command.length()).trim();
					params.add(token);
					params.add(msg);
				}
			} else {
				for (int i = 1; i < parts.length; i++) {
					params.add(parts[i]);
				}
			}
			
			if (isLocalCommand(methodName)) {
				// Simulate an RPC
				onMethodCalled(0, methodName, params);
			} else {
				executeOnCommand(methodName, params);	
			}
		}
	}
	
	private void executeOnCommand(String methodName, Vector<String> params) {
		try {
			String tokenId = params.get(0);
			
			User user = this.m_system.getUser(tokenId);
			
			// Replace the argument
			params.set(0, user.getLogin());

			this.callMethod(0, methodName, params);	
		} catch (FacebookException ex) {
			
		}
		
	}

	private int mapUserToShardId(String userId) {
		return userId.hashCode() % NUMBER_OF_SHARDS;
	}
	
	private boolean isLocalCommand(String methodName) {
		return methodName.equals("create_user") || 
				methodName.equals("login") || 
				methodName.equals("logout");
	}

	private void user_info(String s) {
		// Use a different prefix to be easy to distinguish
		System.out.println(">>>> " + s);
	}
	
	private void onEndCommand(int from, String methodName, Vector<String> params)
	{
		if (params.size() == 2 && params.get(0).equals("error"))
		{
			user_info(String.format("NODE %d: %s", this.addr, params.get(1)));
			user_info("Commands queued will be removed from list.");
		} else if (params.size() >= 1 && params.get(0).equals("ok")) {
			String returnValue = params.size() == 2 ? params.get(1) : null;
			user_info("Server returned ok. returnValue=" + returnValue);
		}
	}
	
	@Override
	protected void onConnectionAborted(int endpoint) {
		
	}
}
