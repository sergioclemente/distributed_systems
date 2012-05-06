package node.rpc;

public interface I2pcCoordinatorReply 
{
	void reply_notifyPrepared(int replyId, int sender, int result, String reply);
	void reply_notifyAborted(int replyId, int sender, int result, String reply);
	void reply_queryDecision(int replyId, int sender, int result, String reply);
}
