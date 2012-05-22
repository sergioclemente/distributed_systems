package paxos;

import util.SerializationUtil;

public class AcceptRequest {
	private PrepareRequest prepareRequest;
	private Object value;
	
	public AcceptRequest(PrepareRequest prepareRequest, Object value) {
		this.prepareRequest = prepareRequest;
		this.value = value;
	}

	public PrepareRequest getPrepareRequest() {
		return prepareRequest;
	}

	public Object getValue() {
		return this.value;
	}
	
	@Override
	public String toString() {
		return SerializationUtil.serialize(this);
	}
}
