package node.rpc;

public class RPCException extends Exception {
	public static final int ERROR_CLASS_CLIENT = 0;
	public static final int ERROR_CLASS_PROTOCOL = 1;
	
	public static final int ERROR_PROTOCOL_TIMEOUT = 1;
	
	private int m_exceptionClass;
	private int m_exceptionCode;
	private String m_replyMessage;
	
	public RPCException(int exceptionCode, String replyMessage)
	{
		this(ERROR_CLASS_CLIENT, exceptionCode, replyMessage);
	}
	
	public RPCException(int exceptionClass, int exceptionCode, String replyMessage)
	{
		m_exceptionClass = exceptionClass;
		m_exceptionCode = exceptionCode;
		m_replyMessage = replyMessage;
	}
	
	public int getExceptionClass()
	{
		return m_exceptionClass;
	}
	
	public int getExceptionCode()
	{
		return m_exceptionCode;
	}

	public String getReplyMessage()
	{
		return m_replyMessage;
	}
	
	public static int packErrorCode(int errorClass, int errorValue)
	{
		return ((errorClass & 0xffff) << 16) | (errorValue & 0xffff);
	}
	
	public static int getErrorClass(int errorCode)
	{
		return (errorCode >> 16) & 0xffff;
	}
	
	public static int getErrorCode(int errorCode)
	{
		return (errorCode) & 0xffff;
	}
	
}
