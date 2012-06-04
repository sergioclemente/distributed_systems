package paxos;

import java.util.Hashtable;

public class Proposer {
	private int numberOfAcceptors;
	private byte hostIdentifier;
	private Hashtable<Integer, PrepareState> responses;
	private Hashtable<Integer, PrepareNumber> prepareNumbers;
	private Hashtable<Integer, AcceptState> acceptances;
	
	public Proposer(byte hostIdentifier, int numberOfAcceptors) {
		this.responses = new Hashtable<Integer, PrepareState>();
		this.hostIdentifier = hostIdentifier;
		this.numberOfAcceptors = numberOfAcceptors;
		this.prepareNumbers = new Hashtable<Integer, PrepareNumber>();
		this.acceptances = new Hashtable<Integer, AcceptState>();
	}
	
	public byte getIdentifier() {
		return this.hostIdentifier;
	}
	
	private boolean isMajority(int n) {
		return n >= (this.numberOfAcceptors/2 + 1);
	}
	
	public boolean hasEnoughResponses(int slotNumber) {
		int count = 0;
		
		if (this.responses.containsKey(slotNumber)) {
			PrepareState ps = this.responses.get(slotNumber);
			count = ps.getProposalResponses().size();
		}
		
		return this.isMajority(count);
	}
	
	public boolean hasEnoughAcceptResponses(int slotNumber) {
		int count = 0;
		
		if (this.acceptances.containsKey(slotNumber)) {
			AcceptState ps = this.acceptances.get(slotNumber);
			count = ps.getAcceptResponses().size();
		}
		
		return this.isMajority(count);
	}
	
	// Prepare
	public PrepareRequest createPrepareRequest(int slotNumber) {
		PrepareNumber pn;
		
		if (this.prepareNumbers.containsKey(slotNumber)) {
			pn = this.prepareNumbers.get(slotNumber);
			
			// Ensure the sequence number is the highest so far
			if (this.responses.containsKey(slotNumber)) {
				PrepareState ps = this.responses.get(slotNumber);
				int sn = ps.getHighestSequenceNumber();
				if (pn.getSequenceNumber() < sn) {
					pn.setSequenceNumber(sn);
				}
			}
			
			// Increment current prepare number
			pn.setSequenceNumber(pn.getSequenceNumber()+1);
		} else {
			// First time sending a prepare request for this slot
			pn = new PrepareNumber(hostIdentifier, 0);
			this.prepareNumbers.put(slotNumber, pn);
		}
		
		PrepareRequest prepareRequest = new PrepareRequest(slotNumber, pn.clone());
		
		// Create the prepare state
		this.responses.put(slotNumber, new PrepareState(prepareRequest));
		
		return prepareRequest;
	}
	
	public boolean processPrepareResponse(PrepareResponse prepareResponse) {
		int slotNumber = prepareResponse.getPrepareRequest().getSlotNumber();
		
		if (!this.responses.containsKey(slotNumber)) {
			throw new PaxosException(PaxosException.UNEXPECTED_SLOT_NUMBER);
		}
		
		PrepareState prepareState = this.responses.get(slotNumber);
		prepareState.addPrepareResponse(prepareResponse);
		
		return canProposeValue(prepareState);
	}

	private boolean canProposeValue(PrepareState proposalState) {
		int count = this.getPromiseCount(proposalState);
		return this.isMajority(count);
	}
	
	/**
	 * shouldResendPrepareRequest2() checks if the proposer is stuck in a prepare
	 * request due to the lack of responses. 
	 * If the current sequence number for the given slot number is higher than the
	 * parameter, then the proposer issued a new request since the timeout that
	 * invoked this method was scheduled, therefore the proposer is NOT stuck.
	 * If the sequence number is the same and the number of prepare replies is
	 * less than a majority, then the proposer hasn't received enough replies to
	 * know that it resubmit the request, therefore it is stuck. 
	 */
	public boolean shouldResendPrepareRequest(int slotNumber, int sequenceNumber) {
		PrepareState state = this.responses.get(slotNumber);
		PrepareNumber pn = this.prepareNumbers.get(slotNumber);
		
		if ((pn.getSequenceNumber() == sequenceNumber) && !this.isMajority(state.getProposalResponses().size())) {
			// Proposer still waiting for replies (that may never come), so should send a new prepare request
			return true;
		} else {
			// Proposer made progress since the timeout was scheduled or has enough replies to know whether 
			// or not to resubmit
			return false;
		}
	}
	
