package node.storage;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ListIterator;
import java.util.Vector;
import node.rpc.IStorageServer;
import node.rpc.RPCException;
import node.rpc.RPCNode;
import edu.washington.cs.cse490h.lib.PersistentStorageReader;
import edu.washington.cs.cse490h.lib.PersistentStorageWriter;
import edu.washington.cs.cse490h.lib.Utility;


public class StorageSystemServer implements IStorageServer
{
	private static final String TEMP_FILE = ".temp";

	private static final String ERROR_MESSAGE_FORMAT = 
			"Error: %s on server %d and file %s returned error code %s";
	
	private static final int FILE_DOES_NOT_EXIST = 10;	
	private static final int FILE_ALREADY_EXISTS = 11;
	private static final int FILE_TOO_LARGE = 30;		
	private static final int IO_EXCEPTION = 40;
	
	private static final int MAX_MESSAGE_SIZE = 1500;
	
	private RPCNode m_node;
	
	public StorageSystemServer(RPCNode node)
	{
		m_node = node;
	}
	
	/**
	 * API: createFile()
	 * @param fileName
	 */
	public String createFile(String fileName) throws RPCException
	{		
		if (Utility.fileExists(m_node, fileName))
		{				
			throw new RPCException(FILE_ALREADY_EXISTS, null);
		}

		try 
		{			
			m_node.getWriter(fileName, false);
			return null;
		} 
		catch (IOException e) 
		{
			throw new RPCException(IO_EXCEPTION, null);
		}
	}
	
	/**
	 * API: getFile()
	 * @param fileName
	 */
	public String getFile(String fileName) throws RPCException
	{
		if (!Utility.fileExists(m_node, fileName))
		{
			throw new RPCException(FILE_DOES_NOT_EXIST, null);
		}

		try
		{
			String contents = readFileContents(fileName);
			return contents;
		}
		catch (IOException e) 
		{
			throw new RPCException(IO_EXCEPTION, null);
		}
	}	
	
	/**
	 * putFile()
	 * @param fileName
	 * @param contents
	 */
	public String putFile(String fileName, String contents) throws RPCException
	{
		if (!Utility.fileExists(m_node, fileName))
		{
			throw new RPCException(FILE_DOES_NOT_EXIST, null);
		}
		
		if (contents.getBytes().length > MAX_MESSAGE_SIZE)
		{
			throw new RPCException(FILE_TOO_LARGE, null);
		}
		
		try {
			updateFileContents(fileName, contents, false);
			return null;
		} catch (IOException e) {
			throw new RPCException(IO_EXCEPTION, null);
		}
	}
	
	/**
	 * API: appendToFile()
	 * @param fileName
	 * @param contents
	 */
	public String appendToFile(String fileName, String contents) throws RPCException
	{
		try {						
			if (!Utility.fileExists(m_node, fileName))
			{
				throw new RPCException(FILE_DOES_NOT_EXIST, null);
			}
			
			if (readFileContents(fileName).getBytes().length > MAX_MESSAGE_SIZE)
			{
				throw new RPCException(FILE_TOO_LARGE, null);
			}
			
			updateFileContents(fileName, contents, true);			
			return null;
		} catch (IOException e) {
			throw new RPCException(IO_EXCEPTION, null);
		}
	}

	/**
	 * API: deleteFile()
	 * @param fileName
	 */
	public String deleteFile(String fileName) throws RPCException
	{
		if (!Utility.fileExists(m_node, fileName))
		{
			throw new RPCException(FILE_DOES_NOT_EXIST, null);
		}
		
		try {
			PersistentStorageWriter writer = m_node.getWriter(fileName, false);
			writer.delete();
			return null;
		} catch (IOException e) {
			throw new RPCException(IO_EXCEPTION, null);
		}
	}
	
	/**
	 * Parses the command received by onCommand and actually executes it.
	 * 
	 * @param command - the command to be executed.
	 */
	/*
	@Override
	protected String executeClientCommand(String command)
	{
		String[] parts = command.split("\\s+");
		
		// at least command target fileName
		if (parts.length >= 3)
		{
			String commandName = parts[0];
			int targetSender = Integer.parseInt(parts[1]);
			String fileName = parts[2];
			
			if (commandName.equals("create"))
			{
				beginCreateFile(targetSender, fileName);
			}
			else if (commandName.equals("get"))
			{
				beginGetFile(targetSender, fileName);
			}
			else if (commandName.equals("put") || commandName.equals("append"))
			{							
				if (parts.length < 4)
				{
					warn(String.format("Invalid number of arguments for 'put' or 'append'. Expected at least 4. Found: %1", parts.length));
					popCommandAndExecuteNext();
				}
				
				StringBuilder contents = new StringBuilder();
				
				for (int i = 3; i < parts.length; i++) {
					contents.append(parts[i]);
					// BUG: append " "?
				}
				
				int contentsSize = contents.toString().getBytes().length;
				
				if (contentsSize > MAX_MESSAGE_SIZE)
				{
					error(String.format("Trying to send information about the max message size. Max size: %d, Actual size: %d", 
							MAX_MESSAGE_SIZE, contentsSize));
				}
				
				if (commandName.equals("put"))
				{
					beginPutFile(targetSender, fileName, contents.toString());
				}
				else
				{
					beginAppendFile(targetSender, fileName, contents.toString());
				}
			}
			else if (commandName.equals("delete"))
			{
				beginDeleteFile(targetSender, fileName);
			}
			else
			{
				warn(String.format("Unknown command: %1", commandName));
			}
		}
		else
		{
			// Will remove this command from the queue and executes the next one, if any
			popCommandAndExecuteNext();
		}
	}
	*/
	
