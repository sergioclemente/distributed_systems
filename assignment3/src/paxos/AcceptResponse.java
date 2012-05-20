package paxos;

public class AcceptResponse {
	private PrepareRequest prepareRequest;
	private PrepareNumber maxNumberPreparedSoFar;
	private boolean success;
	
	public AcceptResponse(PrepareRequest prepareRequest, PrepareNumber maxNumberPreparedSoFar, boolean success) {
		this.prepareRequest = prepareRequest;
		this.maxNumberPreparedSoFar = maxNumberPreparedSoFar;
		this.success = success;
	}
	public PrepareRequest getPrepareRequest() {
		return this.prepareRequest;
	}
	public PrepareNumber getMaxNumberPreparedSoFar() {
		return maxNumberPreparedSoFar;
	}
	public boolean getSuccess() {
		return this.success;
	}
}
