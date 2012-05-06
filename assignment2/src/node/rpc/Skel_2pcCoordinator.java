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
		
		switch (methodName)
		{
		case "notifyPrepared":
			content = m_impl.notifyPrepared(methodArgs.get(0));
			break;
			
		case "notifyAborted":
			content = m_impl.notifyAborted(methodArgs.get(0));
			break;
			
		case "queryDecision":
			content = m_impl.queryDecision(methodArgs.get(0));
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
		node.bindRpcMethod("notifyPrepared", this);
		node.bindRpcMethod("notifyAborted", this);
		node.bindRpcMethod("queryDecision", this);
	}
}

