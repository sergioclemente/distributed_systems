package node.rpc.paxos;

import java.util.Vector;

import node.rpc.RPCException;
import node.rpc.RPCNode;
import node.rpc.RPCSkeleton;

import paxos.*;

import util.SerializationUtil;

public class Skel_AcceptorServer extends RPCSkeleton {
	private IAcceptor acceptor;
	
	public Skel_AcceptorServer(IAcceptor acceptor) {
		this.acceptor = acceptor;
	}
	
	@Override
	protected String invokeInternal(String methodName, Vector<String> methodArgs)
			throws RPCException {
		String content = null;
		
		if (methodName.compareToIgnoreCase("prepare") == 0)
		{
			PrepareRequest request = (PrepareRequest)SerializationUtil.deserialize(methodArgs.get(0), PrepareRequest.class);
			content = SerializationUtil.serialize(acceptor.prepare(request));
		}
		else if (methodName.compareToIgnoreCase("accept") == 0)
		{
			AcceptRequest request = (AcceptRequest)SerializationUtil.deserialize(methodArgs.get(0), AcceptRequest.class);
			content = SerializationUtil.serialize(acceptor.accept(request));
		}
		else if (methodName.compareToIgnoreCase("getAcceptedValue") == 0)
		{
			GetAcceptedValueRequest request = 
					(GetAcceptedValueRequest)SerializationUtil.deserialize(methodArgs.get(0), GetAcceptedValueRequest.class);
			acceptor.getAcceptedValue(request);
		}
		else
		{
			assert false;
		}
		
		return content;
	}

	@Override
	public void bindMethods(RPCNode node) {
		node.bindRpcMethod("prepare", this);
		node.bindRpcMethod("accept", this);
		node.bindRpcMethod("getAcceptedValue", this);
	}
}
