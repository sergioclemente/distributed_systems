package util;

import paxos.Acceptor;

public interface ISerialization {
	void saveState(String name, Object value);
	Object restoreState(String name);
}
