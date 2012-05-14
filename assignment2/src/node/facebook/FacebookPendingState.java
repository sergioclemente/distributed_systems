package node.facebook;

import java.util.Vector;

import util.NodeUtility;

public class FacebookPendingState {
	private Vector<Message> pendingMessages = new Vector<Message>();

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
}
