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
		
		if (methodName.compareToIgnoreCase("login") == 0)
		{
			content = m_impl.login(methodArgs.get(0), methodArgs.get(1));
		}
		else if (methodName.compareToIgnoreCase("logout") == 0)
		{
			content = m_impl.logout(methodArgs.get(0));
		}
		else if (methodName.compareToIgnoreCase("create_user") == 0)
		{
			content = m_impl.createUser(methodArgs.get(0), methodArgs.get(1));
		}
		else if (methodName.compareToIgnoreCase("add_friend_receiver") == 0)
		{
			content = m_impl.addFriendReceiver(methodArgs.get(0), methodArgs.get(1));
		}
		else if (methodName.compareToIgnoreCase("accept_friend_adder") == 0)
		{
			content = m_impl.acceptFriendAdder(methodArgs.get(0), methodArgs.get(1));
		}
		else if (methodName.compareToIgnoreCase("accept_friend_receiver") == 0)
		{
			content = m_impl.acceptFriendReceiver(methodArgs.get(0), methodArgs.get(1));
		}
		else if (methodName.compareToIgnoreCase("write_message_all") == 0)
		{
			content = m_impl.writeMessageAll(methodArgs.get(0), methodArgs.get(1), methodArgs.get(2));
		}
		else if (methodName.compareToIgnoreCase("read_message_all") == 0)
		{
			content = m_impl.readMessageAll(methodArgs.get(0));
		}
		else if (methodName.compareToIgnoreCase("dump") == 0)
		{
			content = m_impl.dump();
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

