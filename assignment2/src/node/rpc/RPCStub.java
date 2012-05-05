package node.rpc;

import java.util.Vector;
import java.util.Hashtable;

public abstract class RPCStub  
{
	protected RPCNode m_node;
	protected int m_addr;
	protected static Integer s_replyId = 0;
	private Hashtable<Integer, String> m_pendingReplies = new Hashtable<Integer, String>();
	
	/**
	 * RPCStub()
	 */
	public RPCStub(RPCNode node, int remoteAddress)
	{
		m_node = node;
		m_addr = remoteAddress;
	}
	
	public int getCurrentReplyId()
	{
		return s_replyId;
	}
	
	protected int invoke(String methodName, String arg1)
	{
		Vector<String> args = new Vector<String>();
		args.add(arg1);
		return invokeInternal(methodName, args);
	}
	
	protected int invoke(String methodName, String arg1, String arg2)
	{
		Vector<String> args = new Vector<String>();
		args.add(arg1);
		args.add(arg2);
		return invokeInternal(methodName, args);
	}
	
	protected int invoke(String methodName, String arg1, String arg2, String arg3)
	{
		Vector<String> args = new Vector<String>();
		args.add(arg1);
		args.add(arg2);
		args.add(arg3);
		return invokeInternal(methodName, args);
	}
	
	private int invokeInternal(String methodName, Vector<String> methodArgs)
	{
		s_replyId++;
		methodArgs.insertElementAt(s_replyId.toString(), 0);
		m_node.callMethod(m_addr, methodName, methodArgs, s_replyId, this);
		m_pendingReplies.put(s_replyId, methodName);
		
		return s_replyId;
	}
	
	public void onMethodReply(int sender, Integer replyId, int result, String content)
	{
		if (m_pendingReplies.containsKey(replyId))
		{
			String methodName = m_pendingReplies.get(replyId);
			m_pendingReplies.remove(replyId);
			dispatchReply(replyId, methodName, sender, result, content);
		}
		else
		{
			// Unexpected reply
			m_node.error(String.format("Unexpected reply id: %d", replyId));
		}
	}
	
	
	protected abstract void dispatchReply(int replyId, String methodName, int sender, int result, String content);
}

