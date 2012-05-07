package node.rpc;

public interface IFacebookServer 
{
	String createUser(String username, String password) throws RPCException;
	
	String login(String username, String password) throws RPCException;
	String logout(String token) throws RPCException;
	
	String addFriend_receiver(String adderLogin, String receiverLogin) throws RPCException;
	
	String acceptFriend_adder(String token, String friendname) throws RPCException;
	String acceptFriend_receiver(String adderLogin, String receiverLogin) throws RPCException;
	
	String readMessageAll(String token) throws RPCException;
	String writeMessageAll(String username, String message) throws RPCException;
}
