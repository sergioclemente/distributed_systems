package paxos;

public class AcceptRequest {
	private PrepareRequest prepareRequest;
	private Object value;

	public PrepareRequest getPrepareRequest() {
		return prepareRequest;
	}

	public Object getValue() {
		return this.value;
	}
}
