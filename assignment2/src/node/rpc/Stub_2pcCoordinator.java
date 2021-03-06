package node.rpc;

public class Stub_2pcCoordinator extends RPCStub implements I2pcCoordinator 
{
	private I2pcCoordinatorReply m_callback;
	
	public Stub_2pcCoordinator(RPCNode node, int remoteAddress, I2pcCoordinatorReply callback)
	{
		super(node, remoteAddress);
		m_callback = callback;
	}
		
	public String notifyPrepared(String transactionId) 
	{
		super.invoke("notifyPrepared", transactionId);
		return null;
	}

	public String notifyAborted(String transactionId) 
	{
		super.invoke("notifyAborted", transactionId);
		return null;
	}

	public String queryDecision(String transactionId) 
	{
		super.invoke("queryDecision", transactionId);
		return null;
	}

	@Override
	protected void dispatchReply(int replyId, String methodName, int sender, int result, String content)
	{
		if (methodName.compareToIgnoreCase("notifyPrepared") == 0)
		{
			m_callback.reply_notifyPrepared(replyId, sender, result, content);
		}
		else if (methodName.compareToIgnoreCase("notifyAborted") == 0)
		{
			m_callback.reply_notifyAborted(replyId, sender, result, content);
		}
		else if (methodName.compareToIgnoreCase("queryDecision") == 0)
		{
			m_callback.reply_queryDecision(replyId, sender, result, content);
		}
		else
		{
			m_node.error("Unexpected method reply: " + methodName);
		}
	}

}

