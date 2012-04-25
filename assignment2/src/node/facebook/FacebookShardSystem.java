package node.facebook;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import node.rpc.RPCMethodCall;

public class FacebookShardSystem extends BaseFacebookSystem {
	// Distributed nodes
	private Hashtable<String, List<Message>> m_messages = new Hashtable<String, List<Message>>();
	
	public FacebookShardSystem(FacebookRPCNode node) {
		super(node);
	}
	
	public String writeMessagesAll(String token, String message) throws FacebookException {
//		User user = getUserFromToken(token);
//		String login = user.getLogin();
//
//		assert this.m_friends.containsKey(login);
//		List<User> friends = this.m_friends.get(login);
//		Message m = new Message(login, message);
//		for (User friend : friends) {
//			List<Message> msglist;
//			msglist = this.m_messages.get(friend.getLogin());
//			msglist.add(m);
//			this.info("Added to user " + friend.getLogin() + " message: [" + m.getMessage() + "]");
//		}
//
//		this.appendToLog("write_message_all " + user.getLogin() + " " + message);
//	
//		this.info("User: " + login + " posted the following message to all users " + message);
		
		// Nothing to return
		return null;
	}
	
	public String readMessagesAll(String token) throws FacebookException {
//		User user = getUserFromToken(token);
//		String login = user.getLogin();
//		
//		StringBuffer sb = new StringBuffer();
//		assert this.m_messages.containsKey(login);
//		
//		List<Message> listOfMessages = this.m_messages.get(login);
//		for (Message message : listOfMessages) {
//			sb.append("From:");
//			sb.append(message.getFromLogin());
//			sb.append('\n');
//			sb.append("Content:");
//			sb.append(message.getMessage());
//			sb.append('\n');
//		}
//
//		return sb.toString();
		return "";
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
