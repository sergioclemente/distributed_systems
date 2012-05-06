package node.facebook;
import node.rpc.RPCException;

public class FacebookException extends RPCException {
	public static final int USER_ALREADY_EXISTS = 1;	
	public static final int USER_DONT_EXIST = 2;
	public static final int SESSION_DONT_EXIST = 3;
	public static final int INVALID_FACEBOOK_METHOD = 4;
	public static final int INVALID_REQUEST = 5;
	public static final int CONNECTION_ABORTED = 6;
	public static final int CANNOT_EXECUTE_COMMANDS_ON_SHARDS = 7;
	
	public FacebookException(int exceptionCode)
	{
		super(RPCException.RPC_ERROR_CLASS_CLIENT, exceptionCode);
	}

}