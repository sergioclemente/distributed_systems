package paxos;

import java.util.HashMap;
import java.util.Map;

public class Learner {
	private LearnedValues learnedValues;
	
	/**
	 * Count of how many accepts a learner got for each proposal number.
	 */
	private Map<Integer, Integer> numAcceptNotifications;
	
	/**
	 * The number of acceptors in the system. It is used by the learner so that it can decide when it has
	 * effectively learned a value, which happens only when it receives learn requests from more than half
	 * of the acceptors.
	 */
	private int numberOfAcceptors;
	
	private byte hostIdentifier;
	
	public Learner(byte hostIdentifier, int numberOfAcceptors) {
		this.learnedValues = new LearnedValues();
		this.hostIdentifier = hostIdentifier;
		this.numAcceptNotifications = new HashMap<Integer, Integer>();
		this.numberOfAcceptors = numberOfAcceptors;
	}
	
	public void processLearnRequest(LearnRequest learnRequest) {
		int slotNumber = learnRequest.getSlotNumber();
		
		// We have already learned the value for this proposal number		
		if (learnedValues.getAt(slotNumber) != null) return;
		
		if (!numAcceptNotifications.containsKey(slotNumber))
		{
			numAcceptNotifications.put(slotNumber, 0);
		}
		
		int countAcceptedNotifications = numAcceptNotifications.get(slotNumber);
		countAcceptedNotifications++;
		
		if (countAcceptedNotifications > (numberOfAcceptors / 2))
		{
			numAcceptNotifications.remove(slotNumber);
			learnedValues.setAt(slotNumber, learnRequest.getLearnedValue());
			
			//TODO-licavalc: maybe we should save to stable storage here.			
		}
		else
		{
			numAcceptNotifications.put(slotNumber, countAcceptedNotifications);
		}
	}

	public LearnedValue getLearnedValue(int slotNumber) {
		return this.learnedValues.getAt(slotNumber);
	}
}
