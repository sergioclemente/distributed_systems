package node.rpc;

import util.NodeSerialization;

public class SerializationTestNode extends RPCNode {
	private NodeSerialization serialization;
	
	public SerializationTestNode() {
		super(false);
		
		this.serialization = new NodeSerialization(this, "serialization.txt");
	}
	
	@Override
	public void onCommand(String command) {
		String[] parts = command.split(" ");
		String stmt = parts[0];
		String name = parts[1];
		
		if (stmt.equals("set")) {
			String value = parts[2];
			this.serialization.saveState(name, value);
		} else if (stmt.equals("read")){
			System.out.println(this.serialization.restoreState(name));
		}
	}
}
