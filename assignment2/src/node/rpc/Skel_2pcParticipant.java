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
	
		if (methodName.compareToIgnoreCase("startTransaction") == 0)
		{
			content = m_impl.startTransaction(methodArgs.get(0), methodArgs.get(1));
		}
		else if (methodName.compareToIgnoreCase("requestPrepare") == 0)
		{
			content = m_impl.requestPrepare(methodArgs.get(0), methodArgs.get(1));
		}
		else if (methodName.compareToIgnoreCase("commitTransaction") == 0)
		{
			content = m_impl.commitTransaction(methodArgs.get(0), methodArgs.get(1));
		}
		else if (methodName.compareToIgnoreCase("abortTransaction") == 0)
		{
			content = m_impl.abortTransaction(methodArgs.get(0), methodArgs.get(1));
		}
		else 
		{
			assert false;
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
	}
}

