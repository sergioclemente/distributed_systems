package node.rpc;
import java.util.Vector;


public class RPCMethodCall {
	private String m_methodName;
	private Vector<String> m_params;
	
	public RPCMethodCall(String methodName, Vector<String> params) {
		this.m_methodName = methodName;
		this.m_params = params;
	}
	
	public RPCMethodCall() {
	}
	
	public String getMethodName() {
		return m_methodName;
	}
	
	public void setMethodName(String m_methodName) {
		this.m_methodName = m_methodName;
	}
	
	public Vector<String> getParams() {
		return m_params;
	}
	
	public void setParams(Vector<String> m_params) {
		this.m_params = m_params;
	}
}
