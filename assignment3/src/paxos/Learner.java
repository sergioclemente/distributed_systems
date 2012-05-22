package paxos;

public class Learner {
	private LearnedValues learnedValues;
	private byte hostIdentifier;
	
	public Learner(byte hostIdentifier) {
		this.learnedValues = new LearnedValues();
		this.hostIdentifier = hostIdentifier;
	}
	
	public void processLearnRequest(LearnRequest learnRequest) {
		// TODO: Learner has to count the majority too 
		this.learnedValues.setAt(learnRequest.getSlotNumber(), learnRequest.getLearnedValue());
	}

	public LearnedValue getLearnedValue(int slotNumber) {
		return this.learnedValues.getAt(slotNumber);
	}
}
