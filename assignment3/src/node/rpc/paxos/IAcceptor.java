package node.rpc.paxos;

import paxos.*;


public interface IAcceptor {
	PrepareResponse prepare(PrepareRequest request);
	AcceptResponse accept(AcceptRequest request);
}
