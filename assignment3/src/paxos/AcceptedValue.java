package paxos;

import util.SerializationUtil;

public class AcceptedValue {
	private int slotNumber;
	private Object content;
	private PrepareNumber number;
	
	public AcceptedValue(int slotNumber, Object content, PrepareNumber number) {
		this.setSlotNumber(slotNumber);
		this.number = number;
		this.content = content;
	}
	
	public PrepareNumber getNumber() {
		return number;
	}
	public void setNumber(PrepareNumber number) {
		this.number = number;
	}
	public Object getContent() {
		return content;
	}
	public void setContent(Object content) {
		this.content = content;
	}

	public int getSlotNumber() {
		return slotNumber;
	}

	public void setSlotNumber(int slotNumber) {
		this.slotNumber = slotNumber;
	}
	
	@Override
	public String toString() {
		return SerializationUtil.serialize(this);
	}
}
