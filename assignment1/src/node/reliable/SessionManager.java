package node.reliable;

import java.util.Hashtable;


public class SessionManager {
	private Hashtable<Integer, Session> m_sendSessions = new Hashtable<Integer, Session>();
	private Hashtable<Integer, Session> m_recvSessions = new Hashtable<Integer, Session>();
	
	public SessionManager() {
	}

	public Session createSession()
	{
		return new Session();
	}
	
	public Session getSendSession(Integer remoteEndpoint) {
		Session session;

		if (this.m_sendSessions.containsKey(remoteEndpoint)) {
			session = this.m_sendSessions.get(remoteEndpoint);
		} else {
			session = null;
			//session = new Session();
			//this.m_sendSessions.put(remoteEndpoint, session);
		}
		return session;
	}

	public Session getRecvSession(Integer remoteEndpoint) {
		Session session;

		if (this.m_recvSessions.containsKey(remoteEndpoint)) {
			session = this.m_recvSessions.get(remoteEndpoint);
		} else {
			session = null;
			//session = new Session();
			//this.m_recvSessions.put(remoteEndpoint, session);
		}
		return session;
	}

	public Session getOutboundSession(int connectionId) {
		Session session;

		if (this.m_sendSessions.containsKey(connectionId)) {
			session = this.m_sendSessions.get(connectionId);
		} else {
			session = new Session();
			this.m_sendSessions.put(connectionId, session);
		}
		return session;
	}

	public Session getInboundSession(int connectionId) {
		Session session;

		if (this.m_recvSessions.containsKey(connectionId)) {
			session = this.m_recvSessions.get(connectionId);
		} else {
			session = new Session();
			this.m_recvSessions.put(connectionId, session);
		}
		return session;
	}
}
