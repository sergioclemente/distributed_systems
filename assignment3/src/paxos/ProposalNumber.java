package paxos;

public class ProposalNumber {
	private long value;
	
	public ProposalNumber(byte hostIdentifier, int sequenceNumber) {
		this.value = (sequenceNumber << 8) | hostIdentifier;;
	}
	
	public long getValue() {
		return value;
	}
	
	public byte getHostIdentifier() {
		return (byte)(this.value & 0xFF);
	}
	
	public int getSequencialNumber() {
		return (int)(this.value >> 8);
	}
	
	public void setSequenceNumber(int sequenceNumber) {
		long sequenceNumberPart = ((long) sequenceNumber)<<8;
		long hostIdentifierPart = (this.value & 0x00000000000000FFL);
		this.value = hostIdentifierPart | sequenceNumberPart;
	}
	
	@Override
	public String toString() {
		return String.format("%x", this.value);
	}
}
