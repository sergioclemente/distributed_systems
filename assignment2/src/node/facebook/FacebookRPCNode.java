package node.facebook;

import java.util.Vector;

import node.rpc.RPCMethodCall;
import node.rpc.RPCNode;


public class FacebookRPCNode extends RPCNode {

	private BaseFacebookSystem m_system;
	
	private final static int FRONTEND_ADDRESS = 0;
	private final static int[] SHARDS_ADDRESSES = new int[] {1,2,3,4,5};
	
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
		
		if (this.addr == FRONTEND_ADDRESS) {
			this.info("Starting frontend instance on address " + this.addr);
			this.m_system = new FacebookFrontendSystem(this);
		} else {
			this.info("Starting sharding instance on address " + this.addr);
			this.m_system = new FacebookShardSystem(this);
		}
		
		this.m_system.recoverFromCrash();
	}
	
	@Override
	public void onCommand(String command)
	{
		// TODO: the queuing logic is not in place
		try {
			RPCMethodCall methodCall = this.m_system.parseRPCMethodCall(command);
			
			// If this node is the frontend?
			if (this.isFrontEnd()) {
				// Can the frontend handle this command?
				if (this.m_system.canCallLocalMethod(methodCall.getMethodName(), methodCall.getParams())) {
					this.m_system.callLocalMethod(methodCall.getMethodName(), methodCall.getParams());
				} else {
					this.routeToAppropriateShards(methodCall.getMethodName(), methodCall.getParams());
				}
			} else {
				// Shards are currently not supporting commands
				throw new FacebookException(FacebookException.CANNOT_EXECUTE_COMMANDS_ON_SHARDS);
			}
		} catch (FacebookException ex) {
			ex.printStackTrace();
		}
	}
	
	private void routeToAppropriateShards(String methodName, Vector<String> params) throws FacebookException {		
		// Validate the session
		FacebookFrontendSystem frontendSystem = (FacebookFrontendSystem)this.m_system;
		String token = params.get(0);
		User user = frontendSystem.getUser(token);
		
		// Update the first argument from token to user
		params.set(0, user.getLogin());
		
		// write_message_all will broadcast to all shards
		if (methodName.startsWith("write_message_all")) {
			for (int shardAddr : SHARDS_ADDRESSES) {
				this.callMethod(shardAddr, methodName, params);
			}			
		} else {
			// read_message_all will read from a specific shard
			int shardAddress =  user.getLogin().hashCode() % SHARDS_ADDRESSES.length;
			this.callMethod(shardAddress, methodName, params);
		}
	}
	
	@Override
	protected void onMethodCalled(int from, String methodName, Vector<String> params) {
		try {
			if (this.isFrontEnd()) {
				if (methodName.equals("endCallback")) {
					// TODO: do a better job here on handling multiple values
					this.m_system.info("Receive return from shard");
					for (int i = 0; i < params.size(); i++) {
						System.out.println(params.get(i));
					}
					
					// TODO: do not have logic of waiting for all shards in order to send next command
				} else {
					// TODO: not sure yet if need this since it would need 3 types of nodes to exercise this codepath
				}
			} else {
				assert this.m_system.canCallLocalMethod(methodName, params);
				String returnValue = this.m_system.callLocalMethod(methodName, params);
				Vector<String> returnParams = new Vector<String>();
				returnParams.add(methodName);
				returnParams.add("ok");
				returnParams.add(returnValue);
				this.callMethod(from, "endCallback", returnParams);
			}
		} catch (FacebookException ex) {
			Vector<String> returnParams = new Vector<String>();
			returnParams.add(methodName);
			returnParams.add("error");
			returnParams.add(new Integer(ex.getExceptionCode()).toString());
			
			this.callMethod(from, "endCallback", returnParams);
		}
	}
	
	@Override
	protected void onConnectionAborted(int endpoint) {
		this.m_system.info("connection aborted!");
	}
}
