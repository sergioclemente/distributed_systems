package node.reliable;
import edu.washington.cs.cse490h.lib.Callback;
import edu.washington.cs.cse490h.lib.Node;
import edu.washington.cs.cse490h.lib.Utility;

import java.lang.reflect.Method;
import java.util.*;

import util.Tuple;

// This class will handle
// - Duplicates (x): check if already received before processing
// - At most once (x): set the received before processing
// - Packet loss (x): Added ack
// - Reordering: (x): Added reordering
public class ReliableDeliveryNode extends Node {
	private SessionManager m_sessionManager = new SessionManager();
	private Method m_timeoutMethod;
	private final static int TIMEOUT = 3;
	
	public ReliableDeliveryNode() {
		try {
			// [B is the same as byte[]
			this.m_timeoutMethod = Callback.getMethod("onTimeout", this, new String[]{ "java.lang.Integer", "java.lang.Integer", "[B" });
		} catch (Exception e) {
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
		
		Session session = this.m_sessionManager.getSession(from);
		
		if (session.didAlreadyReceiveSequence(sequence)) {
			info("Already seen sequence number " + sequence);
			this.sendAck(from.intValue(), sequence);
		} else {
			if (protocol == MESSAGE_TYPE.NORMAL) {
				// At most once semantics. 
				session.markSequenceAsReceived(sequence);

				session.addToReceiveQueue(sequence, from, msg);
				
				Tuple<Integer, byte[]> tuple;
				while ( (tuple=session.getNextReceiveBuffer()) != null) {
					// Remove the sequence number before firing the event
					byte[] nextMsgTruncated = new byte[tuple.getT2().length - 4];
					System.arraycopy(tuple.getT2(), 4, nextMsgTruncated, 0, nextMsgTruncated.length);
					
					this.onReliableMessageReceived(tuple.getT1(), nextMsgTruncated);
				}

				this.sendAck(from.intValue(), sequence);
			} else if (protocol == MESSAGE_TYPE.ACK){
				info("Received ack for sequence number " + sequence);
				session.removeFromWaitingForAckList(sequence);
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
	
	protected static void error(String msg) {
		// Put some markers in the begining so we can easily distinguish between system messages
		System.out.println("********* " + msg);
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
		Session session = this.m_sessionManager.getSession(targetSender);
		
		byte[] buffer = new byte[msg.length + 4];
		addInt(buffer, 0, session.getSendSequence());
		System.arraycopy(msg, 0, buffer, 4, msg.length);
		session.addToWaitingForAckList(session.getSendSequence());
		internalSendPacket(targetSender, session.getSendSequence(), buffer);
		session.incrementSendSequence();
	}
	
	/**
	 * This method is called when the transport layer gives up sending the packet
	 * @param endpoint the remote endpoint that the connection is being affected
	 */
	protected void onConnectionAborted(int endpoint) {
		// TODO: not being called yet, just a placeholder method
	}
	
	private void internalSendPacket(int targetSender, int sequenceNumber, byte[] buffer) {
		this.addTimeout(new Callback(this.m_timeoutMethod, this, new Object[] {targetSender, sequenceNumber, buffer}), TIMEOUT);
		this.send(targetSender, MESSAGE_TYPE.NORMAL, buffer);
	}
	
	public void onTimeout(Integer targetSender, Integer sequenceNumber, byte[] buffer) {
		Session session = this.m_sessionManager.getSession(targetSender);
		
		// Resent the packet if we didn't receive the ack yet
		if (session.containsInWaitinfForAckList(sequenceNumber.intValue())) {
			info("Resending packet " + sequenceNumber);
			this.internalSendPacket(targetSender, sequenceNumber, buffer);			
		}
	}
	
	/**
	 * Method that subclasses will override to handle reliably message received stuff
	 * @param msg
	 */
	protected void onReliableMessageReceived(int from, byte[] msg) {
		info("Received message: - " + Utility.byteArrayToString(msg) + " - from: " + from);
	}
}
