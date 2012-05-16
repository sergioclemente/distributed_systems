package node.rpc;

public class Stub_StorageServer extends RPCStub implements IStorageServer 
{
	private IStorageServerReply m_callback;
	
	public Stub_StorageServer(RPCNode node, int remoteAddress, IStorageServerReply callback)
	{
		super(node, remoteAddress);
		m_callback = callback;
	}
		
	public String createFile(String fileName) 
	{
		super.invoke("createFile", fileName); 
		return null;
	}

	public String getFile(String fileName)
	{
		super.invoke("getFile", fileName);
		return null;
	}

	public String putFile(String fileName, String content)
	{
		super.invoke("putFile", fileName, content);
		return null;
	}
	
	public String appendToFile(String fileName, String content)
	{
		super.invoke("appendToFile", fileName, content);
		return null;
	}
	
	public String deleteFile(String fileName)
	{
		super.invoke("deleteFile", fileName);
		return null;
	}
	
	@Override
	protected void dispatchReply(int replyId, String methodName, int sender, int result, String content)
	{
		switch (methodName)
		{
		case "createFile":
			m_callback.reply_createFile(replyId, sender, result, content);
			break;
			
		case "getFile":
			m_callback.reply_getFile(replyId, sender, result, content);
			break;
			
		case "putFile":
			m_callback.reply_putFile(replyId, sender, result, content);
			break;

		case "appendToFile":
			m_callback.reply_appendToFile(replyId, sender, result, content);
			break;
			
		case "deleteFile":
			m_callback.reply_deleteFile(replyId, sender, result, content);
			break;
			
		default:
			m_node.error("Unexpected method reply: " + methodName);
			break;
		}
	}

}

