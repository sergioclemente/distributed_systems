package node.rpc.paxos;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.math.RandomUtils;

import edu.washington.cs.cse490h.lib.Callback;
import edu.washington.cs.cse490h.lib.Utility;

import paxos.AcceptRequest;
import paxos.AcceptResponse;
import paxos.Acceptor;
import paxos.GetAcceptedValueRequest;
import paxos.LearnRequest;
import paxos.Learner;
import paxos.PaxosValue;
import paxos.PrepareNumber;
import paxos.PrepareRequest;
import paxos.PrepareResponse;
import paxos.Proposer;
import util.NodeSerialization;
import node.rpc.RPCException;
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
			
			Skel_AcceptorServer skel_Acceptor = new Skel_AcceptorServer(this);
			skel_Acceptor.bindMethods(this);
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
			this.learnerSystem = new LearnerSystem((byte)this.addr, this, ACCEPTOR_ADDRESSES.length);
			
			Skel_LearnerServer skel_Learner = new Skel_LearnerServer(this);
			skel_Learner.bindMethods(this);
			
			// Connect to the acceptors
			for (int acceptorAddr : ACCEPTOR_ADDRESSES) {
				this.learnerSystem.connectToAcceptor(acceptorAddr);
			}
			
			Skel_LearnerServer skel = new Skel_LearnerServer(this);
			skel.bindMethods(this);
			
			// Storage system is co-located with the learner
			this.storageSystem = new StorageSystemServer(this);
			Skel_StorageServer skel_Storage = new Skel_StorageServer(this.storageSystem);
			skel_Storage.bindMethods(this);
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
			this.proposerSystem.accept(Integer.parseInt(parts[1]), new PaxosValue((byte)this.addr, parts[2]));
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
		boolean learned = this.learnerSystem.processLearnRequest(request);
		if (learned) {
			try	{
				this.storageSystem.executeCommand(request.getLearnedValue().getContent().getCommand());	
			} catch (RPCException ex) {
				ex.printStackTrace();
			}
		}
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
			this.acceptorSystem.learn(request.getPrepareRequest().getSlotNumber(), request.getValue());
		}
		
		return acceptResponse;
	}
	
	@Override
	public void getAcceptedValue(GetAcceptedValueRequest request) {
		this.acceptorSystem.forwardAcceptedValue(request.getSlotNumber(), request.getLearner());	
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
	
	
	public class ProposerSystem {
		private PaxosNode paxosNode;
		private Proposer proposer;
		private int currentSlot;
		private Vector<IAcceptor> acceptors = new Vector<IAcceptor>();
		private Hashtable<Integer, String> mapSlotToCommands = new Hashtable<Integer, String>();
		private HashSet<Integer> slotsAlreadyAccepted = new HashSet<Integer>();
		private Vector<String> commandQueue = new Vector<String>();
		private String currentCommand = null;
		private Method prepareTimeoutCallback;
		private Method prepareResubmitCallback;
		private Hashtable<Integer, PrepareNumber> acceptsSent = new Hashtable<Integer, PrepareNumber>();
		
		public ProposerSystem(byte hostIdentifier, PaxosNode paxosNode) {
			this.proposer = new Proposer(hostIdentifier, ACCEPTOR_ADDRESSES.length);
			this.paxosNode = paxosNode;
			
			try {
				this.prepareTimeoutCallback = Callback.getMethod("onPrepareTimeout", this, new String[] { "java.lang.Integer", "java.lang.Integer" });
				this.prepareResubmitCallback = Callback.getMethod("onPrepareResubmit", this, new String[] { "java.lang.Integer" });
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void processPrepareResponse(PrepareResponse response) {
			boolean hasPrepareQuorum = this.proposer.processPrepareResponse(response);
			int slotNumber = response.getPrepareRequest().getSlotNumber();
			
			if (hasPrepareQuorum) {
				// Got quorum, we can now send accept requests if we haven't done so yet.
				boolean sendAccept = false;
				
				if (!this.acceptsSent.containsKey(slotNumber)) {
					// First time sending accepts for this slot
					sendAccept = true;
				} else {
					PrepareNumber pnAccept = this.acceptsSent.get(slotNumber);
					PrepareNumber pnResponse = response.getPrepareRequest().getNumber();
					
					if (pnResponse.compareTo(pnAccept) > 0) {
						// Response number is higher than the last accept sent, so send again
						sendAccept = true;
					}
				}
				
				if (sendAccept) {
					// Find out the value associated with the highest numbered prepare response
					PaxosValue value = this.proposer.getAcceptedValueIfAny(slotNumber);
					
					if (value == null) {
						// No value has been proposed yet, therefore we can propose the
						// command at the front of the queue.
						value = new PaxosValue(this.proposer.getIdentifier(), this.currentCommand);
					}
	
					this.accept(slotNumber, value);
					this.acceptsSent.put(slotNumber, response.getPrepareRequest().getNumber().clone());
				}
			} else {
			
				// Here, either (1) we don't have enough responses (yet) or (2) we don't have enough promises.
				// (1): If we don't have enough responses, we don't do anything and simply keep waiting up to 
				// some timeout. 'onPrepareTimeout()' will be called when the timeout expires and, if
				// at that time we still didn't receive enough responses, we'll resend the prepare request.
				// (2): If we don't have enough promises, we are competing with another proposer for this
				// slot. We'll back out for a random time interval and then resubmit the prepare request.
				
				if (this.proposer.hasEnoughResponses(slotNumber)) {
					// If we have enough responses but not prepare quorum, we should retry after
					// a random timeout.
					Object[] args = new Object[] { slotNumber };
					Callback cb = new Callback(this.prepareResubmitCallback, this, args);
					this.paxosNode.addTimeout(cb, Utility.getRNG().nextInt(4));
				} else {
					// Not enough responses yet, so keep waiting.
				}
			}
		}

		public void processAcceptResponse(AcceptResponse response) {
			int slotNumber = response.getPrepareRequest().getSlotNumber();
			boolean hasAcceptQuorum = this.proposer.processAcceptResponse(response);
			
			if (hasAcceptQuorum) {
				// A value has been chosen. If this is the value we proposed, we can
				// move on to the next command in the queue. Otherwise, we need to
				// start a new Paxos round and try to get our current command accepted.
				
				// TODO: implement

				this.currentCommand = null;
				
			} else {
				// Here, either (1) we don't have enough accept responses (yet) or
				// (2) the proposal was not accepted.
				
				if (this.proposer.hasEnoughAcceptResponses(slotNumber)) {
					// Proposal not accepted, we should retry after a random timeout
					Object[] args = new Object[] { slotNumber };
					Callback cb = new Callback(this.prepareResubmitCallback, this, args);
					this.paxosNode.addTimeout(cb, Utility.getRNG().nextInt(4));
				} else {
					// Not enough responses yet, so keep waiting.
				}
			}
				
		}

		private void reExecuteCommand(PrepareRequest request) {
			int slotNumber = request.getSlotNumber();
			
			// TODO-luciano: review and fix
			if (this.mapSlotToCommands.containsKey(slotNumber)) {
				String command = this.mapSlotToCommands.get(slotNumber);
				this.mapSlotToCommands.remove(slotNumber);
				this.executeCommand(command);				
			}
		}

		public void executeCommand(String command) {
			this.mapSlotToCommands.put(this.currentSlot, command);
			
			// Queue the current command.
			// Commands are processed one at a time in FIFO order, so that serialization
			// is preserved among commands sent to the same proposer.
			this.commandQueue.add(command);
			
			if (this.currentCommand == null) {
				// Get the next command from the queue
				this.currentCommand = this.commandQueue.remove(0);
				
				// Start a new Paxos round for this command
				this.prepare(this.currentSlot++);
			}
		}

		public void prepare(int slotNumber) {
			this.paxosNode.info("prepare() on slot " + slotNumber);
			PrepareRequest request = this.proposer.createPrepareRequest(slotNumber);
			
			for (IAcceptor acceptor: acceptors) {
				// The prepare return from the stub is null
				acceptor.prepare(request);
			}
			
			// Schedule a timeout so that we can issue a new prepare request
			// if not enough responses are received.
			// We use an arbitrary timeout of 8 + random(4) ticks.
			Object[] args = new Object[] { slotNumber, request.getNumber().getSequenceNumber() };
			Callback cb = new Callback(this.prepareTimeoutCallback, this, args);
			this.paxosNode.addTimeout(cb, 8 + Utility.getRNG().nextInt(4));
		}
		
		public void accept(int slotNumber, PaxosValue value) {
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
		
		/**
		 * onPrepareTimeout() is called after some period of time so that we can check the status
		 * of the given slot and make sure that the prepare request made progress (i.e. it 
		 * received enough replies to either re-submit or move to the accept phase).
		 */
		public void onPrepareTimeout(Integer slotNumber, Integer sequenceNumber) {
			if (this.proposer.shouldResendPrepareRequest2(slotNumber, sequenceNumber)) {
				this.prepare(slotNumber);
			}
		}
		
		/**
		 * onPrepareResubmit() is called after a random time interval when we decided that we
		 * should resubmit the given prepare request.
		 */
		public void onPrepareResubmit(Integer slotNumber) {
			this.prepare(slotNumber);
		}
	}
	
	public class AcceptorSystem {
		private Acceptor acceptor;
		private PaxosNode paxosNode;
		private Map<Integer, ILearner> learners = new HashMap<Integer, ILearner>();
		
		public AcceptorSystem(byte hostIdentifier, PaxosNode paxosNode) {
			this.acceptor = new Acceptor(hostIdentifier, new NodeSerialization(paxosNode, "serialization.txt"));
			this.paxosNode = paxosNode;
		}
		
		public void forwardAcceptedValue(int slotNumber, int learner) {
			LearnRequest request = this.acceptor.createLearnRequest(slotNumber);
			
			if (learners.containsKey(learner)) {
				learners.get(learner).learn(request);
			}
		}

		public void learn(int slotNumber, PaxosValue value) {
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
	
	public class LearnerSystem {
		private Learner learner;
		private PaxosNode paxosNode;
		private Vector<IAcceptor> acceptors = new Vector<IAcceptor>();
		
		public LearnerSystem(byte hostIdentifier, PaxosNode paxosNode, int numberOfAcceptors) {
			this.learner = new Learner(hostIdentifier, numberOfAcceptors);
			this.paxosNode = paxosNode;
		}

		public boolean processLearnRequest(LearnRequest request) {
			int currentSlotNumber = request.getSlotNumber();					
			
			for (int i = currentSlotNumber - 1; i >= 0; i--) {
				if (this.learner.shouldStartLearningProcess(i)) {
					startLearningProcess(i);
				}
			}
			
			return this.learner.processLearnRequest(request);
		}
		
		public void connectToAcceptor(int addr) {
			this.paxosNode.info("connecting to acceptor address " + addr);
			Stub_AcceptorServer stub = new Stub_AcceptorServer(this.paxosNode, addr, this.paxosNode);
			this.acceptors.add(stub);
		}
		
		private void startLearningProcess(int slotNumber) {
			GetAcceptedValueRequest request = new GetAcceptedValueRequest(slotNumber, paxosNode.addr);
			
			for (IAcceptor acceptor : acceptors) {
				acceptor.getAcceptedValue(request);
			}
		}
	}	
}
