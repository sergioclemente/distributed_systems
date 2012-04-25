package node.facebook;

public class FacebookException extends Exception {
	public static final int USER_ALREADY_EXISTS = 1;	
	public static final int USER_DONT_EXIST = 2;
	public static final int SESSION_DONT_EXIST = 3;
	public static final int INVALID_FACEBOOK_METHOD = 4;
	public static final int INVALID_REQUEST = 5;
	public static final int CONNECTION_ABORTED = 6;
	public static final int CANNOT_EXECUTE_COMMANDS_ON_SHARDS = 7;
	
	private int m_exceptionCode;
	
	public FacebookException(int exceptionCode)
	{
		m_exceptionCode = exceptionCode; 
	}
	
	public int getExceptionCode()
	{
		return m_exceptionCode;
	}
}
