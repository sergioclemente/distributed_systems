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
		
		PrepareNumber pnResponse = prepareResponse.getPrepareRequest().getNumber();
		PrepareNumber pnCurrent  = this.prepareRequest.getNumber();
			
		// If we are receiving a response for a previous prepare request (i.e. the 
		// prepare number in the response doesn't match our current prepare number)
		// we simply ignore it.
			
		if (pnResponse.getValue() != pnCurrent.getValue()) {
			// Ignore this stale response
		} else {
			this.responses.put(hostIdentifier, prepareResponse);
		}
	}

	public Collection<PrepareResponse> getProposalResponses() {
		return this.responses.values();
	}
	
	public int getHighestSequenceNumber() {
		int sn = 0;
		
		for (PrepareResponse response : responses.values()) {
			if (response.getHighestAcceptedProposalSoFar() != null && 
					response.getHighestAcceptedProposalSoFar().getSequenceNumber() > sn) {
				sn = response.getHighestAcceptedProposalSoFar().getSequenceNumber();
			}
		}
		
		return sn;
	}
	
	@Override
	public String toString() {
		return SerializationUtil.serialize(this);
	}
}