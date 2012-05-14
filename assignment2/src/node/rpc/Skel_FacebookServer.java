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
			
		case "create_user":
			content = m_impl.createUser(methodArgs.get(0), methodArgs.get(1));
			break;
			
		case "add_friend_receiver":
			content = m_impl.addFriendReceiver(methodArgs.get(0), methodArgs.get(1));
			break;
			
		case "accept_friend_adder":
			content = m_impl.acceptFriendAdder(methodArgs.get(0), methodArgs.get(1));
			break;
			
		case "accept_friend_receiver":
			content = m_impl.acceptFriendReceiver(methodArgs.get(0), methodArgs.get(1));
			break;			
		case "write_message_all":
			content = m_impl.writeMessageAll(methodArgs.get(0), methodArgs.get(1), methodArgs.get(2));
			break;
			
		case "read_message_all":
			content = m_impl.readMessageAll(methodArgs.get(0));
			break;
			
		case "dump":
			content = m_impl.dump();
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
		node.bindRpcMethod("create_user", this);
		node.bindRpcMethod("add_friend_receiver", this);
		node.bindRpcMethod("accept_friend_adder", this);
		node.bindRpcMethod("accept_friend_receiver", this);
		node.bindRpcMethod("write_message_all", this);
		node.bindRpcMethod("read_message_all", this);
		node.bindRpcMethod("dump", this);
	}
}

