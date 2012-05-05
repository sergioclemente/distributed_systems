package node.facebook;
import java.util.Hashtable;
import java.util.Vector;
import node.rpc.IFacebookServer;
import node.rpc.IFacebookServerReply;
import node.rpc.RPCException;


public class FacebookFrontendSystem extends BaseFacebookSystem implements IFacebookServerReply
{
	private Hashtable<Integer, IFacebookServer> m_stubs = new Hashtable<Integer, IFacebookServer>();

	/**
	 * FacebookFrontendSystem()
	 * @param node
	 */
	public FacebookFrontendSystem(FacebookRPCNode node) 
	{
		super(node);
	}

	/**
	 * onCommand()
	 * Invoked when this node receives a command string from the console
	 * 
	 * @param command
	 */
	public void onCommand(String command)
	{
		IFacebookServer stub;
		Integer serverAddr;

		String[] parts = command.split("\\s+");

		// Attempt to parse a server id from the first token.
		// This can be used either for debugging purposes, or we
		// could always auto-redirect to the right shard regardless
		// of the node receiving the command.
		// Currently we only auto-redirect if no specific shard was
		// provided in the command line.
		
		try
		{
			serverAddr = Integer.parseInt(parts[0]);
			
			// Clone the array but skip the first element 
			String[] parts2 = new String[parts.length-1];
			for (int i=1; i<parts.length; i++)
				parts2[i-1] = parts[i];
			
			parts = parts2;
		}
		catch (NumberFormatException ex)
		{
			// If no specific server was provided, auto-redirect 
			serverAddr = -1;
		}
		
		// Handle auto-redirection to the appropriate server,
		// unless a specific recipient was explicitly provided
		// (see above).
		
		int[] shards;
		if (serverAddr == -1)
		{
			shards = m_node.getAppropriateShards(parts[0], parts[1]);
		}
		else
		{
			shards = new int[] { serverAddr };
		}
		
		
		// Execute the command on every shard.
		// Most commands will run on only one shard, but write_message_all
		// will be sent to all of them.
		// TODO: write_message_all should only contact shards that actually
		// contain friends of the user.
		
		for (int shard : shards)
		{
			// Get the proper stub for communication with the server
			if (!m_stubs.containsKey(shard))
			{
				// NOTE: we create a different stub per server connection,
				// but in the end all replies are handled by the same
				// object ("this" one).
				stub = m_node.connectToFacebookServer(shard, this);
				m_stubs.put(shard, stub);
			}
			else
			{
				stub = m_stubs.get(shard);
			}
			
			// Invoke the command on the server
			try
			{
				switch (parts[0].toLowerCase())
				{
				case "login":
					stub.login(parts[1], parts[2]);
					break;
					
				case "logout":
					stub.logout(parts[1]);
					break;
					
				case "create_user":
					stub.createUser(parts[1], parts[2]);
					break;
					
				case "add_friend":
					stub.addFriend(parts[1], parts[2]);
					break;
					
				case "accept_friend":
					stub.acceptFriend(parts[1], parts[2]);
					break;
					
				case "write_message_one":
					// TODO: implement
					stub.writeMessageOne(parts[1], parts[2], parts[3]);
					break;
					
				case "write_message_all":
					String message = "";
					
					// The write_message_all command is special because it can contain spaces
					// TODO: not very clean approach. Think about how making this in the subclasses
					int idx = command.indexOf(' ');
					int nextIdx = command.indexOf(' ', idx+1);
					if (idx != -1 && nextIdx != -1) 
						message = command.substring(nextIdx, command.length()).trim();
	
					stub.writeMessageAll(parts[1], message);
					break;
					
				case "read_message_all":
					stub.readMessageAll(parts[1]);
					break;
					
				default:
					user_info(String.format("Unknown command: " + parts[0]));
					break;
				}
			}
			catch (RPCException ex)
			{
				// Never happens
			}
			catch (IndexOutOfBoundsException ex2)
			{
				user_info("Invalid syntax for command: " + parts[0]);
			}
		}
	}
	
	@Override
	public void reply_login(int replyId, int sender, int result, String reply)
	{
		Vector<String> v = new Vector<String>();
		v.add(((Integer) result).toString());
		v.add(reply);
		endClientCommand(sender, "login", v);
	}

	@Override
	public void reply_logout(int replyId, int sender, int result, String reply)
	{
		Vector<String> v = new Vector<String>();
		v.add(((Integer) result).toString());
		v.add(reply);
		endClientCommand(sender, "logout", v);
	}

	@Override
	public void reply_createUser(int replyId, int sender, int result, String reply)
	{
		Vector<String> v = new Vector<String>();
		v.add(((Integer) result).toString());
		v.add(reply);
		endClientCommand(sender, "createUser", v);
	}

	@Override
	public void reply_addFriend(int replyId, int sender, int result, String reply)
	{
		Vector<String> v = new Vector<String>();
		v.add(((Integer) result).toString());
		v.add(reply);
		endClientCommand(sender, "addFriend", v);
	}

	@Override
	public void reply_acceptFriend(int replyId, int sender, int result, String reply)
	{
		Vector<String> v = new Vector<String>();
		v.add(((Integer) result).toString());
		v.add(reply);
		endClientCommand(sender, "acceptFriend", v);
	}

	@Override
	public void reply_writeMessageOne(int replyId, int sender, int result, String reply)
	{
		Vector<String> v = new Vector<String>();
		v.add(((Integer) result).toString());
		v.add(reply);
		endClientCommand(sender, "writeMessageOne", v);
	}

	@Override
	public void reply_writeMessageAll(int replyId, int sender, int result, String reply)
	{
		Vector<String> v = new Vector<String>();
		v.add(((Integer) result).toString());
		v.add(reply);
		endClientCommand(sender, "writeMessageAll", v);
	}

	@Override
	public void reply_readMessageAll(int replyId, int sender, int result, String reply)
	{
		Vector<String> v = new Vector<String>();
		v.add(((Integer) result).toString());
		v.add(reply);
		endClientCommand(sender, "readMessageAll", v);
	}

	private void endClientCommand(int from, String methodName, Vector<String> params)
	{
		int result = Integer.parseInt(params.get(0));
		
		if (result != 0)
		{
			String errorMsg = String.format(FacebookRPCNode.ERROR_MESSAGE_FORMAT, methodName, from, result);
			user_info(String.format("NODE %d: %s", m_node.addr, errorMsg));
			user_info("Commands queued will be removed from list.");

			// TODO: allowing concurrent commands for now 
			//this._commandQueue.clear();
		} 
		else
		{
			String returnValue = params.size() == 2 ? params.get(1) : null;
			user_info("Server returned ok. returnValue=" + returnValue);

			// TODO: allowing concurrent commands for now 
			//popCommandAndExecuteNext();
		}
	}
	
}
