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
		super.invoke("createUser", username, password);
		return null;
	}
	
	public String addFriend(String username, String friendname)
	{
		super.invoke("addFriend", username, friendname);
		return null;
	}
	
	public String acceptFriend(String username, String friendname)
	{
		super.invoke("acceptFriend", username, friendname);
		return null;
	}

	public String writeMessageOne(String username, String friendname, String message)
	{
		super.invoke("writeMessageOne", username, friendname, message);
		return null;
	}
	
	public String writeMessageAll(String username, String message)
	{
		super.invoke("writeMessageAll", username, message);
		return null;
	}
	
	public String readMessageAll(String username)
	{
		super.invoke("readMessageAll", username);
		return null;
	}

	
	@Override
	protected void dispatchReply(int replyId, String methodName, int sender, int result, String content)
	{
		switch (methodName)
		{
		case "login":
			m_callback.reply_login(replyId, sender, result, content);
			break;
			
		case "logout":
			m_callback.reply_logout(replyId, sender, result, content);
			break;
			
		case "createUser":
			m_callback.reply_createUser(replyId, sender, result, content);
			break;

		case "addFriend":
			m_callback.reply_addFriend(replyId, sender, result, content);
			break;
			
		case "acceptFriend":
			m_callback.reply_acceptFriend(replyId, sender, result, content);
			break;
			
		case "writeMessageOne":
			m_callback.reply_writeMessageOne(replyId, sender, result, content);
			break;
			
		case "writeMessageAll":
			m_callback.reply_writeMessageAll(replyId, sender, result, content);
			break;
			
		case "readMessageAll":
			m_callback.reply_readMessageAll(replyId, sender, result, content);
			break;
			
		default:
			m_node.error("Unexpected method reply: " + methodName);
			break;
		}
	}

}

