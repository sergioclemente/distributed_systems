package node.reliable;
import java.util.HashSet;
import java.util.Hashtable;


public class SessionManager {
	private Hashtable<Integer, Session> m_currentSessions = new Hashtable<Integer, Session>();

	public Session getSession(Integer remoteEndpoint) {
		if (this.m_currentSessions.containsKey(remoteEndpoint)) {
			return this.m_currentSessions.get(remoteEndpoint);
		}
		Session session = new Session();
		this.m_currentSessions.put(remoteEndpoint, session);
		
		return session;
	}
	
}
