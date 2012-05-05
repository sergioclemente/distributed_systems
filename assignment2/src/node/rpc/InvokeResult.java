package node.rpc;

public class InvokeResult
{
	Integer error;
	Integer replyId;
	String content;
	
	public InvokeResult()
	{
		error = 0;
		replyId = 0;
		content = null;
	}
}
	
