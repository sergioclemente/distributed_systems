package paxos;

public class Acceptor {
	private PrepareNumber maxNumberPrepared;
	private byte hostIdentifier;
	
	public Acceptor(byte hostIdentifier) {
		this.hostIdentifier = hostIdentifier;
		this.maxNumberPrepared = new PrepareNumber((byte)0, 0);
	}
	
	public PrepareResponse processPrepareRequest(PrepareRequest prepareRequest) {
		PrepareResponse prepareResponse = new PrepareResponse(this.hostIdentifier, prepareRequest, maxNumberPrepared);
		
		if (prepareRequest.getNumber().getValue() > this.maxNumberPrepared.getValue()) {
			this.maxNumberPrepared = prepareRequest.getNumber();
		}
		
		return prepareResponse;
	}
}
