package node.rpc;
import java.util.Vector;
import node.reliable.ReliableDeliveryNode;
import edu.washington.cs.cse490h.lib.Utility;


public class RPCNode extends ReliableDeliveryNode {	
	/**
	 * Classes that override this class should use this method to call a method
	 * @param targetSender
	 * @param methodName
	 * @param params
	 */
	protected void callMethod(int targetSender, String methodName, Vector<String> params) {
		RPCMethodCall methodCall = new RPCMethodCall(methodName, params);
		
		StringBuffer sb = serialize(methodCall);
		
		super.sendReliableMessage(targetSender, Utility.stringToByteArray(sb.toString()));
	}
	
	@Override
	protected void onReliableMessageReceived(int from, byte[] msg) {
		StringBuffer sb = new StringBuffer(Utility.byteArrayToString(msg));
		RPCMethodCall methodCall = parseString(sb);
		onMethodCalled(methodCall.getMethodName(), methodCall.getParams());
	}
	
	/**
	 * Classes that override this class should use this method to detect when methods are being called
	 * @param methodName
	 * @param params
	 */
	protected void onMethodCalled (String methodName, Vector<String> params) {
		
	}
	
	public static StringBuffer serialize(RPCMethodCall methodCall) {
		StringBuffer sb = new StringBuffer();
		sb.append(methodCall.getMethodName().length());
		sb.append(" ");
		sb.append(methodCall.getMethodName());
		sb.append(" ");
		
		for	(int i = 0; i < methodCall.getParams().size(); i++) {
			String paramStr = methodCall.getParams().get(i).toString();
			sb.append(paramStr.length());
			sb.append(" ");
			sb.append(paramStr);
			sb.append(" ");
		}
		return sb;
	}
	
	public static RPCMethodCall parseString(StringBuffer sb) {
		RPCMethodCall methodCall = new RPCMethodCall();
		
		Vector<String> v = new Vector<String>();
		
		int i = 0;
		while (i < sb.length()) {
			int length = 0;
			while (sb.charAt(i) != ' ') {
				length = length*10 + (sb.charAt(i)-'0');
				i++;
			}
			
			i++;
			String valueString = sb.substring(i, i+length);
			i = i + length + 1;
			if (methodCall.getMethodName() == null) {
				methodCall.setMethodName(valueString);
			} else {
				v.add(valueString);
			}
		}
		
		methodCall.setParams(v);
		
		return methodCall;
	}
}
