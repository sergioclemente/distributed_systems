package node.rpc.paxos;

import java.util.Hashtable;

import paxos.AcceptRequest;
import paxos.AcceptResponse;
import paxos.Acceptor;
import paxos.Common;
import paxos.LearnRequest;
import paxos.Learner;
import paxos.PrepareRequest;
import paxos.PrepareResponse;
import paxos.Proposer;
import node.rpc.RPCNode;

// This framework really sucks. we have to embedd logic from acceptor and proposer in a single node... /o\
public class PaxosNode extends RPCNode implements IAcceptorReply, IAcceptor, ILearnerReply, ILearner {
	private ProposerSystem proposerSystem;
	private AcceptorSystem acceptorSystem;
	private LearnerSystem learnerSystem;
	
	@Override
	public void start() {
		if (this.isAcceptor()) {
			this.info("Starting acceptor node on address " + this.addr);
			this.acceptorSystem = new AcceptorSystem((byte)this.addr, this);
			
			// Connect to the learners
			for (int i = 10; i <= 14; i++) {
				this.acceptorSystem.connectToLearner(i);
			}
			
			Skel_AcceptorServer skel = new Skel_AcceptorServer(this);
			skel.bindMethods(this);
		}
		
		if (this.isProposer()) {
			this.info("Starting proposer node on address " + this.addr);
			this.proposerSystem = new ProposerSystem((byte)this.addr, this);
			
			// Connect to the acceptors
			for (int i = 5 ; i <= 9; i++) {
				this.proposerSystem.connectToAcceptor(i);
			}
		}
		
		if (this.isLearner()) {
			this.info("Starting learner node on address " + this.addr);
			this.learnerSystem = new LearnerSystem((byte)this.addr, this);
			
			Skel_LearnerServer skel = new Skel_LearnerServer(this);
			skel.bindMethods(this);
		}
	}
	
	@Override
	public void onCommand(String command) {	
		String[] parts = command.split(" ");
		String methodName = parts[0].toLowerCase();
		
		if (methodName.equals("prepare")) {
			this.proposerSystem.prepare(Integer.parseInt(parts[1]));
		} else if (methodName.equals("accept")) {
			this.proposerSystem.accept(Integer.parseInt(parts[1]), parts[2]);
		}
	}

	@Override
	public void reply_prepare(int replyId, int sender, int result,
			PrepareResponse response) {
		this.info("Received prepare() response from acceptor");
		this.proposerSystem.getProposer().processPrepareResponse(response);
	}

	@Override
	public void reply_accept(int replyId, int sender, int result,
			AcceptResponse response) {
		this.info("Received accept() response from acceptor");
		this.proposerSystem.getProposer().processAcceptResponse(response);
	}

	@Override
	public void reply_learn(int replyId, int sender, int result) {
		// Nothing to do...
	}
	
	@Override
	public void learn(LearnRequest request) {
		this.learnerSystem.getLearner().processLearnRequest(request);
	}
	
	@Override
	public PrepareResponse prepare(PrepareRequest request) {
		return this.acceptorSystem.getAcceptor().processPrepareRequest(request);
	}

	@Override
	public AcceptResponse accept(AcceptRequest request) {
		AcceptResponse acceptResponse = this.acceptorSystem.getAcceptor().processAccept(request);
		
		if (acceptResponse.getAccepted()) {
			// Notify the learners
			this.acceptorSystem.learn(request.getPrepareRequest().getSlotNumber());
		}
		
		return acceptResponse;
	}
	
	private boolean isProposer() {
		return this.addr >= 0 && this.addr <= 4;
	}
	
	private boolean isAcceptor() {
		return this.addr >= 5 && this.addr <= 9;
	}
	
	private boolean isLearner() {
		return this.addr >= 10 && this.addr <= 14;
	}
	
	class ProposerSystem {
		private PaxosNode paxosNode;
		private Proposer proposer;
		private Hashtable<Integer, IAcceptor> acceptors = new Hashtable<Integer, IAcceptor>();
		
		public ProposerSystem(byte hostIdentifier, PaxosNode paxosNode) {
			this.proposer = new Proposer(hostIdentifier, Common.NUMBER_OF_ACCEPTORS);
			this.paxosNode = paxosNode;
		}
		
		public void prepare(int slotNumber) {
			this.paxosNode.info("prepare() on slot " + slotNumber);
			PrepareRequest request = this.proposer.createPrepareRequest(slotNumber);
			
			for (IAcceptor acceptor: acceptors.values()) {
				// The prepare return from the stub is null
				acceptor.prepare(request);
			}
		}
		
		public void accept(int slotNumber, String value) {
			this.paxosNode.info("accept() on slot " + slotNumber);
			AcceptRequest request = this.proposer.createAcceptRequest(slotNumber, value);
			
			for (IAcceptor acceptor: acceptors.values()) {
				// accept return from the stub is null
				acceptor.accept(request);
			}
		}

		public void connectToAcceptor(int addr) {
			this.paxosNode.info("connecting to acceptor address " + addr);
			Stub_AcceptorServer stub = new Stub_AcceptorServer(this.paxosNode, addr, this.paxosNode);
			this.acceptors.put(addr, stub);
		}

		public Proposer getProposer() {
			return this.proposer;
		}
	}
	class AcceptorSystem {
		private Acceptor acceptor;
		private PaxosNode paxosNode;
		private Hashtable<Integer, ILearner> learners = new Hashtable<Integer, ILearner>();
		
		public AcceptorSystem(byte hostIdentifier, PaxosNode paxosNode) {
			this.acceptor = new Acceptor(hostIdentifier, null); // TODO: missing serialization
			this.paxosNode = paxosNode;
		}
		
		public void learn(int slotNumber) {
			this.paxosNode.info("learn() on slot " + slotNumber);
			LearnRequest request = this.acceptor.createLearnRequest(slotNumber);
			
			for (ILearner learner: learners.values()) {
				// accept return from the stub is null
				learner.learn(request);
			}
			
		}

		public void connectToLearner(int addr) {
			this.paxosNode.info("connecting to learner address " + addr);
			Stub_LearnerServer stub = new Stub_LearnerServer(this.paxosNode, addr, this.paxosNode);
			this.learners.put(addr, stub);
		}

		public Acceptor getAcceptor() {
			return this.acceptor;
		}
	}
	
	class LearnerSystem {
		private Learner learner;
		private PaxosNode paxosNode;
		
		public LearnerSystem(byte hostIdentifier, PaxosNode paxosNode) {
			this.learner = new Learner(hostIdentifier);
			this.paxosNode = paxosNode;
		}
		
		public Learner getLearner() {
			return this.learner;
		}
	}
}
