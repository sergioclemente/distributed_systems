package paxos;

import util.SerializationUtil;

public class PrepareRequest {
	private int slotNumber;
	private PrepareNumber number;
	
	public PrepareRequest(int slotNumber, PrepareNumber number) {
		this.slotNumber = slotNumber;
		this.number = number;
	}

	public PrepareNumber getNumber() {
		return number;
	}

	public int getSlotNumber() {
		return slotNumber;
	}
	
	@Override
	public String toString() {
		return SerializationUtil.serialize(this);
	}
}
