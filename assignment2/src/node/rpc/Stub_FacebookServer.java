package node.rpc;

public class Stub_FacebookServer extends RPCStub implements IFacebookServer 
{
	private IFacebookServerReply m_callback;
	
	public Stub_FacebookServer(RPCNode node, int remoteAddress, IFacebookServerReply callback)
	{
		super(node, remoteAddress);
		m_callback = callback;
	}
		
	public String login(String username, String password) 
	{
		super.invoke("login", username, password);
		return null;
	}

	public String logout(String username)
	{
		super.invoke("logout", username);
		return null;
	}

	public String createUser(String username, String password)
	{
		super.invoke("create_user", username, password);
		return null;
	}
	
	public String addFriendReceiver(String username, String friendname)
	{
		super.invoke("add_friend_receiver", username, friendname);
		return null;
	}
	
	public String acceptFriendReceiver(String username, String friendname)
	{
		super.invoke("accept_friend_receiver", username, friendname);
		return null;
	}
	
	public String acceptFriendAdder(String username, String friendname)
	{
		super.invoke("accept_friend_adder", username, friendname);
		return null;
	}
	
	public String writeMessageAll(String from, String transactionId, String message)
	{
		super.invoke("write_message_all", from, transactionId, message);
		return null;
	}
	
	public String readMessageAll(String username)
	{
		super.invoke("read_message_all", username);
		return null;
	}

	@Override
	public String dump() {
		super.invoke("dump");
		return null;
	}
	
	@Override
	protected void dispatchReply(int replyId, String methodName, int sender, int result, String content)
	{
		if (methodName.compareToIgnoreCase("login") == 0)
		{
			m_callback.reply_login(replyId, sender, result, content);
		}
		else if (methodName.compareToIgnoreCase("logout") == 0)
		{
			m_callback.reply_logout(replyId, sender, result, content);
		}
		else if (methodName.compareToIgnoreCase("create_user") == 0)
		{
			m_callback.reply_createUser(replyId, sender, result, content);
		}
		else if (methodName.compareToIgnoreCase("add_friend_receiver") == 0)
		{
			m_callback.reply_addFriend_receiver(replyId, sender, result, content);
		}
		else if (methodName.compareToIgnoreCase("accept_friend_adder") == 0)
		{
			m_callback.reply_acceptFriend_adder(replyId, sender, result, content);
		}
		else if (methodName.compareToIgnoreCase("accept_friend_receiver") == 0)
		{
			m_callback.reply_acceptFriend_receiver(replyId, sender, result, content);
		}
		else if (methodName.compareToIgnoreCase("write_message_all") == 0)
		{
			m_callback.reply_writeMessageAll(replyId, sender, result, content);
		}
		else if (methodName.compareToIgnoreCase("read_message_all") == 0)
		{
			m_callback.reply_readMessageAll(replyId, sender, result, content);
		}
		else if (methodName.compareToIgnoreCase("dump") == 0)
		{
			m_callback.reply_dump(replyId, sender, result,  null);
		}
		else 
		{
			m_node.error("Unexpected method reply: " + methodName);
		}
	}
}

