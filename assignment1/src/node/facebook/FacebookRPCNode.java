package node.facebook;

import java.util.Vector;
import node.rpc.RPCNode;


public class FacebookRPCNode extends RPCNode {
	private FacebookSystem m_system;
	
	private static final String ERROR_MESSAGE_FORMAT = 
			"Error: %s on facebook server %d. Returned error code was %s";
	
	public FacebookRPCNode() {
	}
	
	@Override
	public void start() {
		super.start();
		if (this.isServer()) {
			this.m_system = new FacebookSystem(this);
			// Replay the file in memory
			this.m_system.recoverFromCrash();
		}
	}
	 
	@Override
	protected void onMethodCalled(int from, String methodName, Vector<String> params) {
		if (isServer())
		{
			executeServerCommand(from, methodName, params);
		}
		else
		{
			// Make to lower
			endClientCommand(from, methodName.toLowerCase().trim(), params);
		}
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
	protected void executeClientCommand(String command)
	{
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

		this.callMethod(0, methodName, params);
	}
	
	private void user_info(String s) {
		// Use a different prefix to be easy to distinguish
		System.out.println(">>>> " + s);
	}
	
	private void endClientCommand(int from, String methodName, Vector<String> params)
	{
		if (params.size() == 2 && params.get(0).equals("error"))
		{
			user_info(String.format("NODE %d: %s", this.addr, params.get(1)));
		} else if (params.size() >= 1 && params.get(0).equals("ok")) {
			String returnValue = params.size() == 2 ? params.get(1) : null;
			
			user_info("Server returned ok. returnValue=" + returnValue);
		}
		
		// Will remove this command from the queue and executes the next one, if any
		endCommand();
	}
	
	@Override
	protected void onConnectionAborted(int endpoint) {
		if (isServer()) {
			// Nothing to do here
		} else {
			String pendingCommand;
			if ((pendingCommand = this._commandQueue.peek()) != null) {
				Vector<String> args = new Vector<String>();
				args.add("error");
				args.add(String.format(ERROR_MESSAGE_FORMAT, pendingCommand, 0, FacebookException.CONNECTION_ABORTED));
				endClientCommand(0, pendingCommand, args);
			}
		}
	}
}
