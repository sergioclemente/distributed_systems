package node.rpc;
import java.util.Vector;


public class RPCMethodCall 
{
	private String m_methodName;
	private Vector<String> m_params;
	
	public RPCMethodCall() 
	{
	}
	
	public RPCMethodCall(String methodName, Vector<String> params) 
	{
		this.m_methodName = methodName;
		this.m_params = params;
	}
	
	public RPCMethodCall(StringBuffer sb) 
	{
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
			if (getMethodName() == null) {
				setMethodName(valueString);
			} else {
				v.add(valueString);
			}
		}

		setParams(v);
	}
	
	public String getMethodName() 
	{
		return m_methodName;
	}
	
	public void setMethodName(String m_methodName) 
	{
		this.m_methodName = m_methodName;
	}
	
	public Vector<String> getParams() 
	{
		return m_params;
	}
	
	public void setParams(Vector<String> m_params) 
	{
		this.m_params = m_params;
	}
	
	/**
	 * serialize()
	 * @param methodCall
	 * @return
	 */
	public StringBuffer serialize() 
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getMethodName().length());
		sb.append(" ");
		sb.append(getMethodName());
		sb.append(" ");
		
		if (getParams() != null) {
			for	(int i = 0; i < getParams().size(); i++) {
				String paramStr = getParams().get(i).toString();
				sb.append(paramStr.length());
				sb.append(" ");
				sb.append(paramStr);
				sb.append(" ");
			}	
		}
		return sb;
	}
	
}
