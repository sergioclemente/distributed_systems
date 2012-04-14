package node.facebook;

import java.util.Vector;

import node.rpc.RPCNode;

public class FacebookRPCNode extends RPCNode implements INode {
	private FacebookSystem m_system;
	private static final String ERROR_MESSAGE_FORMAT = 
			"Error: %s on facebook server %d. Returned error code was %s";
	
	public FacebookRPCNode() {
		if (this.isServer()) {
			this.m_system = new FacebookSystem(this);
		}
	}
	 
	@Override
	protected void onMethodCalled (int from, String methodName, Vector<String> params) {
		if (isServer())
		{
			executeServerCommand(from, methodName, params);
		}
		else
		{
			endClientCommand(from, methodName, params);
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
			} else if (methodName.startsWith("read_message_all")){
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
		
		String verb = parts[0];
		
		Vector<String> params = new Vector<String>();
		for (int i = 1; i < parts.length; i++) {
			params.add(parts[i]);
		}
		
		this.callMethod(0, verb, params);
	}
	
	private void endClientCommand(int from, String methodName, Vector<String> params)
	{
		if (params.size() == 2 && params.get(0).equals("error"))
		{
			error(String.format("NODE %d: %s", this.addr, params.get(1)));
		} else if (params.size() == 2 && params.get(0).equals("ok")) {
			info("Server returned: " + params.get(1));
		}
		
		// Will remove this command from the queue and executes the next one, if any
		endCommand();
	}
}
