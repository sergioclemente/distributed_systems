package storage;

public class StorageException extends Exception {

	public static final int FILE_DOES_NOT_EXISTS = 10;	
	public static final int FILE_ALREADY_EXISTS = 11;
	public static final int FILE_TOO_LARGE = 30;
	
	private static final long serialVersionUID = 1773217326946381591L;

	private int _exceptionCode;
	
	public StorageException(int exceptionCode)
	{
		_exceptionCode = exceptionCode; 
	}
	
	public int getExceptionCode()
	{
		return _exceptionCode;
	}
}
