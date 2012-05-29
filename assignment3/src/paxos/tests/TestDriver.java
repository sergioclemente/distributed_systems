package paxos.tests;

import java.util.Vector;

import paxos.*;

public class TestDriver {
	private Vector<Proposer> proposers;
	private Vector<Acceptor> acceptors;
	private Vector<Learner> learners;
	
	public TestDriver(int numberOfProposers, int numberOfAcceptors, int numberOfLearners) {
		this.proposers = new Vector<Proposer>();
		this.acceptors = new Vector<Acceptor>();
		this.learners = new Vector<Learner>();
		
		byte hostIdentifier = 0;		
		
		for(int i = 0 ; i < numberOfProposers; i++) {
			this.proposers.add(new Proposer(hostIdentifier++, numberOfAcceptors));
		}
		
		for (int i = 0; i< numberOfAcceptors; i++) {
			this.acceptors.add(new Acceptor(hostIdentifier++, null));
		}
		
		for (int i = 0; i< numberOfLearners; i++) {
			this.learners.add(new Learner(hostIdentifier++, this.acceptors.size()));
		}
	}
	
	private int[] generateArray(int size) {
		int[] r = new int[size];
		for(int i = 0; i < size; i++) {
			r[i] = i;
		}
		
		return r;
	}
	
	public void prepare(int proposeServer, int slotNumber) {
		prepare(proposeServer, slotNumber, generateArray(this.acceptors.size()));
	}
	
	public void prepare(int proposeServer, int slotNumber, int[] acceptServers) {
		Proposer proposer = this.proposers.get(proposeServer);
		
		PrepareRequest request = proposer.createPrepareRequest(slotNumber);
		
		for (int idx : acceptServers) {
			Acceptor acceptor = this.acceptors.get(idx);
			
			PrepareResponse response = acceptor.processPrepareRequest(request);
			proposer.processPrepareResponse(response);
		}
	}
	
	public boolean accept(int proposeServer, int slotNumber, String value) {
		return accept(proposeServer, slotNumber, value, generateArray(this.acceptors.size()));
	}
	
	
	public boolean accept(int proposeServer, int slotNumber, String value, int[] acceptServers) {
		Proposer proposer = this.proposers.get(proposeServer);
		
		AcceptRequest request = proposer.createAcceptRequest(slotNumber, new PaxosValue((byte) proposeServer, value));
		
		long prepareNumberValue = request.getPrepareRequest().getNumber().getValue();
		
		boolean accepted = true;
		for (int idx : acceptServers) {
			Acceptor acceptor = this.acceptors.get(idx);
			AcceptResponse acceptResponse = acceptor.processAccept(request);
			accepted = accepted && acceptResponse.getAccepted();
			proposer.processAcceptResponse(acceptResponse);
		}
		
		return accepted;
	}
	
	public void learn(int acceptServer, int slotNumber, String value) {
		this.learn(acceptServer, slotNumber, value, generateArray(this.learners.size()));
	}
	
	public void learn(int acceptServer, int slotNumber, String value, int[] learnServers) {
		Acceptor acceptor = this.acceptors.get(acceptServer);
		LearnRequest request = acceptor.createLearnRequest(slotNumber, new PaxosValue((byte)0, value));
		
		for (int idx : learnServers) {
			Learner learner = this.learners.get(idx);
			learner.processLearnRequest(request);
		}
	}
	
	public LearnedValue getLearnedValue(int learnServer, int slotNumber) {
		return this.learners.get(learnServer).getLearnedValue(slotNumber);
	}
}
