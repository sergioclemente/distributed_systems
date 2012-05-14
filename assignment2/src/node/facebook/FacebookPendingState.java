package node.facebook;

import java.util.UUID;
import java.util.Vector;

import util.NodeUtility;

public class FacebookPendingState {
	private Vector<Message> pendingMessages = new Vector<Message>();
	private UUID activeTxn = null;

	public FacebookPendingState(UUID activeTxn) {
		this.activeTxn = activeTxn;
	}

	public Vector<Message> getPendingMessages() {
		return pendingMessages;
	}

	public void setPendingMessages(Vector<Message> pendingMessages) {
		this.pendingMessages = pendingMessages;
	}
	
	// Debug helper
	@Override
	public String toString() {
		return NodeUtility.serialize(this);
	}

	public UUID getActiveTxn() {
		return activeTxn;
	}

	public void setActiveTxn(UUID activeTxn) {
		this.activeTxn = activeTxn;
	}
}
