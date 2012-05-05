package node.rpc;

public interface IFacebookServerReply 
{
	void reply_login(int replyId, int sender, int result, String reply);
	void reply_logout(int replyId, int sender, int result, String reply);
	void reply_createUser(int replyId, int sender, int result, String reply);
	void reply_addFriend(int replyId, int sender, int result, String reply);
	void reply_acceptFriend(int replyId, int sender, int result, String reply);
	void reply_writeMessageOne(int replyId, int sender, int result, String reply);
	void reply_writeMessageAll(int replyId, int sender, int result, String reply);
	void reply_readMessageAll(int replyId, int sender, int result, String reply);
}
