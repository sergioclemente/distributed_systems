package node.reliable;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Queue;
import java.util.LinkedList;
import edu.washington.cs.cse490h.lib.Utility;;

public class Session {
	private HashSet<Integer> m_receivedSequence = new HashSet<Integer>();
	private HashSet<Integer> m_waitingForAck = new HashSet<Integer>();
	private Hashtable<Integer, Packet> m_reorderMap = new Hashtable<Integer, Packet>();
	
	private Queue<Packet> m_outQueue = new LinkedList<Packet>();
	
	private boolean m_connecting;
	private boolean m_connected;
	private boolean m_closed;
	private int m_connectionId;
	private int m_sequence;
	
	public Session() {
		m_connecting = false;
		m_connected = false;
		m_connectionId = Utility.getRNG().nextInt() | 0x00010000;
		m_sequence = 0;
	}
	
	public int getSequence() {
		return m_sequence;
	}
	
	public void incSequence() {
		m_sequence++;
	}
	
	public int getConnectionId() {
		return m_connectionId;
	}
	
	public void setConnectionId(int connectionId) {
		m_connectionId = connectionId;
	}
	
	public boolean getConnecting() {
		return m_connecting;
	}
	
	public void setConnecting() {
		m_connecting = true;
	}
	
	public boolean getConnected() {
		return m_connected;
	}
	
	public void setConnected() {
		m_connected = true;
	}
	
	public boolean getClosed() {
		return m_closed;
	}
	
	public void setClosed() {
		m_closed = true;
	}
	
	// Duplicates
	public boolean didAlreadyReceiveSequence(int sequence) {
		return this.m_receivedSequence.contains(sequence);
	}
	
	public void markSequenceAsReceived(int sequence) {
		this.m_receivedSequence.add(sequence);
	}
	
	// Ack handling
	public void addToWaitingForAckList(int sequence) {
		this.m_waitingForAck.add(sequence);
	}
	
	public void removeFromWaitingForAckList(int sequence) {
		this.m_waitingForAck.remove(sequence);
	}
	
	public boolean containsInWaitingForAckList(int sequence) {
		return this.m_waitingForAck.contains(sequence);
	}
	
	// Queuing
	public void addToSendQueue(Packet packet) {
		m_outQueue.add(packet);
	}
	
	public Queue<Packet> getSendQueue() {
		return m_outQueue;
	}
	
	// Reordering
	public void addToReceiveQueue(Packet packet) {
		this.m_reorderMap.put(packet.getSequence(), packet);
	}
	
	public Packet getNextReceivePacket() {
		if (this.m_reorderMap.containsKey(this.m_sequence)) {
			Packet packet = this.m_reorderMap.get(this.m_sequence);
			
			this.m_reorderMap.remove(this.m_sequence);
			
			this.m_sequence++;
			
			return packet;
		} else {
			return null;
		}
	}
}
