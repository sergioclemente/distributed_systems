package paxos;

import java.util.Vector;

import util.SerializationUtil;

public class AcceptedValues {
	private Vector<AcceptedValue> values;
	
	public AcceptedValues() {
		this.values = new Vector<AcceptedValue>();
	}
	
	public void setAt(int slotNumber, AcceptedValue value) {
		if (value == null) {
			throw new PaxosException(PaxosException.VALUE_IS_NULL);
		}
		if (value.getContent() == null) {
			throw new PaxosException(PaxosException.CONTENT_IS_NULL);
		}
		
		AcceptedValue previousValue = this.getAt(slotNumber);
		
		if (previousValue != null) {
//			P2a. If a proposal with value v is chosen, 
//			then every higher-numbered proposal 
//			accepted by any acceptor has value v
			if (!value.getContent().equals(previousValue.getContent())) {
				throw new PaxosException(PaxosException.PREVIOUS_VALUE_DIFFERENT_THAN_CURRENT);
			}
			if (value.getSlotNumber() != previousValue.getSlotNumber()) {
				throw new PaxosException(PaxosException.PREVIOUS_SLOT_NUMBER_DIFFERENT_THAN_CURRENT);
			} 
			
			// Previous value cannot have higher sequence number
			if (previousValue.getNumber().getSequenceNumber() > value.getNumber().getSequenceNumber()) {
				throw new PaxosException(PaxosException.PREVIOUS_SEQUENCE_NUMBER_HIGHER_THAN_CURRENT);
			}
		}
		
		this.values.add(slotNumber, value);
	}
	
	public AcceptedValue getAt(int slotNumber) {
		if (slotNumber < 0 || slotNumber >= this.values.size()) {
			return null;
		}
		
		return this.values.get(slotNumber);
	}
	
	@Override
	public String toString() {
		return SerializationUtil.serialize(this);
	}
}
