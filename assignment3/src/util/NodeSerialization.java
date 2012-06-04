package util;
import java.io.IOException;
import java.util.Hashtable;
import node.rpc.RPCNode;
import edu.washington.cs.cse490h.lib.PersistentStorageWriter;

public class NodeSerialization implements ISerialization {
	private RPCNode node;
	private String filename;

	private class SerializationObject {
		private Hashtable<String, Object> values;
		
		public SerializationObject() {
			this.values = new Hashtable<String, Object>();
		}
		
		public void addPropertyValue(String name, Object value) {
			this.values.put(name, value);
		}
		
		public Object getPropertyValue(String name) {
			return this.values.containsKey(name) ? this.values.get(name) : null;
		}
	}
	
	public NodeSerialization(RPCNode node, String filename) {
		this.node = node;
		this.filename = filename;
	}
	
	@Override
	public void saveState(String name, Object value) {
		SerializationObject obj = (SerializationObject)SerializationUtil.deserialize(this.readAllLines(), SerializationObject.class);
		
		if (obj == null) {
			obj = new SerializationObject();
		}
		
		obj.addPropertyValue(name, value);
		
		String contentString = SerializationUtil.serialize(obj);
	
		this.updateFileContents(this.filename, contentString, false);
	}

	@Override
	public Object restoreState(String name) {
		SerializationObject obj = (SerializationObject)SerializationUtil.deserialize(this.readAllLines(), SerializationObject.class);
		
		return obj != null ? obj.getPropertyValue(name) : null;
	}
	
	private void updateFileContents(String filename, String contents, boolean append) {
		try {
			// TODO: not reading the .tmp file on startup
			
			String tempFile = filename + ".tmp";
			
			// read old file
			String oldContents = this.readAllLines();

			// write old to backup
			if (oldContents != null) {
				PersistentStorageWriter psw_bck = this.node.getWriter(tempFile, false);
				psw_bck.write(oldContents);
				psw_bck.close();
			}

			// update new
			PersistentStorageWriter psw = this.node.getWriter(filename, append);
			psw.write(contents);
			psw.close();

			// delete temporary file
			if (oldContents != null) {
				PersistentStorageWriter f = this.node.getWriter(tempFile, false);
				f.delete();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String readAllLines() {
		return NodeUtility.readAllLines(this.node, filename);
	}
}
