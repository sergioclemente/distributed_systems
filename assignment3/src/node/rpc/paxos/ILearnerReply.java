package node.rpc.paxos;

public interface ILearnerReply {
	void reply_learn(int replyId, int sender, int result);
}
