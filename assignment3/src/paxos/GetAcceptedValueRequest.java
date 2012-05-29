package paxos;

import util.SerializationUtil;

public class GetAcceptedValueRequest {
	private int slotNumber;
	
	private int learner;
	
	public GetAcceptedValueRequest(int slotNumber, int learner)
	{
		this.slotNumber = slotNumber;
	}
	
	public int getSlotNumber()
	{
		return slotNumber;
	}
	
	public int getLearner()
	{
		return learner;
	}
	
	@Override
	public String toString() {
		return SerializationUtil.serialize(this);
	}
}
