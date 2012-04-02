import edu.washington.cs.cse490h.lib.Node;
import edu.washington.cs.cse490h.lib.Utility;
import java.util.*;

// This class will handle
// - Duplicates (x): 
// - At most once (x):
// - Packet loss:
// - Reordering:
public class ReliableDeliveryNode extends Node {
	private int m_sendSequence;
	private HashSet<Integer> m_receivedSequence = new HashSet<Integer>();
	private HashSet<Integer> m_waitingForAck = new HashSet<Integer>();
	
	private class MESSAGE_TYPE {
		public final static int UNKNOWN=0;
		public final static int NORMAL=0;
		public final static int ACK=0;
	}

	@Override
	public void start() {
		System.out.println("start() called, address=" + this.addr);
	}

	@Override
	public void onReceive(Integer from, int protocol, byte[] msg) {
		int sequence = getInt(msg, 0);
		
		if (this.m_receivedSequence.contains(sequence)) {
			// TODO: Send ack again
			info("Already seen sequence number " + sequence);
		} else {
			if (protocol == MESSAGE_TYPE.NORMAL) {
				// At most once semantics. 
				this.m_receivedSequence.add(sequence);
				
				byte[] buffer = new byte[msg.length - 4];
				System.arraycopy(msg, 4, buffer, 0, buffer.length);
				
				onMessageReceived(buffer);
				// TODO: send ack
			} else if (protocol == MESSAGE_TYPE.ACK){
				// TODO: timer, mark as received
			} else {
				// TODO: throw exception!
			}
		}
	}

	@Override
	public void onCommand(String command) {
		// Send command format: send [targetSender] [message without space]
		// For example: send 2 heyjude!
		if (command.startsWith("send")) {
			String[] parts = command.split("\\s+");
			int targetSender = Integer.parseInt(parts[1]);
			String msgStr = parts[2]; 
			byte[] msg = Utility.stringToByteArray(msgStr);
			this.sendMessage(targetSender, msg);
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
	
	protected static void warn(String msg) {
		System.out.println(msg);
	}
	
	protected static void info(String msg) {
		System.out.println(msg);
	}
	
	/**
	 * Methods that subclasses will call to reliably deliver message
	 * @param targetSender
	 * @param msg
	 */
	protected void sendMessage(int targetSender, byte[] msg) {
		byte[] buffer = new byte[msg.length + 4];
		addInt(buffer, 0, this.m_sendSequence);
		System.arraycopy(msg, 0, buffer, 4, msg.length);
		this.m_waitingForAck.add(this.m_sendSequence);
		// TODO: Add timer to retry
		this.send(targetSender, MESSAGE_TYPE.NORMAL, buffer);
		this.m_sendSequence++;
	}
	
	// TODO: timer function
	// if (wasNotAckedYet()) {
	// 	resend()
	// }
	
	
	// ACK handling
	// heap.add(seq);
	// while ( (seq = heap.topKey()) == lsn + 1) {
	//   msg = heap.dequeue();
	// 	 removeTimer();
	//   onMessageReceived();
	//   lsn++;
	// }
	
	/**
	 * Method that subclasses will override to handle reliably message received stuff
	 * @param msg
	 */
	protected void onMessageReceived(byte[] msg) {
		info("Received message " + Utility.byteArrayToString(msg));
	}
}
