package node.rpc;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.Vector;
import node.reliable.ReliableDeliveryNode;
import edu.washington.cs.cse490h.lib.Utility;


public class RPCNode extends ReliableDeliveryNode 
{
	private class MethodInfo
	{
		public RPCSkeleton skeleton;
		public String methodName;
		
		public MethodInfo()
		{
			this.skeleton = null;
			this.methodName = null;
		}
	}
	
	private class CallInfo
	{
		public String methodName;
		public RPCStub caller;
	}
	
	public static final int ERROR_INVALID_METHOD 		= 0x10001;
	public static final int ERROR_UNHANDLED_EXCEPTION 	= 0x10002;
	
	private Hashtable<String, MethodInfo> m_methods;
	private Hashtable<Integer, CallInfo> m_ongoingCalls;
	protected Queue<String> _commandQueue;
	private boolean useReliableTransport;
	
	
	/**
	 * RPCNode()
	 */
	public RPCNode(boolean useReliableTransport)
	{
		_commandQueue = new LinkedList<String>();
		m_methods = new Hashtable<String, MethodInfo>();
		m_ongoingCalls = new Hashtable<Integer, CallInfo>();
		
		// Using unreliable transport WILL screw up internal RPC state tracking.
		//this.useReliableTransport = useReliableTransport;
		this.useReliableTransport = true;
	}
	

	@Override
	public void start()
	{
		super.start();
	}
	

	public void callMethod(int targetSender, String methodName, Vector<String> params, Integer replyId, RPCStub caller) 
	{
		if (caller != null)
		{
			CallInfo callInfo = new CallInfo();
			callInfo.methodName = "reply_" + methodName;
			callInfo.caller = caller;
			m_ongoingCalls.put(replyId, callInfo);
		}
		
		RPCMethodCall methodCall = new RPCMethodCall(methodName, params);
		StringBuffer sb = methodCall.serialize();
		
		if (this.useReliableTransport) {
			super.sendReliableMessage(targetSender, Utility.stringToByteArray(sb.toString()));
		} else {
			super.sendUnreliableMessage(targetSender, Utility.stringToByteArray(sb.toString()));
		}
	}
	
	/**
	 * onReliableMessageReceived()
	 * Called when the next in-order message has been received.
	 * Acknowledgement and ordering of messages is handled internally by the base class.
	 */
	@Override
	protected void onReliableMessageReceived(int from, byte[] msg) 
	{
		this.onMessageReceived(from, msg);
	}
	
	@Override
	protected void onUnreliableMessageReceived(int from, byte[] msg) 
	{
		this.onMessageReceived(from, msg);
	} 
	
	private void onMessageReceived(int from, byte[] msg) {
		StringBuffer sb = new StringBuffer(Utility.byteArrayToString(msg));
		RPCMethodCall methodCall = new RPCMethodCall(sb);
		onMethodCalled(from, methodCall.getMethodName(), methodCall.getParams());
	}

	/**
	 * Classes that override this class should use this method to detect when methods are being called
	 * @param methodName
	 * @param params
	 */
	protected void onMethodCalled(int from, String methodName, Vector<String> params) 
	{
		MethodInfo mi = null;
		InvokeResult result;
		
		if (methodName.startsWith("reply_"))
		{
			//
			// This is the return value from a previous message call.
			//
			
			Integer replyId = Integer.parseInt(params.get(0));
			Integer retcode = Integer.parseInt(params.get(1));	
			String content = params.get(2);
			
			if (m_ongoingCalls.containsKey(replyId))
			{
				CallInfo callInfo = m_ongoingCalls.get(replyId);
				callInfo.caller.onMethodReply(from, replyId, retcode, content);
				m_ongoingCalls.remove(replyId);
			}
			else
			{
				// Unexpected reply. Maybe the call timed out? Log for now.
				error(String.format("Unexpected reply: id=%d, method=%s, content=[%s]", replyId, methodName, content));
			}
		}
		else
		{
			//
			// This is a remote method call to a registered impl on this node.
			//
			
			if (m_methods.containsKey(methodName))
			{
				mi = m_methods.get(methodName);
				result = mi.skeleton.invoke(methodName, params);
			}
			else
			{
				result = new InvokeResult();
				result.error = ERROR_INVALID_METHOD;
				result.replyId = Integer.parseInt(params.get(0));
				result.content = null;
			}
			
			if (result.content == null)
				result.content = "<null>";
			
			Vector<String> replyArgs = new Vector<String>();
			replyArgs.add(result.replyId.toString());
			replyArgs.add(result.error.toString());
			replyArgs.add(result.content.toString());
	
			callMethod(from, "reply_" + methodName, replyArgs, 0, null);
		}
	}

	/**
	 * onMessageTimeout()
	 * 
	 */
	@Override
	protected void onMessageTimeout(int endpoint, byte[] payload)
	{
		String content = Utility.byteArrayToString(payload);
		StringBuffer sb = new StringBuffer(content);
		RPCMethodCall methodCall = new RPCMethodCall(sb);
		
		if (methodCall.getMethodName().startsWith("reply_"))
		{
			// Timed out while sending a reply to a command.
			// Ignore for now -- the caller will timeout as well.
		}
		else
		{
			// Timed out sending a command to server.
			// Fake a failed reply.
			
			Vector<String> replyArgs = new Vector<String>();
			replyArgs.add(methodCall.getParams().get(0));
			
			// TODO: using 1 here, should use an error code that comes from elsewhere
			replyArgs.add("1");
			replyArgs.add("<null>");
			
			onMethodCalled(endpoint, "reply_" + methodCall.getMethodName(), replyArgs);
		}
	}
	
	
	/**
	 * Removes the current command from the queue and executes the next command, if there is one.
	 */
	protected void popCommandAndExecuteNext()
	{
		// Removes the head
		_commandQueue.remove();
		
		// Gets the next element in the queue (new head) and executes it
		String command = _commandQueue.peek();		
		if (command != null)
		{
			executeClientCommand(command);
		}
	}
	
	protected void executeClientCommand(String command)
	{
	}
	
	
	/**
	 * bindRpcMethod()	
	 */
	public void bindRpcMethod(String methodName, RPCSkeleton skeleton)
	{
		assert !m_methods.containsKey(methodName);
		
		MethodInfo mi = new MethodInfo();
		mi.skeleton = skeleton;
		mi.methodName = methodName;
				
		m_methods.put(methodName, mi);
	}
}

