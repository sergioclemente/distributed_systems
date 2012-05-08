package node.facebook;


import java.util.Vector;
import node.rpc.RPCMethodCall;

public abstract class BaseFacebookSystem {


	protected FacebookRPCNode m_node;
	
	public BaseFacebookSystem(FacebookRPCNode node) {
		this.m_node = node;
	}

	protected void user_info(String s) {
		// Use a different prefix to be easy to distinguish
		System.out.println(">>>> " + this.m_node.addr + " - "+ s);
	}
	
	protected RPCMethodCall parseRPCMethodCall(String command) {
		RPCMethodCall methodCall = new RPCMethodCall();
		
		String[] parts = command.toLowerCase().split("\\s+");
		
		String methodName = parts[0];
		Vector<String> params = new Vector<String>();
		
		// The write_message_all command is special because it can contain spaces
		// TODO: not very clean approach. Think about how making this in the subclasses
		if (methodName.startsWith("write_message_all")) {
			int idx = command.indexOf(' ');
			int nextIdx = command.indexOf(' ', idx+1);
			
			if (idx != -1 && nextIdx != -1) {
				String login = command.substring(idx, nextIdx).trim();
				String msg = command.substring(nextIdx, command.length()).trim();
				params.add(login);
				params.add(msg);
			}
		} else {
			for (int i = 1; i < parts.length; i++) {
				params.add(parts[i]);
			}
		}

		methodCall.setParams(params);
		methodCall.setMethodName(parts[0]);
		
		return methodCall;
	}

}
