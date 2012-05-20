package paxos;

public class AcceptResponse {
	private PrepareRequest prepareRequest;
	private PrepareNumber maxNumberPreparedSoFar;
	
	public AcceptResponse(PrepareRequest prepareRequest, PrepareNumber maxNumberPreparedSoFar) {
		this.prepareRequest = prepareRequest;
		this.maxNumberPreparedSoFar = maxNumberPreparedSoFar;
	}
	public PrepareRequest getPrepareRequest() {
		return this.prepareRequest;
	}
	public PrepareNumber getMaxNumberPreparedSoFar() {
		return maxNumberPreparedSoFar;
	}
}
