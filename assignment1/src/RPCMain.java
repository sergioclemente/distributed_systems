import java.util.Vector;

public class RPCMain {
	public static void main(String args[]) {
		Vector<String> params = new Vector<String>();
		params.add("hey");
		params.add("jude");
		
		StringBuffer sb = RPCNode.serialize(new RPCMethodCall("foo", params));
		System.out.println(sb.toString());
		
		RPCMethodCall methodCall = RPCNode.parseString(sb);
		
	}
}
