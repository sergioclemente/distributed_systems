package node.facebook;

import java.io.IOException;
import java.util.Vector;
import node.rpc.RPCMethodCall;
import edu.washington.cs.cse490h.lib.PersistentStorageReader;
import edu.washington.cs.cse490h.lib.PersistentStorageWriter;
import edu.washington.cs.cse490h.lib.Utility;

public abstract class BaseFacebookSystem {
	private static final String FILE_NAME = "facebookdb.txt";

	protected boolean m_inRecovery = false;
	protected FacebookRPCNode m_node;
	
	public BaseFacebookSystem(FacebookRPCNode node) {
		this.m_node = node;
	}
	
	public void recoverFromCrash() {
		try {
			m_inRecovery = true;
			if (Utility.fileExists(this.m_node, FILE_NAME)) {
				PersistentStorageReader psr = this.m_node.getReader(FILE_NAME);
				String line;
				while ((line = psr.readLine()) != null) {
					user_info("Recovery: replaying command from log: " + line);
					RPCMethodCall methodCall = parseRPCMethodCall(line);
					// TODO: fix recovery
					//callLocalMethod(methodCall.getMethodName(), methodCall.getParams());
				}
				psr.close();				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			m_inRecovery = false;
		}
	}
	
	//TODO: if you fix the TODOs below, do it for TwoPhaseCommitNode.saveContext too.
	protected void appendToLog(String content) {
		// Don't append to the log if in recovery mode
		if (!m_inRecovery) {
			try {
				// TODO: use IStorageServer instead?
				PersistentStorageWriter psw = m_node.getWriter(FILE_NAME, true);
				psw.write(content);
				psw.close();
			} catch (IOException e) {
				// TODO: return the proper error
				e.printStackTrace();
			}
		}
	}

	protected void user_info(String s) {
		// Use a different prefix to be easy to distinguish
		System.out.println(">>>> " + this.m_node.addr + " - "+ s);
	}
	
	protected RPCMethodCall parseRPCMethodCall(String command) {
		RPCMethodCall methodCall = new RPCMethodCall();
		
		String[] parts = command.toLowerCase().split("\\s+");
		
		String methodName = parts[0];
		Vector<String> params = new Vector<String>();
		
		// The write_message_all command is special because it can contain spaces
		// TODO: not very clean approach. Think about how making this in the subclasses
		if (methodName.startsWith("write_message_all")) {
			int idx = command.indexOf(' ');
			int nextIdx = command.indexOf(' ', idx+1);
			
			if (idx != -1 && nextIdx != -1) {
				String login = command.substring(idx, nextIdx).trim();
				String msg = command.substring(nextIdx, command.length()).trim();
				params.add(login);
				params.add(msg);
			}
		} else {
			for (int i = 1; i < parts.length; i++) {
				params.add(parts[i]);
			}
		}

		methodCall.setParams(params);
		methodCall.setMethodName(parts[0]);
		
		return methodCall;
	}

}
