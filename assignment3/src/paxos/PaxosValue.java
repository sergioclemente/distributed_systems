package paxos;

import util.SerializationUtil;

public class PaxosValue {
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
}
