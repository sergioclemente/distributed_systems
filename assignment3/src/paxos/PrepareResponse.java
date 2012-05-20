package paxos;

import util.SerializationUtil;

public class PrepareResponse {
	private byte hostIdentifier;
	private PrepareRequest prepareRequest;
	private PrepareNumber maxNumberPreparedSoFar;
	
	public PrepareResponse(byte hostIdentifier, PrepareRequest prepareRequest, PrepareNumber maxNumberPreparedSoFar) {
		this.hostIdentifier = hostIdentifier;
		this.prepareRequest = prepareRequest;
		this.maxNumberPreparedSoFar = maxNumberPreparedSoFar;
	}

	public byte getHostIdentifier() {
		return hostIdentifier;
	}
	public PrepareRequest getPrepareRequest() {
		return this.prepareRequest;
	}
	public PrepareNumber getMaxNumberPreparedSoFar() {
		return maxNumberPreparedSoFar;
	}
	
	@Override
	public String toString() {
		return SerializationUtil.serialize(this);
	}
}
