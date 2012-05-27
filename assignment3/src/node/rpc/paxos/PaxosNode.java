package node.rpc.paxos;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.lang.math.RandomUtils;

import paxos.AcceptRequest;
import paxos.AcceptResponse;
import paxos.Acceptor;
import paxos.LearnRequest;
import paxos.Learner;
import paxos.PrepareRequest;
import paxos.PrepareResponse;
import paxos.Proposer;
import node.rpc.RPCNode;
import node.storage.StorageSystemServer;
import node.rpc.Skel_StorageServer;

public class PaxosNode extends RPCNode implements IAcceptorReply, IAcceptor, ILearnerReply, ILearner {
	private ProposerSystem proposerSystem;
	private AcceptorSystem acceptorSystem;
	private LearnerSystem learnerSystem;
	private StorageSystemServer storageSystem;
	
	private final int[] PROPOSER_ADDRESSES = {0,1,2}; //,3,4
	private final int[] ACCEPTOR_ADDRESSES = {0,1,2}; //{5,6,7}; //,8,9
	private final int[] LEARNER_ADDRESSES  = {0,1,2}; //{10}; // ,11,12,13,14
	
	public PaxosNode() {
		super(false); // do not use reliable transport
	}
	
	@Override
	public void start() {
		super.start();
		
		if (this.isAcceptor()) {
			this.info("Starting acceptor node on address " + this.addr);
			this.acceptorSystem = new AcceptorSystem((byte)this.addr, this);
			
			// Connect to the learners
			for (int learnerAddr : LEARNER_ADDRESSES) {
				this.acceptorSystem.connectToLearner(learnerAddr);
			}
			
			Skel_AcceptorServer skel = new Skel_AcceptorServer(this);
			skel.bindMethods(this);
		}
		
		if (this.isProposer()) {
			this.info("Starting proposer node on address " + this.addr);
			this.proposerSystem = new ProposerSystem((byte)this.addr, this);
			
			// Connect to the acceptors
			for (int acceptorAddr : ACCEPTOR_ADDRESSES) {
				this.proposerSystem.connectToAcceptor(acceptorAddr);
			}
		}
		
		if (this.isLearner()) {
			this.info("Starting learner node on address " + this.addr);
			this.learnerSystem = new LearnerSystem((byte)this.addr, this);
			
			Skel_LearnerServer skel = new Skel_LearnerServer(this);
			skel.bindMethods(this);
		}
		
		this.info("Starting storage system on address " + this.addr);
		storageSystem = new StorageSystemServer(this);
		Skel_StorageServer storageSkel = new Skel_StorageServer(storageSystem);
		storageSkel.bindMethods(this);
	}
	
	@Override
	public void onCommand(String command) {	
		String[] parts = command.split(" ");
		String methodName = parts[0].toLowerCase();
		
		if (methodName.equals("prepare")) {
			this.proposerSystem.prepare(Integer.parseInt(parts[1]));
		} else if (methodName.equals("accept")) {
			this.proposerSystem.accept(Integer.parseInt(parts[1]), parts[2]);
		} else if (methodName.equals("execute_command")) {
			this.proposerSystem.executeCommand(command.replaceFirst("execute_command ", ""));
		}
	}

	/**
	 * IAcceptor_Reply.reply_prepare()
	 */
	@Override
	public void reply_prepare(int replyId, int sender, int result, PrepareResponse response) {
		this.info("Received prepare() response from acceptor");
		this.proposerSystem.processPrepareResponse(response);
	}

	/**
	 * IAcceptor_Reply.reply_accept()
	 */
	@Override
	public void reply_accept(int replyId, int sender, int result, AcceptResponse response) {
		this.info("Received accept() response from acceptor");
		this.proposerSystem.processAcceptResponse(response);
	}

	/**
	 * ILearner_Reply.reply_learn()
	 */
	@Override
	public void reply_learn(int replyId, int sender, int result) {
		// Nothing to do...
	}
	
	/**
	 * ILearner.learn()
	 */
	@Override
	public void learn(LearnRequest request) {
		this.learnerSystem.getLearner().processLearnRequest(request);
	}
	
	/**
	 * IAcceptor.prepare()
	 */
	@Override
	public PrepareResponse prepare(PrepareRequest request) {
		return this.acceptorSystem.getAcceptor().processPrepareRequest(request);
	}

