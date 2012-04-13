package node.facebook;

import java.io.IOException;

import edu.washington.cs.cse490h.lib.PersistentStorageWriter;

// TODO (sergioclemente): Not the cleanest, should think in a better way to do this
public interface INode {
	// Logging API
	public void warn(String msg);	
	public void info(String msg);

	// Storage API
	public void updateFileContents(String filename, String contents, boolean append) throws IOException;
	public void appendFileContents(String filename, String contents) throws IOException;
}
