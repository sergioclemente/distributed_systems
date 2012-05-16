package paxos;

public class ProposedValue {
	private ProposalNumber number;
	private Object value;
	
	public ProposedValue(ProposalNumber number, Object value) {
		this.number = number;
		this.value = value;
	}
	
	public ProposalNumber getNumber() {
		return number;
	}
	public void setNumber(ProposalNumber number) {
		this.number = number;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
}
