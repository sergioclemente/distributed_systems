package node.rpc;

public class Stub_2pcParticipant extends RPCStub implements I2pcParticipant 
{
	private I2pcParticipantReply m_callback;
	
	public Stub_2pcParticipant(RPCNode node, int remoteAddress, I2pcParticipantReply callback)
	{
		super(node, remoteAddress);
		m_callback = callback;
	}
		
	public String startTransaction(String coordId, String transactionId) 
	{
		super.invoke("startTransaction", coordId, transactionId);
		return null;
	}

	public String requestPrepare(String coordId, String transactionId) 
	{
		super.invoke("requestPrepare", coordId, transactionId);
		return null;
	}

	public String commitTransaction(String coordId, String transactionId) 
	{
		super.invoke("commitTransaction", coordId, transactionId);
		return null;
	}

	public String abortTransaction(String coordId, String transactionId) 
	{
		super.invoke("abortTransaction", coordId, transactionId);
		return null;
	}

	@Override
	protected void dispatchReply(int replyId, String methodName, int sender, int result, String content)
	{
		switch (methodName)
		{
		case "startTransaction":
			m_callback.reply_startTransaction(replyId, sender, result, content);
			break;
			
		case "requestPrepare":
			m_callback.reply_requestPrepare(replyId, sender, result, content);
			break;
			
		case "commitTransaction":
			m_callback.reply_commitTransaction(replyId, sender, result, content);
			break;

		case "abortTransaction":
			m_callback.reply_abortTransaction(replyId, sender, result, content);
			break;
			
		default:
			m_node.error("Unexpected method reply: " + methodName);
			break;
		}
	}

}

