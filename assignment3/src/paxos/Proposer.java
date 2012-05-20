package paxos;

import java.util.Hashtable;

import com.google.gson.internal.Pair;

public class Proposer {
	private ProposedValues proposedValues;
	private PrepareNumber currentPrepareNumber;
	private byte hostIdentifier;
	private byte[] acceptorsHostIdentifiers;
	
	// Map from: Slot Number (Index) --> Host Identifier --> PrepareResponse
	// That is used when the value is not chosen yet
	private Hashtable<Integer, PrepareState> responses;
	
	public Proposer(byte hostIdentifier, byte[] acceptorsHostIdentifiers) {
		this.proposedValues = new ProposedValues();
		this.responses = new Hashtable<Integer, PrepareState>();
		this.currentPrepareNumber = new PrepareNumber(hostIdentifier, 0);
		
		
		this.hostIdentifier = hostIdentifier;
		this.acceptorsHostIdentifiers = acceptorsHostIdentifiers;
	}
	
	// Prepare
	public PrepareRequest createPrepareRequest(int slotNumber) {
		// Check first if we have an outstanding prepare request
		if (this.responses.containsKey(slotNumber)) {
			throw new PaxosException(PaxosException.CANNOT_CREATE_PREPARE_REQUEST_WITH_PENDING_RESPONSES);
		}
		return this.internalCreatePrepareRequest(slotNumber);
	}
	
	public PrepareRequest createPrepareRequestResend(int slotNumber) {
		// Check first if we have an outstanding prepare request
		if (!this.responses.containsKey(slotNumber)) {
			throw new PaxosException(PaxosException.CANNOT_CREATE_PREPARE_REQUEST_WITHOUT_PENDING_RESPONSES);
		}
		return this.internalCreatePrepareRequest(slotNumber);		
	}
	
	public PrepareRequest internalCreatePrepareRequest(int slotNumber) {
		// Increment current prepare number
		this.currentPrepareNumber.setSequenceNumber(this.currentPrepareNumber.getSequenceNumber()+1);
		
		PrepareRequest prepareRequest = new PrepareRequest(slotNumber, this.currentPrepareNumber.clone());
		
		// Create the prepare state
		this.responses.put(slotNumber, new PrepareState(prepareRequest));
		
		return prepareRequest;
	}
	
	public boolean processPrepareResponse(PrepareResponse prepareResponse) {
		int slotNumber = prepareResponse.getSlotNumber();
		
		if (!this.responses.containsKey(slotNumber)) {
			// Should have set the map to a non null value
			throw new PaxosException(PaxosException.INVALID_STATE_NOT_WAITING_FOR_REPARE_RESPONSE);
		}
		
		PrepareState prepareState = this.responses.get(slotNumber);
		
		prepareState.addPrepareResponse(prepareResponse);
		
		// Update the prepare to max
		this.currentPrepareNumber.setSequenceNumber(prepareResponse.getMaxNumberAcceptedSoFar().getSequenceNumber());
		
		return canProposeValue(prepareState);
	}

	private boolean canProposeValue(PrepareState proposalState) {
		// Goes through the PrepareResponses
		int majority = this.acceptorsHostIdentifiers.length/2 + 1;

		return getAcceptCount(proposalState) >= majority;
	}
	
	public boolean shouldResendPrepareRequest(PrepareRequest prepareRequest) {
		int majority = this.acceptorsHostIdentifiers.length/2 + 1;
		PrepareState state = this.responses.get(prepareRequest.getSlotNumber());
		 
		int acceptCount = this.getAcceptCount(state);
		int totalCount = this.getTotal(state);
		int remaining = this.acceptorsHostIdentifiers.length - totalCount;
		
		// Should we should the prepare when the number of acceptors + remaining
		// can never be a majority
		// TODO: it also have to consider timeouts
		return acceptCount + remaining < majority;
	}

	// TODO: I don't like the name of this function, think in a better name
	private int getAcceptCount(PrepareState proposalState) {
		int acceptCount = 0;
		PrepareNumber prepareNumber = proposalState.getPrepareRequest().getNumber();
		
		for (PrepareResponse prepareResponse : proposalState.getProposalResponses()) {
			if (prepareNumber.compareTo(prepareResponse.getMaxNumberAcceptedSoFar()) > 0) {
				acceptCount++;
			}
		}
		
		return acceptCount;
	}
	
	// TODO: I don't like the name of this function, think in a better name
	private int getTotal(PrepareState proposalState) {
		int totalCount = 0;
		PrepareNumber prepareNumber = proposalState.getPrepareRequest().getNumber();
		
		for (PrepareResponse prepareResponse : proposalState.getProposalResponses()) {
			totalCount++;
		}
		
		return totalCount;
	}
}
