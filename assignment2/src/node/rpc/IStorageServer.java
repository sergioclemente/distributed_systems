package node.rpc;

public interface IStorageServer
{
	String createFile(String fileName) throws RPCException;
	String getFile(String fileName) throws RPCException;
	String putFile(String fileName, String contents) throws RPCException;
	String appendToFile(String fileName, String contents) throws RPCException;
	String deleteFile(String fileName) throws RPCException;
}
