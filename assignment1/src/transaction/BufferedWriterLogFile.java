package transaction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import edu.washington.cs.cse490h.lib.Node;
import edu.washington.cs.cse490h.lib.PersistentStorageOutputStream;

public class BufferedWriterLogFile implements ILogFile {

	private BufferedWriter m_writer;
	
	public BufferedWriterLogFile(BufferedWriter writer) {
		this.m_writer = writer;
	}
	
	@Override
	public void append(String str) throws IOException {
		this.m_writer.append(str);

	}

}
