package paxos;

import util.SerializationUtil;

public class PaxosValue implements Comparable<PaxosValue> {
	private String command;
	private byte proposer;
	
	public PaxosValue(byte proposer, String command) {
		this.command = command;
		this.proposer = proposer;
	}
	
	public String getCommand() {
		return this.command;
	}
	
	public byte getProposer() {
		return this.proposer;
	}
	
	@Override
	public String toString() {
		return SerializationUtil.serialize(this);
	}
	
	@Override
	public int hashCode() {
		return command.hashCode() ^ ((int) proposer);
	}
	
	@Override
	public int compareTo(PaxosValue other) {
		if (other == null) {
			return 1;
		} else {
			if (this.proposer > other.proposer) {
				return +1;
			} else if (this.proposer < other.proposer) {
				return -1;
			} else {
				return this.command.compareTo(other.command);
			}
		}
	}
	
	@Override 
	public boolean equals(Object obj) {
		if (obj.getClass() == this.getClass()) {
			return this.compareTo((PaxosValue) obj) == 0;
		} else {
			return false;
		}
	}
}
