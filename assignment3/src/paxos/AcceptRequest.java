package paxos;

import util.SerializationUtil;

public class AcceptRequest {
	private PrepareRequest prepareRequest;
	private PaxosValue value;
	
	public AcceptRequest(PrepareRequest prepareRequest, PaxosValue value) {
		this.prepareRequest = prepareRequest;
		this.value = value;
	}

	public PrepareRequest getPrepareRequest() {
		return prepareRequest;
	}

	public PaxosValue getValue() {
		return this.value;
	}
	
	@Override
	public String toString() {
		return SerializationUtil.serialize(this);
	}
}
