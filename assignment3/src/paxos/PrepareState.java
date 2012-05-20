package paxos;

import java.util.Collection;
import java.util.Hashtable;

import util.SerializationUtil;

public class PrepareState {
	private Hashtable<Byte, PrepareResponse> responses;
	private PrepareRequest prepareRequest;
	
	public PrepareState(PrepareRequest prepareRequest) {
		this.prepareRequest = prepareRequest;
		this.responses = new Hashtable<Byte, PrepareResponse>();
	}
	
	public PrepareRequest getPrepareRequest() {
		return this.prepareRequest;
	}
	
	public PrepareResponse getPrepareResponse(byte hostIdentifier) {
		if (this.responses.containsKey(hostIdentifier)) {
			return this.responses.get(hostIdentifier);
		} else {
			return null;
		}
	}
	
	public void addPrepareResponse(PrepareResponse prepareResponse) {
		byte hostIdentifier = prepareResponse.getHostIdentifier();
		
		if (this.responses.containsKey(hostIdentifier)) {
			// Already received response from this node
			throw new PaxosException(PaxosException.ALREADY_RECEIVED_RESPONSE_FROM_THIS_ACCEPTOR);
		} else {
			if (prepareResponse.getRequestNumber().getValue() != this.prepareRequest.getNumber().getValue()) {
				throw new PaxosException(PaxosException.REQUEST_NUMBER_DIDNT_MATCH);
			}
			
			this.responses.put(hostIdentifier, prepareResponse);
		}
	}

	public Collection<PrepareResponse> getProposalResponses() {
		return this.responses.values();
	}
	
	@Override
	public String toString() {
		return SerializationUtil.serialize(this);
	}
}