	/**
	 * getAcceptedValueIfAny() returns the PaxosValue of the highest prepare reply we've
	 * received, or null if no replies contained a PaxosValue.
	 */
	public PaxosValue getAcceptedValueIfAny(int slotNumber) {
		PaxosValue value = null;
		PrepareNumber number = null;
		PrepareState ps = this.responses.get(slotNumber);
		
		for (PrepareResponse response : ps.getProposalResponses()) {
			if (response.getContent() != null) {
				if (number == null) {
					// We haven't found a PaxosValue yet, so any will do
					number = response.getHighestAcceptedProposalSoFar();
					value = response.getContent();
				} else if (response.getHighestAcceptedProposalSoFar().compareTo(number) > 0) {
					// Only update the PaxosValue if the prepare number is higher
					number = response.getHighestAcceptedProposalSoFar();
					value = response.getContent();
				}
			}
		}
		
		return value;
	}
	
	/**
	 * getPromiseCount() returns the number of acceptors that promised to accept 
	 * our proposal.
	 */
	private int getPromiseCount(PrepareState proposalState) {
		int promiseCount = 0;
		PrepareNumber currentProposalNumber = proposalState.getPrepareRequest().getNumber();
		
		for (PrepareResponse prepareResponse : proposalState.getProposalResponses()) {
			if (prepareResponse.promised(currentProposalNumber)) {
				promiseCount++;
			}
		}
		
		return promiseCount;
	}
	
	/**
	 * getAcceptCount() returns the number of acceptors that accepted our proposal.
	 */
	private int getAcceptCount(AcceptState acceptState) {
		int acceptCount = 0;
		
		for (AcceptResponse acceptResponse : acceptState.getAcceptResponses()) {
			if (acceptResponse.getAccepted()) {
				acceptCount++;
			}
		}
		
		return acceptCount;
	}

	public AcceptRequest createAcceptRequest(int slotNumber, PaxosValue value) {
		PrepareState prepareState = this.responses.get(slotNumber);
		
		if (!this.canProposeValue(prepareState)) {
			throw new PaxosException(PaxosException.CANNOT_CREATE_ACCEPT_REQUEST);
		}

		AcceptState state = new AcceptState();
		this.acceptances.put(slotNumber, state);
		
		return new AcceptRequest(prepareState.getPrepareRequest(), value);			
	}
	
	public boolean processAcceptResponse(AcceptResponse acceptResponse) {
		int slotNumber = acceptResponse.getPrepareRequest().getSlotNumber();
		AcceptState acceptState = this.acceptances.get(slotNumber);
		acceptState.addAcceptResponse(acceptResponse);
		
		int acceptCount = getAcceptCount(acceptState);
		return isMajority(acceptCount);
	}
	
	public int getNumberOfAcceptors() {
		return this.numberOfAcceptors;
	}
	
	/**
	 * isValueChosen() is called by the ProposerSystem to verify if the chosen
	 * value matches the given one. This method is only called after the 
	 * ProposerSystem is sure that a value has been chosen, so we can skip some
	 * unnecessary validation. 
	 * For our purposes here, we don't care *which* value was chosen, we only 
	 * care about whether or not it matches the given value.
	 */
	public boolean isValueChosen(int slotNumber, PaxosValue value) {
		int matchCount = 0;
		AcceptState acceptState = this.acceptances.get(slotNumber);
	
		for (AcceptResponse acceptResponse : acceptState.getAcceptResponses()) {
			if (acceptResponse.getAccepted()) {
				PaxosValue acceptedValue = acceptResponse.getValue();
				if (value.compareTo(acceptedValue) == 0) {
					matchCount++;
				}
			}
		}
		
		return this.isMajority(matchCount); 
	}
}
