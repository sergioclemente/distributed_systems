package node.facebook;

import java.util.Vector;

public class FacebookPendingState {
	private Vector<Message> pendingMessages = new Vector<Message>();

	public Vector<Message> getPendingMessages() {
		return pendingMessages;
	}

	public void setPendingMessages(Vector<Message> pendingMessages) {
		this.pendingMessages = pendingMessages;
	}
}
