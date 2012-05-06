package node.rpc;

public interface IFacebookServer 
{
	String createUser(String username, String password) throws RPCException;
	String login(String username, String password) throws RPCException;
	String logout(String username) throws RPCException;
	String addFriend(String username, String friendname) throws RPCException;
	String acceptFriend(String username, String friendname) throws RPCException;
	String writeMessageAll(String username, String message) throws RPCException;
	String readMessageAll(String username) throws RPCException;
}
