package paxos;

import java.util.Hashtable;

import com.google.gson.internal.Pair;

public class Proposer {
	private PrepareNumber currentPrepareNumber;
	private int numberOfAcceptors;
	
	// Map from: Slot Number (Index) --> Host Identifier --> PrepareResponse
	// That is used when the value is not chosen yet
	private Hashtable<Integer, PrepareState> responses;
	
	public Proposer(byte hostIdentifier, int numberOfAcceptors) {
		this.responses = new Hashtable<Integer, PrepareState>();
		this.currentPrepareNumber = new PrepareNumber(hostIdentifier, 0);
		this.numberOfAcceptors = numberOfAcceptors;
	}
	
	// Prepare
	public PrepareRequest createPrepareRequest(int slotNumber) {
		// Increment current prepare number
		this.currentPrepareNumber.setSequenceNumber(this.currentPrepareNumber.getSequenceNumber()+1);
		
		PrepareRequest prepareRequest = new PrepareRequest(slotNumber, this.currentPrepareNumber.clone());
		
		// Create the prepare state
		this.responses.put(slotNumber, new PrepareState(prepareRequest));
		
		return prepareRequest;
	}
	
	public boolean processPrepareResponse(PrepareResponse prepareResponse) {
		int slotNumber = prepareResponse.getPrepareRequest().getSlotNumber();
		
		if (!this.responses.containsKey(slotNumber)) {
			// Should have set the map to a non null value
			throw new PaxosException(PaxosException.INVALID_STATE_NOT_WAITING_FOR_REPARE_RESPONSE);
		}
		
		PrepareState prepareState = this.responses.get(slotNumber);
		
		prepareState.addPrepareResponse(prepareResponse);
		
		// Update the prepare to max
		this.currentPrepareNumber.setSequenceNumber(prepareResponse.getMaxNumberPreparedSoFar().getSequenceNumber());
		
		return canProposeValue(prepareState);
	}

	private boolean canProposeValue(PrepareState proposalState) {
		// Goes through the PrepareResponses
		int majority = this.numberOfAcceptors/2 + 1;

		return getAcceptCount(proposalState) >= majority;
	}
	
	public boolean shouldResendPrepareRequest(int slotNumber) {
		int majority = this.numberOfAcceptors/2 + 1;
		PrepareState state = this.responses.get(slotNumber);
		 
		int acceptCount = this.getAcceptCount(state);
		int totalCount = state.getProposalResponses().size();
		int remaining = this.numberOfAcceptors - totalCount;
		
		// Should we should the prepare when the number of acceptors + remaining
		// can never be a majority
		// TODO: it also have to consider timeouts
		return haveAnyNodeAccepted(state) || acceptCount + remaining < majority;
	}

	private boolean haveAnyNodeAccepted(PrepareState proposalState) {
		return getFirstAcceptedValue(proposalState) != null;
	}

	public Object getFirstAcceptedValue(int slotNumber) {
		PrepareState state = this.responses.get(slotNumber);
		return getFirstAcceptedValue(state);
	}
	
	private Object getFirstAcceptedValue(PrepareState proposalState) {
		for (PrepareResponse prepareResponse : proposalState.getProposalResponses()) {
			if (prepareResponse.getContent() != null) {
				return prepareResponse.getContent();
			}
		}
		
		return null;
	}

	// TODO: I don't like the name of this function, think in a better name
	private int getAcceptCount(PrepareState proposalState) {
		int acceptCount = 0;
		PrepareNumber prepareNumber = proposalState.getPrepareRequest().getNumber();
		
		for (PrepareResponse prepareResponse : proposalState.getProposalResponses()) {
			if (prepareNumber.compareTo(prepareResponse.getMaxNumberPreparedSoFar()) >= 0
					&& prepareResponse.getContent() == null) {
				acceptCount++;
			}
		}
		
		return acceptCount;
	}
	
	private Object getAnyAcceptedValue(PrepareState proposalState) {
		for (PrepareResponse prepareResponse : proposalState.getProposalResponses()) {
			if (prepareResponse.getContent() != null) {
				return prepareResponse.getContent();
			}
		}
		
		return null;
	}
	
	public AcceptRequest createAcceptRequest(int slotNumber, Object value) {
		PrepareState prepareState = this.responses.get(slotNumber);
		
		if (this.canProposeValue(prepareState)) {
			// We cannot accept with a different value
			Object alreadyAcceptedValue = this.getAnyAcceptedValue(prepareState);
			if (alreadyAcceptedValue != null && !alreadyAcceptedValue.equals(value)) {
				throw new PaxosException(PaxosException.CANNOT_ACCEPT_WITH_DIFFERENT_VALUE);
			}

			return new AcceptRequest(prepareState.getPrepareRequest(), value);			
		} else {
			throw new PaxosException(PaxosException.CANNOT_CREATE_ACCEPT_REQUEST);
		}
	}
	
	public void processAcceptResponse(AcceptResponse acceptResponse) {
		// Update the sequence number
		this.currentPrepareNumber.setSequenceNumber(acceptResponse.getMaxNumberPreparedSoFar().getSequenceNumber()); 
	}
}
