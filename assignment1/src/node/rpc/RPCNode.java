package node.rpc;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;
import node.reliable.ReliableDeliveryNode;
import edu.washington.cs.cse490h.lib.PersistentStorageReader;
import edu.washington.cs.cse490h.lib.PersistentStorageWriter;
import edu.washington.cs.cse490h.lib.Utility;


public class RPCNode extends ReliableDeliveryNode {	
	private static final String TEMP_FILE = ".temp";
	
	private Queue<String> _commandQueue;
	
	public RPCNode()
	{
		_commandQueue = new LinkedList<String>();
	}
	
	/**
	 * Parses the commands sent to the client by the simulator or the emulator.
	 */
	@Override
	public void onCommand(String command)
	{
		// If this is the server, it doesn't handle onCommand.
		if (isServer()) return;
		
		_commandQueue.add(command);
		
		if (_commandQueue.size() == 1)
		{
			executeClientCommand(command);
		}								
	}
	
	@Override
	public void start() {
		super.start();
		recoverFromCrash();
	}

	/**
	 * Classes that override this class should use this method to send a message
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
		onMethodCalled(from, methodCall.getMethodName(), methodCall.getParams());
	}

	/**
	 * Classes that override this class should use this method to detect when methods are being called
	 * @param methodName
	 * @param params
	 */
	protected void onMethodCalled (int from, String methodName, Vector<String> params) {

	}

	public static StringBuffer serialize(RPCMethodCall methodCall) {
		StringBuffer sb = new StringBuffer();
		sb.append(methodCall.getMethodName().length());
		sb.append(" ");
		sb.append(methodCall.getMethodName());
		sb.append(" ");
		
		if (methodCall.getParams() != null) {
			for	(int i = 0; i < methodCall.getParams().size(); i++) {
				String paramStr = methodCall.getParams().get(i).toString();
				sb.append(paramStr.length());
				sb.append(" ");
				sb.append(paramStr);
				sb.append(" ");
			}	
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

	/**
	 * Removes the current command from the queue and executes the next command, if there is one.
	 */
	protected void endCommand()
	{
		// Removes the head
		_commandQueue.remove();
		
		// Gets the next element in the queue (new head) and executes it
		String command = _commandQueue.peek();		
		if (command != null)
		{
			executeClientCommand(command);
		}
	}
	
	protected void executeClientCommand(String command)
	{
		
	}
	
	/**
	 * Because the framework doesn't allow us to have two different types running at the same time, we need to implement
	 * both client and server together.
	 * 
	 * To differentiate between one and another, we are assuming that the server has a addr = 0.
	 */
	protected boolean isServer()
	{
		return addr == 0;
	}
	
	/**
	 * Sub classes will call this method in order to store files on disk. it already takes care of crashes
	 * @param filename
	 * @param contents
	 * @throws IOException
	 */
	public void updateFileContents(String filename, String contents, boolean append) throws IOException {
		try {
			// read old file
			String oldFile = readAllLines(filename);

			// write old to backup
			PersistentStorageWriter psw_bck = this.getWriter(".temp", false);
			psw_bck.write(filename + "\n" + oldFile);
			psw_bck.close();

			// update new
			PersistentStorageWriter psw = this.getWriter(filename, append);
			psw.write(contents);
			psw.close();

			// delete temporary file
			PersistentStorageWriter f = this.getWriter(TEMP_FILE, false);
			f.delete();
		} catch (IOException e) {
			throw e;
		}
	}
	
	public void appendFileContents(String filename, String contents) throws IOException {
		try {
			// update new
			PersistentStorageWriter psw = this.getWriter(filename, true);
			psw.write(contents);
			psw.close();
		} catch (IOException e) {
			throw e;
		}
	}
	
	private void recoverFromCrash() {
		try {
			if (Utility.fileExists(this, TEMP_FILE)) {
				PersistentStorageReader psw_bck = this.getReader(TEMP_FILE);
				if (!psw_bck.ready()) {
					PersistentStorageWriter f = this.getWriter(TEMP_FILE, false);
					f.delete();
				} else {
					String filename = psw_bck.readLine();
					psw_bck.close();

					String oldContents = readAll(psw_bck);
					PersistentStorageWriter psw = this.getWriter(filename, false);
					psw.write(oldContents);
					psw.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// some helpers for transaction
	private String readAllLines(String filename)  {
		try {
			PersistentStorageReader psr = this.getReader(filename);
			return readAll(psr);
		} catch (FileNotFoundException e) {
			return null;
		}

	}

	private String readAll(PersistentStorageReader psr) {
		StringBuffer sb = new StringBuffer();

		String line;
		try {
			while ((line=psr.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}	
}
