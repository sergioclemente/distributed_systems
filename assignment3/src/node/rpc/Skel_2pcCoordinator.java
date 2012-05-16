package node.rpc;
import java.util.Vector;

public class Skel_2pcCoordinator extends RPCSkeleton  
{
	private I2pcCoordinator m_impl;
	
	/**
	 * Skel_2pcCoordinator()
	 * @param impl
	 */
	public Skel_2pcCoordinator(I2pcCoordinator impl)
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
		
		if (methodName.compareToIgnoreCase("notifyPrepared") == 0)
		{
			content = m_impl.notifyPrepared(methodArgs.get(0));
		} 
		else if (methodName.compareToIgnoreCase("notifyAborted") == 0)
		{
			content = m_impl.notifyAborted(methodArgs.get(0));
		} 
		else if (methodName.compareToIgnoreCase("queryDecision") == 0)
		{
			content = m_impl.queryDecision(methodArgs.get(0));
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
		node.bindRpcMethod("notifyPrepared", this);
		node.bindRpcMethod("notifyAborted", this);
		node.bindRpcMethod("queryDecision", this);
	}
}

