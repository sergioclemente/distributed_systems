package node.rpc.paxos;

import paxos.*;

public interface IAcceptorReply {
	void reply_prepare(int replyId, int sender, int result, PrepareResponse response);
	void reply_accept(int replyId, int sender, int result, AcceptResponse response);
}
