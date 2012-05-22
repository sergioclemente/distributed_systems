package node.rpc.paxos;

import paxos.LearnRequest;

public interface ILearner {
	void learn(LearnRequest request);
}
