package node.rpc;

public interface IFacebookServerReply 
{
	void reply_login(int replyId, int sender, int result, String reply);
	void reply_logout(int replyId, int sender, int result, String reply);
	void reply_createUser(int replyId, int sender, int result, String reply);
	void reply_addFriend_receiver(int replyId, int sender, int result, String reply);
	void reply_acceptFriend_adder(int replyId, int sender, int result, String reply);
	void reply_acceptFriend_receiver(int replyId, int sender, int result, String reply);
	void reply_writeMessageAll(int replyId, int sender, int result, String reply);
	void reply_readMessageAll(int replyId, int sender, int result, String reply);
}
