package paxos;

import util.SerializationUtil;

public class PrepareResponse {
	private byte hostIdentifier;
	private PrepareRequest prepareRequest;
	private PrepareNumber maxNumberPreparedSoFar;
	private Object content;
	
	public PrepareResponse(byte hostIdentifier, PrepareRequest prepareRequest, PrepareNumber maxNumberPreparedSoFar, Object content) {
		this.hostIdentifier = hostIdentifier;
		this.prepareRequest = prepareRequest;
		this.maxNumberPreparedSoFar = maxNumberPreparedSoFar;
		this.content = content;
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
	public Object getContent() {
		return this.content;
	}
	
	@Override
	public String toString() {
		return SerializationUtil.serialize(this);
	}
}
