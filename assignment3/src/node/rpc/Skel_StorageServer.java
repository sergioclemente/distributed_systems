package node.rpc;
import java.util.Vector;

public class Skel_StorageServer extends RPCSkeleton 
{
	private IStorageServer m_impl;
	
	/**
	 * Skel_StorageServer()
	 * @param impl
	 */
	public Skel_StorageServer(IStorageServer impl)
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
		
		if (methodName.compareToIgnoreCase("createFile") == 0)
		{
			content = m_impl.createFile(methodArgs.get(0));
		}
		else if (methodName.compareToIgnoreCase("getFile") == 0)
		{
			content = m_impl.getFile(methodArgs.get(0));
		}
		else if (methodName.compareToIgnoreCase("putFile") == 0)
		{
			content = m_impl.putFile(methodArgs.get(0), methodArgs.get(1));
		}
		else if (methodName.compareToIgnoreCase("appendToFile") == 0)
		{
			content = m_impl.appendToFile(methodArgs.get(0), methodArgs.get(1));
		}
		else if (methodName.compareToIgnoreCase("deleteFile") == 0)
		{
			content = m_impl.deleteFile(methodArgs.get(0));
		}
		else 
		{
			assert false;
		}
		
		return content;
	}
	
	@Override
	public void bindMethods(RPCNode node)
	{
		node.bindRpcMethod("createFile", this);
		node.bindRpcMethod("getFile", this);
		node.bindRpcMethod("putFile", this);
		node.bindRpcMethod("appendToFile", this);
		node.bindRpcMethod("deleteFile", this);
	}
}


