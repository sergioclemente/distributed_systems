package node.rpc;

public interface I2pcParticipantReply 
{
	void reply_startTransaction(int replyId, int sender, int result, String reply);
	void reply_requestPrepare(int replyId, int sender, int result, String reply);
	void reply_commitTransaction(int replyId, int sender, int result, String reply);
	void reply_abortTransaction(int replyId, int sender, int result, String reply);
	
	void reply_backcompat(int replyId, int sender, int result, String reply);
}
