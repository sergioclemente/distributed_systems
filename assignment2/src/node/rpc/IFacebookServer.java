package node.rpc;

public interface IFacebookServer 
{
	String createUser(String username, String password) throws RPCException;
	
	String login(String username, String password) throws RPCException;
	String logout(String token) throws RPCException;
	
	String addFriendReceiver(String adderLogin, String receiverLogin) throws RPCException;
	
	String acceptFriendAdder(String token, String friendname) throws RPCException;
	String acceptFriendReceiver(String adderLogin, String receiverLogin) throws RPCException;
	
	String readMessageAll(String token) throws RPCException;
	String writeMessageAll(String from, String transactionId, String message) throws RPCException;
	
	String dump();
}
