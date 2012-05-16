package node.storage;
import node.rpc.RPCException;

public class StorageException extends RPCException {

	public static final int FILE_DOES_NOT_EXISTS = 10;	
	public static final int FILE_ALREADY_EXISTS = 11;
	public static final int FILE_TOO_LARGE = 30;
	
	private static final long serialVersionUID = 1773217326946381591L;

	public StorageException(int exceptionCode, String replyMessage) {
		super(exceptionCode, replyMessage);
	}

}