	/**
	 * IAcceptor.accept()
	 */
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
		return Arrays.binarySearch(PROPOSER_ADDRESSES, this.addr) >= 0;
	}
	
	private boolean isAcceptor() {
		return Arrays.binarySearch(ACCEPTOR_ADDRESSES, this.addr) >= 0;
	}
	
	private boolean isLearner() {
		return Arrays.binarySearch(LEARNER_ADDRESSES, this.addr) >= 0;
	}
	
	class ProposerSystem {
		private PaxosNode paxosNode;
		private Proposer proposer;
		private int currentSlot;
		private Vector<IAcceptor> acceptors = new Vector<IAcceptor>();
		private Hashtable<Integer, String> mapSlotToCommands = new Hashtable<Integer, String>();
		
		private HashSet<Integer> slotsAlreadyAccepted = new HashSet<Integer>();
 
		
		public ProposerSystem(byte hostIdentifier, PaxosNode paxosNode) {
			this.proposer = new Proposer(hostIdentifier, ACCEPTOR_ADDRESSES.length);
			this.paxosNode = paxosNode;
		}
		
		public void processAcceptResponse(AcceptResponse response) {
			this.proposer.processAcceptResponse(response);
			
			boolean accepted = response.getMaxNumberPreparedSoFar().getValue() 
					== response.getPrepareRequest().getNumber().getValue();
			
			// TODO: handle timeouts
			if (!accepted) {
				reExecuteCommand(response.getPrepareRequest());
			}
		}

		public void processPrepareResponse(PrepareResponse response) {
			boolean hasPrepareQuorum = this.proposer.processPrepareResponse(response);
			int slotNumber = response.getPrepareRequest().getSlotNumber();
			
			// TODO: handle timeouts & drops
			if (hasPrepareQuorum) {
				// Got quorum, now we can send accept requests 
				if (this.mapSlotToCommands.containsKey(slotNumber) && !this.slotsAlreadyAccepted.contains(slotNumber)) {
					this.slotsAlreadyAccepted.add(slotNumber);
					String command = this.mapSlotToCommands.get(slotNumber);
					this.accept(slotNumber, command);
				}
			} else {
				if (this.proposer.shouldResendPrepareRequest(slotNumber)) {
					this.prepare(slotNumber);
				} else {
					if (this.proposer.getFirstAcceptedValue(slotNumber) != null) {
						// try on next slot
						this.reExecuteCommand(response.getPrepareRequest());
					}
				}
			}
		}

		private void reExecuteCommand(PrepareRequest request) {
			int slotNumber = request.getSlotNumber();
			
			if (this.mapSlotToCommands.containsKey(slotNumber)) {
				String command = this.mapSlotToCommands.get(slotNumber);
				this.mapSlotToCommands.remove(slotNumber);
				this.executeCommand(command);				
			}
		}

		public void executeCommand(String command) {
			this.mapSlotToCommands.put(this.currentSlot, command);
			this.prepare(currentSlot++);
		}

		public void prepare(int slotNumber) {
			this.paxosNode.info("prepare() on slot " + slotNumber);
			PrepareRequest request = this.proposer.createPrepareRequest(slotNumber);
			
			for (IAcceptor acceptor: acceptors) {
				// The prepare return from the stub is null
				acceptor.prepare(request);
			}
		}
		
		public void accept(int slotNumber, String value) {
			this.paxosNode.info("accept() on slot " + slotNumber);
			AcceptRequest request = this.proposer.createAcceptRequest(slotNumber, value);
			
			for (IAcceptor acceptor: acceptors) {
				// accept return from the stub is null
				acceptor.accept(request);
			}
		}

		public void connectToAcceptor(int addr) {
			this.paxosNode.info("connecting to acceptor address " + addr);
			Stub_AcceptorServer stub = new Stub_AcceptorServer(this.paxosNode, addr, this.paxosNode);
			this.acceptors.add(stub);
		}

		public Proposer getProposer() {
			return this.proposer;
		}
	}
	
	class AcceptorSystem {
		private Acceptor acceptor;
		private PaxosNode paxosNode;
		private Vector<ILearner> learners = new Vector<ILearner>();
		
		public AcceptorSystem(byte hostIdentifier, PaxosNode paxosNode) {
			this.acceptor = new Acceptor(hostIdentifier, null); // TODO: missing serialization
			this.paxosNode = paxosNode;
		}
		
		public void learn(int slotNumber) {
			this.paxosNode.info("learn() on slot " + slotNumber);
			LearnRequest request = this.acceptor.createLearnRequest(slotNumber);
			
			for (ILearner learner: learners) {
				// accept return from the stub is null
				learner.learn(request);
			}
			
		}

		public void connectToLearner(int addr) {
			this.paxosNode.info("connecting to learner address " + addr);
			Stub_LearnerServer stub = new Stub_LearnerServer(this.paxosNode, addr, this.paxosNode);
			this.learners.add(stub);
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
