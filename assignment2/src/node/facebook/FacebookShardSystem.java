package node.facebook;

import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import node.rpc.RPCMethodCall;

public class FacebookShardSystem extends BaseFacebookSystem {
	// Distributed nodes
	private Hashtable<String, Vector<Message>> m_messages = new Hashtable<String, Vector<Message>>();
	
	public FacebookShardSystem(FacebookRPCNode node) {
		super(node);
	}
	
	public String writeMessagesAll(String from, String message) throws FacebookException {
		
		Set<String> logins = m_messages.keySet();
		
		Message m = new Message(from, message);
		for (String login : logins) {
			this.m_messages.get(login).add(m);
		}

		this.appendToLog("write_message_all " + from + " " + message);
		
		// Nothing to return
		return null;
	}
	
	public String readMessagesAll(String login) throws FacebookException {
		if (!this.m_messages.containsKey(login)) {
			return null;
		}
		Vector<Message> messages = this.m_messages.get(login);
		StringBuffer sb = new StringBuffer();

		for (Message message : messages) {
			sb.append("From:");
			sb.append(message.getFromLogin());
		 	sb.append('\n');
		 	sb.append("Content:");
		 	sb.append(message.getMessage());
		 	sb.append('\n');
		}

		return sb.toString();
	}

	@Override
	protected boolean canCallLocalMethod(String methodCall, Vector<String> params) {
		return methodCall.startsWith("write_message_all") || 
				methodCall.startsWith("read_message_all");
	}

	@Override
	protected String callLocalMethod(String methodCall, Vector<String> params)
			throws FacebookException {
		if (methodCall.startsWith("write_message_all")) {
			return this.writeMessagesAll(params.get(0), params.get(1));
		} else if (methodCall.startsWith("read_message_all")) {
			return this.readMessagesAll(params.get(0));
		} else {
			return null;
		}
	}


}
