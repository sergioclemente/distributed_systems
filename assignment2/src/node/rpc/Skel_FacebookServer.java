package node.rpc;
import java.util.Vector;

public class Skel_FacebookServer extends RPCSkeleton  
{
	private IFacebookServer m_impl;
	
	/**
	 * Skel_FacebookServer()
	 * @param impl
	 */
	public Skel_FacebookServer(IFacebookServer impl)
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
		case "login":
			content = m_impl.login(methodArgs.get(0), methodArgs.get(1));
			break;
			
		case "logout":
			content = m_impl.logout(methodArgs.get(0));
			break;
			
		case "createUser":
			content = m_impl.createUser(methodArgs.get(0), methodArgs.get(1));
			break;
			
		case "addFriend":
			content = m_impl.addFriend(methodArgs.get(0), methodArgs.get(1));
			break;
			
		case "acceptFriend":
			content = m_impl.acceptFriend(methodArgs.get(0), methodArgs.get(1));
			break;
			
		case "writeMessageOne":
			content = m_impl.writeMessageOne(methodArgs.get(0), methodArgs.get(1), methodArgs.get(2));
			break;
			
		case "writeMessageAll":
			content = m_impl.writeMessageAll(methodArgs.get(0), methodArgs.get(1));
			break;
			
		case "readMessageAll":
			content = m_impl.readMessageAll(methodArgs.get(0));
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
		node.bindRpcMethod("login", this);
		node.bindRpcMethod("logout", this);
		node.bindRpcMethod("createUser", this);
		node.bindRpcMethod("addFriend", this);
		node.bindRpcMethod("acceptFriend", this);
		node.bindRpcMethod("writeMessageOne", this);
		node.bindRpcMethod("writeMessageAll", this);
		node.bindRpcMethod("readMessageAll", this);
	}
}

