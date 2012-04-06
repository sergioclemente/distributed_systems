package node.reliable;
import java.util.HashSet;
import java.util.Hashtable;

import util.Tuple;



public class Session {
	private HashSet<Integer> m_receivedSequence = new HashSet<Integer>();
	private HashSet<Integer> m_waitingForAck = new HashSet<Integer>();
	private Hashtable<Integer, Tuple<Integer, byte[]>> m_reorderMap = new Hashtable<Integer, Tuple<Integer, byte[]>>();
	private int m_sendSequenceNumber;
	private int m_receiveSequenceNumber;
	
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
	public boolean containsInWaitinfForAckList(int sequence) {
		return this.m_waitingForAck.contains(sequence);
	}
	
	// Send sequence numbers
	public int getSendSequence() {
		return this.m_sendSequenceNumber;
	}
	
	public void incrementSendSequence() {
		this.m_sendSequenceNumber++;
	}	

	// Reordering
	public void addToReceiveQueue(int sequence, int from, byte[] buffer) {
		this.m_reorderMap.put(sequence, new Tuple<Integer,byte[]>(from, buffer));
	}
	
	public Tuple<Integer, byte[]> getNextReceiveBuffer() {
		if (this.m_reorderMap.containsKey(this.m_receiveSequenceNumber)) {
			Tuple<Integer, byte[]> tuple = this.m_reorderMap.get(this.m_receiveSequenceNumber);
			
			this.m_reorderMap.remove(this.m_receiveSequenceNumber);
			
			this.m_receiveSequenceNumber++;
			
			return tuple;
		} else {
			return null;
		}
	}
}
