package paxos;

import util.ISerialization;

public class Acceptor {
	private byte hostIdentifier;
	private PrepareNumber maxNumberPrepared;
	private LearnedValues learnedValues;
	private ISerialization serialization;
	
	public Acceptor(byte hostIdentifier, ISerialization serialization) {
		this.hostIdentifier = hostIdentifier;
		this.serialization = serialization;
		
		if (this.serialization != null) {
			this.maxNumberPrepared = (PrepareNumber)this.serialization.restoreState("maxNumberPrepared");			
			this.learnedValues = (LearnedValues)this.serialization.restoreState("proposedValues");
		}
		
		if (this.maxNumberPrepared == null) {
			this.maxNumberPrepared = new PrepareNumber((byte)0, 0);
		}
		if (this.learnedValues == null) {
			this.learnedValues = new LearnedValues();
		}
	}

	public PrepareResponse processPrepareRequest(PrepareRequest prepareRequest) {		
		if (prepareRequest.getNumber().getValue() > this.maxNumberPrepared.getValue()) {
			this.maxNumberPrepared = prepareRequest.getNumber();
		}

		// Get any learned value (if any)
		LearnedValue learnedValue = this.learnedValues.getAt(prepareRequest.getSlotNumber());
		
		PrepareResponse prepareResponse = new PrepareResponse(this.hostIdentifier, prepareRequest, this.maxNumberPrepared.clone(), learnedValue != null ? learnedValue.getContent() : null);
		
		if (this.serialization != null) {
			this.serialization.saveState("maxNumberPrepared", this.maxNumberPrepared);
		}
		
		return prepareResponse;
	}
	
	public AcceptResponse processAccept(AcceptRequest acceptRequest) {
		PrepareRequest prepareRequest = acceptRequest.getPrepareRequest();
		
		boolean accepted = false;
		if (prepareRequest.getNumber().getValue() >= this.maxNumberPrepared.getValue()) {
			this.learnedValues.setAt(prepareRequest.getSlotNumber(), new LearnedValue(prepareRequest.getSlotNumber(), acceptRequest.getValue(), prepareRequest.getNumber()));
			
			if (this.serialization != null) {
				this.serialization.saveState("proposedValues", this.learnedValues);
			}
			accepted = true;
		}
		
		return new AcceptResponse(prepareRequest, this.maxNumberPrepared.clone(), accepted);
	}
	
	public LearnRequest createLearnRequest(int slotNumber) {
		LearnedValue learnedValue = this.learnedValues.getAt(slotNumber);
		if (learnedValue == null) {
			throw new PaxosException(PaxosException.VALUE_WAS_NOT_LEARNED);
		}
		return new LearnRequest(slotNumber, learnedValue);
	}
	
	public LearnedValue getLearnedValue(int slotNumber) {
		return this.learnedValues.getAt(slotNumber);
	}
}
