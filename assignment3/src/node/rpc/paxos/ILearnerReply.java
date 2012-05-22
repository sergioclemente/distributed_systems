package node.rpc.paxos;

import paxos.AcceptResponse;
import paxos.LearnRequest;

public interface ILearnerReply {
	void reply_learn(int replyId, int sender, int result);
}