	/**
	 * Executes commands directed to the server version of the Storage System.
	 * 
	 * @param from - who invoked the server.
	 * @param methodName - the method being invoked.
	 * @param params - any parameters to the method.
	 */
	/*
	private void executeServerCommand(String methodName,
			Vector<String> params) {
		if (params.size() < 1)
		{
			error(String.format("Invalid number of arguments passed. Expected 1, found %d", params.size()));
		}
		
		String fileName = params.get(0);
		
		if (methodName.equals("create"))
		{			
			createFile(from, fileName);
		}
		else if (methodName.equals("get"))
		{			
			getFile(from, fileName);
		}
		else if (methodName.equals("put") || methodName.equals("append"))
		{
			if (params.size() < 2)
			{
				error(String.format("Invalid number of arguments passed. Expected 2, found %d", params.size()));
			}
			
			StringBuilder contents = new StringBuilder();
			ListIterator<String> iterator = params.listIterator(1);
			while (iterator.hasNext())
			{
				contents.append(iterator.next());
			}
			
			if (methodName.equals("put"))
			{
				putFile(from, fileName, contents.toString());
			}
			else 
			{
				appendToFile(from, fileName, contents.toString());
			}
		}
		else if (methodName.equals("delete"))
		{
			deleteFile(from, fileName);
		}
		else
		{
			error(String.format("Unknown command: %d", methodName));
		}
	}
	
	private void endClientCommand(String methodName, Vector<String> params)
	{
		if (params.size() == 2 && params.get(0).equals("error"))
		{
			error(String.format("NODE %d: %s", addr, params.get(1)));
			info("Commands queued will be removed from list.");
			m_node._commandQueue.clear();
			return;
		}		
		else if (methodName.equals("endGetFile"))
		{
			info(params.get(0));
		}
		
		// Will remove this command from the queue and executes the next one, if any
		popCommandAndExecuteNext();
	}
	*/

	// some helpers for transaction
	private String readAllLines(String filename)  {
		try {
			PersistentStorageReader psr = m_node.getReader(filename);
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
			PersistentStorageWriter psw_bck = m_node.getWriter(".temp", false);
			psw_bck.write(filename + "\n" + oldFile);
			psw_bck.close();

			// update new
			PersistentStorageWriter psw = m_node.getWriter(filename, append);
			psw.write(contents);
			psw.close();

			// delete temporary file
			PersistentStorageWriter f = m_node.getWriter(TEMP_FILE, false);
			f.delete();
		} catch (IOException e) {
			throw e;
		}
	}
	
	public void appendFileContents(String filename, String contents) throws IOException {
		try {
			// update new
			PersistentStorageWriter psw = m_node.getWriter(filename, true);
			psw.write(contents);
			psw.close();
		} catch (IOException e) {
			throw e;
		}
	}
	

	private String readFileContents(String fileName)
			throws FileNotFoundException, IOException {
		BufferedReader bufferedReader = m_node.getReader(fileName);
		
		StringBuffer contents = new StringBuffer();
		
		String line = bufferedReader.readLine();  
		while (line != null)
		{
			contents.append(line);
			
			line = bufferedReader.readLine();
		}
		
		bufferedReader.close();
		return contents.toString();
	}
	


	
	private void recoverTempFileFromCrash() {
		try {
			if (Utility.fileExists(m_node, TEMP_FILE)) {
				PersistentStorageReader psw_bck = m_node.getReader(TEMP_FILE);
				if (!psw_bck.ready()) {
					PersistentStorageWriter f = m_node.getWriter(TEMP_FILE, false);
					f.delete();
				} else {
					String filename = psw_bck.readLine();
					psw_bck.close();

					String oldContents = readAll(psw_bck);
					PersistentStorageWriter psw = m_node.getWriter(filename, false);
					psw.write(oldContents);
					psw.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}