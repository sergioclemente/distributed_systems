package node.facebook;

import java.util.Vector;

import node.rpc.RPCNode;

public class FacebookRPCClientNode extends RPCNode {
	@Override
	public void onCommand(String command) {
		String[] parts = command.split("\\s+");
		
		String verb = parts[0];
		
		Vector<String> params = new Vector<String>();
		for (int i = 1; i < parts.length; i++) {
			params.add(parts[i]);
		}
		
		this.callMethod(0, verb, params);
	}
	
	@Override
	protected void onMethodCalled (int from, String methodName, Vector<String> params) {
		
	}
}
