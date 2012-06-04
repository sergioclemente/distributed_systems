package paxos;
import java.util.Hashtable;
import java.util.LinkedHashMap;

public class Learner {
	private LearnedValues learnedValues;
	
	/**
	 * The number of acceptors in the system. It is used by the learner so that it can decide when it has
	 * effectively learned a value, which happens only when it receives learn requests from more than half
	 * of the acceptors.
	 */
	private int numberOfAcceptors;
	
	private byte hostIdentifier;
	private Hashtable<Integer, Hashtable<Byte, LearnedValue>> acceptances;
	
	public Learner(byte hostIdentifier, int numberOfAcceptors, LinkedHashMap<Integer, PaxosValue> knownValues, int slotCount) {
		this.learnedValues = new LearnedValues();
		this.hostIdentifier = hostIdentifier;
		this.acceptances = new Hashtable<Integer, Hashtable<Byte, LearnedValue>>();
		this.numberOfAcceptors = numberOfAcceptors;
		
		// Populate current known values
		for (int i = 0; i < slotCount; i++) {
			LearnedValue lv = new LearnedValue(i, knownValues.get(i), new PrepareNumber(hostIdentifier, 1)); 
			this.learnedValues.setAt(i, lv);
		}
	}
	
	public boolean processLearnRequest(LearnRequest learnRequest) {
		int slotNumber = learnRequest.getSlotNumber();
	
		// We have already learned the value for this proposal number		
		if (learnedValues.getAt(slotNumber) != null) {
			return false;
		}

		Hashtable<Byte, LearnedValue> slotAcceptances;
		if (!this.acceptances.containsKey(slotNumber)) {
			slotAcceptances = new Hashtable<Byte, LearnedValue>();
			this.acceptances.put(slotNumber, slotAcceptances);
		} else {
			slotAcceptances = this.acceptances.get(slotNumber);
		}
		
		// Store / update the sender's accepted value
		slotAcceptances.put(learnRequest.getHostIdentifier(), learnRequest.getLearnedValue());

		// Build a histogram of each accepted value.
		// Technically we should be counting proposals, but since each 
		// proposed value is unique (since it carries the value plus
		// proposer ID), counting values works just as well.
		Hashtable<PaxosValue, Integer> valueCounter = new Hashtable<PaxosValue, Integer>();
		for (LearnedValue acceptedValue : slotAcceptances.values()) {
			PaxosValue v = acceptedValue.getContent();
			if (!valueCounter.containsKey(v)) {
				valueCounter.put(v, 1);
			} else {
				valueCounter.put(v, valueCounter.get(v) + 1);
			}
		}
		
		// A value is chosen when its accepted count is a majority.
		PaxosValue chosenValue = null;
		for (PaxosValue v : valueCounter.keySet()) {
			int count = valueCounter.get(v);
			if (count > (numberOfAcceptors/2)) {
				// Value was chosen
				chosenValue = v;
			}
		}
		
		if (chosenValue != null) {
			LearnedValue lv = new LearnedValue(slotNumber, chosenValue, learnRequest.getLearnedValue().getNumber());
			learnedValues.setAt(slotNumber, lv);
			System.out.println(String.format("##### L%d: Value was chosen: (%d,%s)", 
					(int) this.hostIdentifier, (int) chosenValue.getProposer(), 
					chosenValue.getCommand()));
			return true;
		} else {
			System.out.println(String.format("##### L%d: Value NOT chosen yet",
					(int) this.hostIdentifier));
			return false;
		}
	}

	public LearnedValue getLearnedValue(int slotNumber) {
		return this.learnedValues.getAt(slotNumber);
	}
	
	public boolean shouldStartLearningProcess(int slotNumber) {
		//TODO-licavalc: need to deal with the case where we are in the middle or learning but didn't get all answers
		// Don't know the value and haven't started learning
		if (this.learnedValues.getAt(slotNumber) == null && !this.acceptances.containsKey(slotNumber)) {
			this.acceptances.put(slotNumber, new Hashtable<Byte, LearnedValue>());
			return true;
		}
		
		return false;
	}
}
