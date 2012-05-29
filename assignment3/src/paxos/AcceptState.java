package paxos;
import java.util.Collection;
import java.util.Hashtable;

import util.SerializationUtil;

public class AcceptState {
	private Hashtable<Byte, AcceptResponse> acceptances;
	
	public AcceptState() {
		this.acceptances = new Hashtable<Byte, AcceptResponse>();
	}
	
	public void addAcceptResponse(AcceptResponse response) {
		this.acceptances.put(response.getHostIdentifier(), response);
	}
	
	public AcceptResponse getAcceptResponse(byte hostIdentifier) {
		if (this.acceptances.containsKey(hostIdentifier)) {
			return this.acceptances.get(hostIdentifier);
		} else {
			return null;
		}
	}

	public Collection<AcceptResponse> getAcceptResponses() {
		return this.acceptances.values();
	}
	
	@Override
	public String toString() {
		return SerializationUtil.serialize(this);
	}
	
}
