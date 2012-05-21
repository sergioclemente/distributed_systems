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
			this.learners.add(new Learner(hostIdentifier++));
		}
	}
	
	public void prepare(int proposeServer, int slotNumber) {
		Proposer proposer = this.proposers.get(proposeServer);
		
		PrepareRequest request = proposer.createPrepareRequest(slotNumber);
		
		for (Acceptor acceptor : this.acceptors) {
			PrepareResponse response = acceptor.processPrepareRequest(request);
			proposer.processPrepareResponse(response);
		}
	}
	
	public void rePrepare(int proposeServer, int slotNumber) {
		Proposer proposer = this.proposers.get(proposeServer);
		
		PrepareRequest request = proposer.createRePrepareRequest(slotNumber);
		
		for (Acceptor acceptor : this.acceptors) {
			PrepareResponse response = acceptor.processPrepareRequest(request);
			proposer.processPrepareResponse(response);
		}
	}
	
	public boolean accept(int proposeServer, int slotNumber, Object value) {
		Proposer proposer = this.proposers.get(proposeServer);
		
		AcceptRequest request = proposer.createAcceptRequest(slotNumber, value);
		
		long prepareNumberValue = request.getPrepareRequest().getNumber().getValue();
		
		boolean accepted = true;
		for (Acceptor acceptor : this.acceptors) {
			AcceptResponse acceptResponse = acceptor.processAccept(request);
			accepted = accepted && acceptResponse.getMaxNumberPreparedSoFar().getValue() == prepareNumberValue;
			proposer.processAcceptResponse(acceptResponse);
		}
		
		return accepted;
	}
	
	public void learn(int acceptServer, int slotNumber) {
		Acceptor acceptor = this.acceptors.get(acceptServer);
		
		LearnRequest request = acceptor.createLearnRequest(slotNumber);

		
		for (Learner learner : this.learners) {
			learner.processAcceptResponse(request);
		}
	}
	
	public LearnedValue getLearnedValue(int learnServer, int slotNumber) {
		return this.learners.get(learnServer).getLearnedValue(slotNumber);
	}
}
