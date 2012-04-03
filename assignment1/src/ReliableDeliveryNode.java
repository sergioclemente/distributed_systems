	import edu.washington.cs.cse490h.lib.Callback;
import edu.washington.cs.cse490h.lib.Node;
import edu.washington.cs.cse490h.lib.Utility;

import java.lang.reflect.Method;
import java.util.*;

// This class will handle
// - Duplicates (x): check if already received before processing
// - At most once (x): set the received before processing
// - Packet loss (x): Added ack
// - Reordering:
public class ReliableDeliveryNode extends Node {
	private int m_sendSequence;
	private HashSet<Integer> m_receivedSequence = new HashSet<Integer>();
	private Hashtable<Integer, byte[]> m_waitingForAck = new Hashtable<Integer, byte[]>();
	private Method m_timeoutMethod;
	
	private final static int TIMEOUT = 3;

	
	public ReliableDeliveryNode() {
		try {
			this.m_timeoutMethod = Callback.getMethod("onTimeout", this, new String[]{ "java.lang.Integer", "java.lang.Integer" });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private class MESSAGE_TYPE {
		public final static int UNKNOWN=0;
		public final static int NORMAL=1;
		public final static int ACK=2;
	}

	@Override
	public void start() {
		info("Start called, address=" + this.addr);
	}

	@Override
	public void onReceive(Integer from, int protocol, byte[] msg) {
		int sequence = getInt(msg, 0);
		
		if (this.m_receivedSequence.contains(sequence)) {
			info("Already seen sequence number " + sequence);
			this.sendAck(from.intValue(), sequence);
		} else {
			if (protocol == MESSAGE_TYPE.NORMAL) {
				// At most once semantics. 
				this.m_receivedSequence.add(sequence);
				
				byte[] buffer = new byte[msg.length - 4];
				System.arraycopy(msg, 4, buffer, 0, buffer.length);
				
				// Handle reorder
				
				onReliableMessageReceived(buffer);
				this.sendAck(from.intValue(), sequence);
			} else if (protocol == MESSAGE_TYPE.ACK){
				info("Received ack for sequence number " + sequence);
				this.m_waitingForAck.remove(sequence);
			} else {
				// TODO: throw exception!
			}
		}
	}
	
	// todo: reorder handling
	// heap.add(seq);
	// while ( (seq = heap.topKey()) == lsn + 1) {
	//   msg = heap.dequeue();
	// 	 removeTimer();
	//   onMessageReceived();
	//   lsn++;
	// }

	@Override
	public void onCommand(String command) {
		// Send command format: send [targetSender] [message without space]
		// For example: send 2 heyjude!
		if (command.startsWith("send")) {
			String[] parts = command.split("\\s+");
			int targetSender = Integer.parseInt(parts[1]);
			String msgStr = parts[2]; 
			byte[] msg = Utility.stringToByteArray(msgStr);
			this.sendReliableMessage(targetSender, msg);
		}
		info("Command: " + command);
	}
	
	private void addInt(byte[] buffer, int offset, int value) {
		buffer[offset] 	   = (byte)(value >>> 24);
        buffer[offset + 1] = (byte)(value >>> 16);
        buffer[offset + 2] = (byte)(value >>> 8);
        buffer[offset + 3] = (byte)value;
	}
	
	private int getInt(byte[] buffer, int offset) {
		return (buffer[offset] << 24) + ((buffer[offset + 1] & 0xFF) << 16) +
				+ ((buffer[offset + 2] & 0xFF) << 8) + (buffer[offset + 3] & 0xFF);
	}
	
	private void sendAck(int targetSender, int sequenceNumber) {
		byte[] buffer = new byte[4];
		addInt(buffer, 0, sequenceNumber);
		this.send(targetSender, MESSAGE_TYPE.ACK, buffer);
	}
	
	protected static void warn(String msg) {
		// Put some markers in the begining so we can easily distinguish between system messages
		System.out.println("********* " + msg);
	}
	
	protected static void info(String msg) {
		// Put some markers in the begining so we can easily distinguish between system messages
		System.out.println("********* " + msg);
	}
	
	/**
	 * Methods that subclasses will call to reliably send a message
	 * @param targetSender
	 * @param msg
	 */
	protected void sendReliableMessage(int targetSender, byte[] msg) {
		byte[] buffer = new byte[msg.length + 4];
		addInt(buffer, 0, this.m_sendSequence);
		System.arraycopy(msg, 0, buffer, 4, msg.length);
		internalSendPacket(targetSender, this.m_sendSequence, buffer);
		this.m_sendSequence++;
	}
	
	private void internalSendPacket(int targetSender, int sequenceNumber, byte[] buffer) {
		if (!this.m_waitingForAck.containsKey(sequenceNumber)) {
			this.m_waitingForAck.put(sequenceNumber, buffer);
		}
		this.addTimeout(new Callback(this.m_timeoutMethod, this, new Object[] {targetSender, sequenceNumber}), TIMEOUT);
		this.send(targetSender, MESSAGE_TYPE.NORMAL, buffer);
	}
	
	public void onTimeout(Integer targetSender, Integer sequenceNumber) {
		// Resent the packet if we didn't receive the ack yet
		if (this.m_waitingForAck.containsKey(sequenceNumber.intValue())) {
			info("Resending packet " + sequenceNumber);
			byte[] buffer = this.m_waitingForAck.get(sequenceNumber.intValue());
			this.internalSendPacket(targetSender, sequenceNumber, buffer);			
		}
	}
	
	/**
	 * Method that subclasses will override to handle reliably message received stuff
	 * @param msg
	 */
	protected void onReliableMessageReceived(byte[] msg) {
		info("Received message " + Utility.byteArrayToString(msg));
	}
}
