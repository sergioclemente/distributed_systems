package node.rpc;
import java.util.Vector;
import node.facebook.FacebookException;


public abstract class RPCSkeleton  
{
	public RPCSkeleton()
	{
	}
	
	public InvokeResult invoke(String methodName, Vector<String> methodArgs)
	{
		InvokeResult result = new InvokeResult(); 
		
		try
		{
			result.replyId = Integer.parseInt(methodArgs.get(0));
			methodArgs.remove(0);
			result.content = invokeInternal(methodName, methodArgs);
			result.error = 0;
		}
		catch (FacebookException fbex)
		{
			result.error = fbex.getExceptionCode();
		}
		catch (Exception ex)
		{
			result.error = RPCNode.ERROR_UNHANDLED_EXCEPTION;
		}
		
		return result;
	}
	
	protected abstract String invokeInternal(String methodName, Vector<String> methodArgs) throws RPCException;
	
	protected abstract void BindMethods(RPCNode node);
}

