package node.rpc;

public class RPCException extends Exception {
	public static final int RPC_ERROR_CLASS_CLIENT = 0;
	public static final int RPC_ERROR_CLASS_PROTOCOL = 1;
	
	private int m_exceptionClass;
	private int m_exceptionCode;
	
	public RPCException(int exceptionCode)
	{
		m_exceptionClass = RPC_ERROR_CLASS_CLIENT;
		m_exceptionCode = exceptionCode;
	}
	
	public RPCException(int exceptionClass, int exceptionCode)
	{
		m_exceptionClass = exceptionClass;
		m_exceptionCode = exceptionCode;
	}
	
	public int getExceptionClass()
	{
		return m_exceptionClass;
	}
	
	public int getExceptionCode()
	{
		return m_exceptionCode;
	}
}
