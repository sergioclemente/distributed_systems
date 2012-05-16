package node.rpc;
import java.lang.reflect.Method;
import java.util.Vector;
import java.util.Hashtable;
import edu.washington.cs.cse490h.lib.Callback;
import edu.washington.cs.cse490h.lib.Utility;

public abstract class RPCStub  
{
	// Initialize s_replyID with a random base, so that there's little likelihood that pending replies 
	// will clash across crashes (otherwise a reply to call made before crash may be received as the 
	// reply of a subsequent call with the same reply ID)
	protected static Integer s_replyId = (Utility.getRNG().nextInt() & 0xffff) << 16;
	
	protected RPCNode m_node;
	protected int m_addr;
	private Method m_timeoutMethod;
	private Hashtable<Integer, String> m_pendingReplies = new Hashtable<Integer, String>();
	
	/**
	 * RPCStub()
	 */
	public RPCStub(RPCNode node, int remoteAddress)
	{
		m_node = node;
		m_addr = remoteAddress;

		try {
			m_timeoutMethod = Callback.getMethod("onInvokeTimeout", this, new String[] { "java.lang.Integer" });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static int getCurrentReplyId()
	{
		return s_replyId;
	}
	
	protected int invoke(String methodName)
	{
		Vector<String> args = new Vector<String>();
		return invokeInternal(methodName, args);
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
		
		// Wait up to 9 ticks for an RPC reply to arrive
		Callback cb = new Callback(m_timeoutMethod, this, new Object[] { s_replyId });
		m_node.addTimeout(cb, 3+6+9);
		
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
	
	public void onInvokeTimeout(Integer replyId)
	{
		if (m_pendingReplies.containsKey(replyId))
		{
			// If this reply is still pending, it means we haven't got an
			// answer for the RPC call in the past 9 ticks, so fake an
			// answer with a timeout error.
			int error = RPCException.packErrorCode(RPCException.ERROR_CLASS_PROTOCOL, RPCException.ERROR_PROTOCOL_TIMEOUT);
			onMethodReply(m_addr, replyId, error, "<null>");
		}
	}
	
	protected abstract void dispatchReply(int replyId, String methodName, int sender, int result, String content);
}

