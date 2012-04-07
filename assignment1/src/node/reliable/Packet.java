package node.reliable;

import util.ByteManipulator;

public class Packet {
	private byte[] m_buffer;
	private int m_from;
	private int m_sequence;
	
	public Packet(byte[] buffer, int from) {
		super();
		
		// create a copy of the buffer without the sequence number
		this.m_buffer = new byte[buffer.length - 4];
		this.m_from = from;
		this.m_sequence = ByteManipulator.getInt(buffer, 0);
		
		System.arraycopy(buffer, 4, this.m_buffer, 0, this.m_buffer.length);
	}
	
	
	public byte[] getBuffer() {
		return m_buffer;
	}
	public void setBuffer(byte[] buffer) {
		this.m_buffer = buffer;
	}
	public int getFrom() {
		return m_from;
	}
	public void setFrom(int from) {
		this.m_from = from;
	}
	public int getSequence() {
		return m_sequence;
	}
	public void setSequence(int sequence) {
		this.m_sequence = sequence;
	}
}
