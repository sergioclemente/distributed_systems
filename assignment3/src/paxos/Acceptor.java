package paxos;

// This is a mockup acceptor class. i didn't verified the logic with tests yet
public class Acceptor {
	private byte hostIdentifier;
	private PrepareNumber maxNumberPrepared;
	private AcceptedValues proposedValues;
	
	public Acceptor(byte hostIdentifier) {
		this.hostIdentifier = hostIdentifier;
		this.maxNumberPrepared = new PrepareNumber((byte)0, 0);
		this.proposedValues = new AcceptedValues();
	}
	
	public PrepareResponse processPrepareRequest(PrepareRequest prepareRequest) {
		PrepareResponse prepareResponse = new PrepareResponse(this.hostIdentifier, prepareRequest, maxNumberPrepared);
		
		if (prepareRequest.getNumber().getValue() > this.maxNumberPrepared.getValue()) {
			this.maxNumberPrepared = prepareRequest.getNumber();
		}
		
		return prepareResponse;
	}
	
	public boolean processAccept(AcceptRequest acceptRequest) {
		PrepareRequest prepareRequest = acceptRequest.getPrepareRequest();
		
		if (prepareRequest.getNumber().getValue() >= this.maxNumberPrepared.getValue()) {
			this.proposedValues.setAt(prepareRequest.getSlotNumber(), new AcceptedValue(prepareRequest.getSlotNumber(), acceptRequest.getValue(), prepareRequest.getNumber()));
			
			return true;
		} else {
			return false;
		}
	}
}
