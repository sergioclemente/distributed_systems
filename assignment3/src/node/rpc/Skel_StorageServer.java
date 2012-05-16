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
		
		switch (methodName)
		{
		case "createFile":
			content = m_impl.createFile(methodArgs.get(0));
			break;
			
		case "getFile":
			content = m_impl.getFile(methodArgs.get(0));
			break;
			
		case "putFile":
			content = m_impl.putFile(methodArgs.get(0), methodArgs.get(1));
			break;
			
		case "appendToFile":
			content = m_impl.appendToFile(methodArgs.get(0), methodArgs.get(1));
			break;
			
		case "deleteFile":
			content = m_impl.deleteFile(methodArgs.get(0));
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
		node.bindRpcMethod("createFile", this);
		node.bindRpcMethod("getFile", this);
		node.bindRpcMethod("putFile", this);
		node.bindRpcMethod("appendToFile", this);
		node.bindRpcMethod("deleteFile", this);
	}
}


