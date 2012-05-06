package node.rpc;
import java.util.Vector;

public class Skel_2pcParticipant extends RPCSkeleton  
{
	private I2pcParticipant m_impl;
	
	/**
	 * Skel_2pcParticipant()
	 * @param impl
	 */
	public Skel_2pcParticipant(I2pcParticipant impl)
	{
		m_impl = impl;
	}
		
	/**
	 * invokeInternal()
	 */
	@Override
	protected String invokeInternal(String methodName, Vector<String> methodArgs) throws RPCException
	{
		String content = null;
		
		switch (methodName)
		{
		case "startTransaction":
			content = m_impl.startTransaction(methodArgs.get(0));
			break;
			
		case "requestPrepare":
			content = m_impl.requestPrepare(methodArgs.get(0));
			break;
			
		case "commitTransaction":
			content = m_impl.commitTransaction(methodArgs.get(0));
			break;
			
		case "abortTransaction":
			content = m_impl.abortTransaction(methodArgs.get(0));
			break;
			
		case "backcompat":
			String orig = "";
			for (String str : methodArgs)
				orig += str + " ";
			content = m_impl.backcompat(orig.trim());
			break;

		default:
			assert false;
			break;
		}
		
		return content;
	}
	
	@Override
	protected void BindMethods(RPCNode node)
	{
		node.bindRpcMethod("startTransaction", this);
		node.bindRpcMethod("requestPrepare", this);
		node.bindRpcMethod("commitTransaction", this);
		node.bindRpcMethod("abortTransaction", this);
		node.bindRpcMethod("backcompat", this);
	}
}

