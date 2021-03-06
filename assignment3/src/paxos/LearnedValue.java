package paxos;

import util.SerializationUtil;

public class LearnedValue {
	private int slotNumber;
	private PaxosValue content;
	private PrepareNumber number;
	
	public LearnedValue(int slotNumber, PaxosValue content, PrepareNumber number) {
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
	
	public PaxosValue getContent() {
		return content;
	}
	
	public void setContent(PaxosValue content) {
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
	
	@Override
	public int hashCode() {
		return slotNumber ^ content.hashCode() ^ number.hashCode();
	}
}
