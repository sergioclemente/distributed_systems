package paxos;

import util.ISerialization;

public class Acceptor {
	private byte hostIdentifier;
	private PrepareNumber maxNumberPrepared;
	private AcceptedValues proposedValues;
	private ISerialization serialization;
	
	public Acceptor(byte hostIdentifier, ISerialization serialization) {
		this.hostIdentifier = hostIdentifier;
		this.serialization = serialization;
		
		if (this.serialization != null) {
			this.maxNumberPrepared = (PrepareNumber)this.serialization.restoreState("maxNumberPrepared");
			this.proposedValues = (AcceptedValues)this.serialization.restoreState("proposedValues");
		} else {
			this.maxNumberPrepared = new PrepareNumber((byte)0, 0);
			this.proposedValues = new AcceptedValues();
		}
	}

	public PrepareResponse processPrepareRequest(PrepareRequest prepareRequest) {		
		if (prepareRequest.getNumber().getValue() > this.maxNumberPrepared.getValue()) {
			this.maxNumberPrepared = prepareRequest.getNumber();
		}

		PrepareResponse prepareResponse = new PrepareResponse(this.hostIdentifier, prepareRequest, this.maxNumberPrepared.clone());
		
		if (this.serialization != null) {
			this.serialization.saveState("maxNumberPrepared", this.maxNumberPrepared);
		}
		
		return prepareResponse;
	}
	
	public AcceptResponse processAccept(AcceptRequest acceptRequest) {
		PrepareRequest prepareRequest = acceptRequest.getPrepareRequest();
		
		if (prepareRequest.getNumber().getValue() >= this.maxNumberPrepared.getValue()) {
			this.proposedValues.setAt(prepareRequest.getSlotNumber(), new AcceptedValue(prepareRequest.getSlotNumber(), acceptRequest.getValue(), prepareRequest.getNumber()));
			
			if (this.serialization != null) {
				this.serialization.saveState("proposedValues", this.proposedValues);
			}
		}
		
		return new AcceptResponse(prepareRequest, this.maxNumberPrepared.clone());
	}
}
