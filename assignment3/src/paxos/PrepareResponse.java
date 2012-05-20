package paxos;

import util.SerializationUtil;

public class PrepareResponse {
	private byte hostIdentifier;
	private int slotNumber;
	private PrepareNumber requestNumber;
	private PrepareNumber maxNumberAcceptedSoFar;

	
	public PrepareResponse(byte hostIdentifier, int slotNumber, PrepareNumber requestNumber, PrepareNumber maxNumberAcceptedSoFar) {
		this.slotNumber = slotNumber;
		this.requestNumber = requestNumber;
		this.maxNumberAcceptedSoFar = maxNumberAcceptedSoFar;
		this.hostIdentifier = hostIdentifier;
	}

	public int getSlotNumber() {
		return slotNumber;
	}

	public PrepareNumber getMaxNumberAcceptedSoFar() {
		return maxNumberAcceptedSoFar;
	}
	public PrepareNumber getRequestNumber() {
		return requestNumber;
	}
	public byte getHostIdentifier() {
		return hostIdentifier;
	}
	
	@Override
	public String toString() {
		return SerializationUtil.serialize(this);
	}
}
