package paxos;

import java.util.Vector;

public class ProposedValues {
	private Vector<ProposedValue> values;
	
	public ProposedValues() {
		this.values = new Vector<ProposedValue>();
	}
	
	public void setValue(int index, ProposedValue value) {
		if (value == null || value.getValue() == null) {
			throw new PaxosException();
		}
		ProposedValue previousValue = this.getValue(index);
		
		if (previousValue != null) {
//			P2a. If a proposal with value v is chosen, 
//			then every higher-numbered proposal 
//			accepted by any acceptor has value v
			if (!value.getValue().equals(previousValue.getValue())) {
				throw new PaxosException();
			}
			
			// Previous value cannot have higher sequence number
			if (previousValue.getNumber().getSequencialNumber() > value.getNumber().getSequencialNumber()) {
				throw new PaxosException();
			}
		}
		
		this.values.add(index, value);
	}
	
	public ProposedValue getValue(int index) {
		if (index < 0 || index >= this.values.size()) {
			return null;
		}
		
		return this.values.get(index);
	}
}
