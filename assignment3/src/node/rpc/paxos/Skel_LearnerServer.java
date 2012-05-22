package node.rpc.paxos;

import java.util.Vector;

import node.rpc.RPCException;
import node.rpc.RPCNode;
import node.rpc.RPCSkeleton;
import paxos.LearnRequest;
import util.SerializationUtil;

public class Skel_LearnerServer extends RPCSkeleton {
	private ILearner learner;
	
	public Skel_LearnerServer(ILearner learner) {
		this.learner = learner;
	}
	
	@Override
	protected String invokeInternal(String methodName, Vector<String> methodArgs)
			throws RPCException {
		String content = null;
		
		if (methodName.compareToIgnoreCase("learn") == 0)
		{
			LearnRequest request = (LearnRequest)SerializationUtil.deserialize(methodArgs.get(0), LearnRequest.class);
			this.learner.learn(request);
		}
		else
		{
			assert false;
		}
		
		return content;
	}

	@Override
	protected void bindMethods(RPCNode node) {
		node.bindRpcMethod("learn", this);
	}
}
