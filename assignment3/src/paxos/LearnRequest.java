package paxos;

public class LearnRequest {
	private int slotNumber;
	private LearnedValue learnedValue;

	public LearnRequest(int slotNumber, LearnedValue learnedValue) {
		this.slotNumber = slotNumber;
		this.learnedValue = learnedValue;
	}
	
	public int getSlotNumber() {
		return slotNumber;
	}

	public LearnedValue getLearnedValue() {
		return learnedValue;
	}	
}
