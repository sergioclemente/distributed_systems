package node.rpc.paxos;

import node.rpc.RPCNode;
import node.rpc.RPCStub;
import paxos.*;
import util.SerializationUtil;

public class Stub_AcceptorServer extends RPCStub implements IAcceptor {
	private IAcceptorReply acceptorReply;
	
	public Stub_AcceptorServer(RPCNode node, int remoteAddress, IAcceptorReply acceptorReply) {
		super(node, remoteAddress);
		this.acceptorReply = acceptorReply;
	}

	@Override
	public PrepareResponse prepare(PrepareRequest request) {
		String serializedArguments = SerializationUtil.serialize(request);
		super.invoke("prepare", serializedArguments);
		return null;
	}


	@Override
	public AcceptResponse accept(AcceptRequest request) {
		String serializedArguments = SerializationUtil.serialize(request);
		super.invoke("accept", serializedArguments);
		return null;
	}
	
	@Override
	public void getAcceptedValue(GetAcceptedValueRequest request) {
		String serializedArguments = SerializationUtil.serialize(request);
		super.invoke("getAcceptedValue", serializedArguments);
	}

	@Override
	protected void dispatchReply(int replyId, String methodName, int sender,
			int result, String content) {
		if (methodName.compareToIgnoreCase("accept") == 0)
		{
			AcceptResponse response = (AcceptResponse)SerializationUtil.deserialize(content, AcceptResponse.class);
			
			acceptorReply.reply_accept(replyId, sender, result, response);
		} else if (methodName.compareToIgnoreCase("prepare") == 0) {
			PrepareResponse response = (PrepareResponse)SerializationUtil.deserialize(content, PrepareResponse.class);
			acceptorReply.reply_prepare(replyId, sender, result, response);
		} else {
			m_node.error("Unexpected method reply: " + methodName);
		}
	}	
}
