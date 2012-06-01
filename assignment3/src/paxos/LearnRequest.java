package paxos;

public class LearnRequest {
	private int slotNumber;
	private byte hostIdentifier;
	private LearnedValue learnedValue;

	public LearnRequest(int slotNumber, byte hostIdentifier, LearnedValue learnedValue) {
		this.slotNumber = slotNumber;
		this.hostIdentifier = hostIdentifier;
		this.learnedValue = learnedValue;
	}
	
	public int getSlotNumber() {
		return slotNumber;
	}

	public byte getHostIdentifier() {
		return this.hostIdentifier;
	}
	
	public LearnedValue getLearnedValue() {
		return learnedValue;
	}	
}
