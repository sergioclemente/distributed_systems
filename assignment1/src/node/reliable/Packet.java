package node.reliable;

import edu.washington.cs.cse490h.lib.Utility;
import util.ByteManipulator;

public class Packet 
{
	private int m_from;
	private int m_to;
	private int m_type;
	private int m_connId;
	private int m_seqNum;
	private byte[] m_payload;
	
	/**
	 * Creates a Packet object from a raw data byte array 
	 * @param buffer - byte array containing the full packet contents
	 * @return Packet object
	 */
	public static Packet CreateFromBuffer(byte[] buffer) 
	{
		assert buffer.length >= 20;
		
		Packet packet = new Packet();
		packet.setFrom(ByteManipulator.getInt(buffer, 0));
		packet.setTo(ByteManipulator.getInt(buffer, 4));
		packet.setType(ByteManipulator.getInt(buffer, 8));
		packet.setConnectionId(ByteManipulator.getInt(buffer, 12));
		packet.setSequence(ByteManipulator.getInt(buffer, 16));
		
		if (buffer.length > 20)
		{
			byte[] payload = new byte[buffer.length - 20];
			System.arraycopy(buffer, 20, payload, 0, buffer.length-20);
			packet.setPayload(payload);
		}
		
		return packet;
	}
	
	
	/**
	 * Assembles individual field values into a Packet object
	 * @param from - sender
	 * @param to - receiver
	 * @param ptype - packet type
	 * @param connId - connection ID
	 * @param seqNum - sequence number
	 * @param payload - packet contents
	 * @return Packet object
	 */
	public static Packet Create(int from, int to, int ptype, int connId, int seqNum, byte[] payload)
	{
		Packet packet;
		
		packet = new Packet();
		packet.setFrom(from);
		packet.setTo(to);
		packet.setType(ptype);
		packet.setConnectionId(connId);
		packet.setSequence(seqNum);
		packet.setPayload(payload);
		
		return packet;
	}
	
	
	public Packet()
	{
		m_from 	= -1;
		m_to	= -1;
		m_type	= -1;
		m_connId = -1;
		m_seqNum = -1;
		m_payload = null;
	}
	

	public int getFrom() {
		return m_from;
	}
	
	public void setFrom(int from) {
		m_from = from;
	}
	
	public int getTo() {
		return m_to;
	}
	
	public void setTo(int to) {
		m_to = to;
	}
	
	public int getType() {
		return m_type;
	}
	
	public void setType(int ptype) {
		m_type = ptype;
	}
	
	public int getConnectionId() {
		return m_connId;
	}
	
	public void setConnectionId(int connId) {
		m_connId = connId;
	}
	
	public int getSequence() {
		return m_seqNum;
	}
	
	public void setSequence(int seqNum) {
		m_seqNum = seqNum;
	}

	public byte[] getPayload() {
		return m_payload;
	}
	
	public void setPayload(byte[] payload) {
		m_payload = payload;
	}
	
	public byte[] toByteArray() {
		byte[] buffer;
		int length = 20;
		
		if (m_payload != null) {
			length += m_payload.length;
		}
		
		buffer = new byte[length];
		
		ByteManipulator.addInt(buffer, 0, m_from);
		ByteManipulator.addInt(buffer, 4, m_to);
		ByteManipulator.addInt(buffer, 8, m_type);
		ByteManipulator.addInt(buffer, 12, m_connId); 
		ByteManipulator.addInt(buffer, 16, m_seqNum);
		
		if (m_payload != null) {
			System.arraycopy(m_payload, 0, buffer, 20, m_payload.length);
		}

		return buffer;
	}
	
	public String stringizeHeader() {
		int contentLength = 0;
		
		if (m_payload != null)
		{
			contentLength = m_payload.length;
		}
		
		return String.format("[ from:%d, to:%d, type:%d, id:0x%08X, seq:0x%08X | dlen:%d, ... ]", 
 							 m_from, m_to, m_type, m_connId, m_seqNum, contentLength);
	}
	
	public String stringize() {
		String content = "<none>";
		int contentLength = 0;
		
		if (m_payload != null)
		{
			content = Utility.byteArrayToString(m_payload);
			contentLength = m_payload.length;
		}
		
		return String.format("[ from:%d, to:%d, type:%d, id:0x%08X, seq:0x%08X | dlen:%d, data:'%s' ]", 
 							 m_from, m_to, m_type, m_connId, m_seqNum, contentLength, content);
	}
}
