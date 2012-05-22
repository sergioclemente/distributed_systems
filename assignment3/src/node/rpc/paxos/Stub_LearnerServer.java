package node.rpc.paxos;

import node.rpc.RPCNode;
import node.rpc.RPCStub;
import paxos.LearnRequest;
import util.SerializationUtil;

public class Stub_LearnerServer extends RPCStub implements ILearner {
	private ILearnerReply learnerReply;
	
	public Stub_LearnerServer(RPCNode node, int remoteAddress, ILearnerReply learnerReply) {
		super(node, remoteAddress);
		this.learnerReply = learnerReply;
	}

	@Override
	protected void dispatchReply(int replyId, String methodName, int sender,
			int result, String content) {
		if (methodName.compareToIgnoreCase("learn") == 0)
		{
			learnerReply.reply_learn(replyId, sender, result);
		} else {
			m_node.error("Unexpected method reply: " + methodName);
		}
	}

	@Override
	public void learn(LearnRequest request) {
		String serializedArguments = SerializationUtil.serialize(request);
		super.invoke("learn", serializedArguments);
	}
}
