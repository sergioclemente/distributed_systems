package paxos;

import util.SerializationUtil;

public class PrepareNumber implements  Comparable<PrepareNumber> {
	private long value;
	
	public PrepareNumber(byte hostIdentifier, int sequenceNumber) {
		this.value = (sequenceNumber << 8) | hostIdentifier;
	}
	
	private PrepareNumber(long value) {
		this.value = value;
	}
	
	public long getValue() {
		return value;
	}
	
	public byte getHostIdentifier() {
		return (byte)(this.value & 0xFF);
	}
	
	public int getSequenceNumber() {
		return (int)(this.value >> 8);
	}
	
	public void setSequenceNumber(int sequenceNumber) {
		long sequenceNumberPart = ((long) sequenceNumber)<<8;
		long hostIdentifierPart = (this.value & 0x00000000000000FFL);
		this.value = hostIdentifierPart | sequenceNumberPart;
	}
	
	public PrepareNumber clone() {
		return new PrepareNumber(this.value);
	}

	@Override
	public int compareTo(PrepareNumber prepareNumber) {
		if (prepareNumber == null) {
			return 1;
		} else {
			if (this.getValue() > prepareNumber.getValue()) {
				return +1;
			} else if (this.getValue() < prepareNumber.getValue()){
				return -1;
			} else {
				return 0;
			}
		}
	}
	
	@Override
	public String toString() {
		return SerializationUtil.serialize(this);
	}
}
