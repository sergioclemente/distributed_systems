package paxos;

import util.SerializationUtil;

public class AcceptResponse {
	private PrepareRequest prepareRequest;
	private PrepareNumber maxNumberPreparedSoFar;
	private boolean accepted;
	
	public AcceptResponse(PrepareRequest prepareRequest, PrepareNumber maxNumberPreparedSoFar, boolean accepted) {
		this.prepareRequest = prepareRequest;
		this.maxNumberPreparedSoFar = maxNumberPreparedSoFar;
		this.accepted = accepted;
	}
	public PrepareRequest getPrepareRequest() {
		return this.prepareRequest;
	}
	public PrepareNumber getMaxNumberPreparedSoFar() {
		return maxNumberPreparedSoFar;
	}
	public boolean getAccepted() {
		return this.accepted;
	}
	
	@Override
	public String toString() {
		return SerializationUtil.serialize(this);
	}
}
