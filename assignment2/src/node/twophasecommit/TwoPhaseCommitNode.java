package node.twophasecommit;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.UUID;
import java.util.Vector;

import edu.washington.cs.cse490h.lib.Callback;
import edu.washington.cs.cse490h.lib.PersistentStorageReader;
import edu.washington.cs.cse490h.lib.PersistentStorageWriter;

import node.rpc.I2pcCoordinator;
import node.rpc.I2pcCoordinatorReply;
import node.rpc.I2pcParticipant;
import node.rpc.I2pcParticipantReply;
import node.rpc.RPCException;
import node.rpc.RPCNode;
import node.facebook.FacebookException;


// TODO: this is not a node anymore, rename to something else. ThoPhaseCommitCoordinator?
public class TwoPhaseCommitNode implements I2pcCoordinator, I2pcParticipant, I2pcCoordinatorReply, I2pcParticipantReply 
{		
	private RPCNode m_node;
	private Hashtable<Integer, I2pcParticipant> m_stubs = new Hashtable<Integer, I2pcParticipant>();
	private Method m_waitForVotesTimeoutMethod;
	private Method m_waitForDecisionTimeoutMethod;
	protected boolean m_inRecovery = false;
	
	/**
	 * We need to keep the history of two phase commit for the case of a node waking up and asking us for data about
	 * an old node.
	 */
	private Dictionary<UUID, TwoPhaseCommitContext> _twoPhaseCommitContexts;
	
	/**
	 * Constructor.
	 * 
	 * Restores the node state from the log from a Two Phase Commit point of view.
	 */
	public TwoPhaseCommitNode(RPCNode node)
	{
		m_node = node;
		
		_twoPhaseCommitContexts = new Hashtable<UUID, TwoPhaseCommitContext>();
		
		recover();
		
		try {
			this.m_waitForVotesTimeoutMethod = Callback.getMethod("onWaitForVotesTimeout", this, new String[] { "java.util.UUID" });
			this.m_waitForDecisionTimeoutMethod = Callback.getMethod("onWaitForDecisionTimeout", this, new String[] { "java.util.UUID" });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void onMethodCalled(int from, String methodName, Vector<String> params) {
		if (methodName.equals("vote-request"))
		{
			receiveVoteRequest(from, params);
		}
		else if (methodName.equals("send-vote"))
		{
			receiveVote(from, params);
		}
		else if (methodName.equals("abort"))
		{
			receiveAbort(from, params);
		}
		else if (methodName.equals("commit"))
		{
			receiveCommit(from, params);
		}
		else if (methodName.equals("decision-req"))
		{
			receiveDecisionRequest(from, params);
		}
	}

	
	public UUID startTransaction()
	{
		TwoPhaseCommitContext newContext = new TwoPhaseCommitContext(m_node.addr);
		_twoPhaseCommitContexts.put(newContext.getId(), newContext);
		return newContext.getId();
	}

	public void addParticipant(UUID transactionId, int participantId) throws FacebookException
	{
		TwoPhaseCommitContext context;
		context = _twoPhaseCommitContexts.get(transactionId);
		context.addParticipant(participantId);
	}
	
	/**
	 * Starts a new two phase commit process. It tries to execute the passed command on the passed list of 
	 * participants.
	 * 
	 * @param participants - The list of participants that will participate in the two phase commit.
	 * @param command - The command to be executed.
	 * @param params - Any params to the command above.
	 */
	public void startTwoPhaseCommit(UUID transactionId)
	{
		TwoPhaseCommitContext context;
		context = _twoPhaseCommitContexts.get(transactionId);
		
		context.setInProgress(true);
		
		// TODO: remove - write_message_all is responsible for blocking concurrent transactions
		/*
		if (anyTwoPhaseCommitPending(context))
		{
			// LICAVALC: is just the fact that the coordinator decided enough? Do we have to wait for it to notify
			// participants? I don't think so, right?
			abortTwoPhaseCommit(context.getId());
			return;
		}
		*/
		
		Callback cb = new Callback(this.m_waitForVotesTimeoutMethod, this, new Object[] { context.getId() });
		m_node.addTimeout(cb, context.getTimeout());
		
		for (Participant participant : context.getParticipants()) {
			// TODO: why do we need to send the list of participants? 
			beginVoteRequest(participant.getId(), context.getId(), /*context.getParticipants()*/ new Vector<String>());
		}
				
		saveContext(context);
	}
	
	public void abortTwoPhaseCommit(UUID twoPhaseCommitContextId)
	{
		TwoPhaseCommitContext context = _twoPhaseCommitContexts.get(twoPhaseCommitContextId);
		
		if (context == null)
		{
			m_node.error("abortTwoPhaseCommit: context not found.");
			return;
		}
		
		context.setDecision(Decision.Abort);
		saveContext(context);
		
		Vector<Participant> participantsWhoVotedYes = context.getAllParticipantsWhoVotedYes();	
		for (Participant participant : participantsWhoVotedYes) {
			beginSendAbort(participant.getId(), context.getId());
		}
		
		
		context.setFinished(true);
		saveContext(context);
	}	
	
	public void beginSendAbort(int targetSender, UUID twoPhaseCommitId) {
		Vector<String> params = new Vector<String>(1);
		
		params.add(twoPhaseCommitId.toString());
		
		callMethod(targetSender, "abort", params);
	}
	
	public void receiveAbort(int from, Vector<String> params)
	{
		if (params.size() < 1)
		{
			m_node.error("receiveAbort: wrong number of params.");
			return;
		}
		
		UUID twoPhaseCommitContextId = UUID.fromString(params.get(0));
		
		TwoPhaseCommitContext context = _twoPhaseCommitContexts.get(twoPhaseCommitContextId);
		
		if (context == null) 
		{
			m_node.error("receiveAbort: context not found.");
			return;
		}
		
		context.setDecision(Decision.Abort);
		saveContext(context);
		
		abortOrCommit(context, true);
	}

	private void abortOrCommit(TwoPhaseCommitContext context, boolean abort) {
		if (abort)
		{
			m_node.abort();
		}
		else
		{
			m_node.commit();
		}
		
		context.getParticipant(m_node.addr).setFinished(true);
		saveContext(context);
	}

	private void commitTwoPhaseCommit(UUID twoPhaseCommitContextId) {
		TwoPhaseCommitContext context = _twoPhaseCommitContexts.get(twoPhaseCommitContextId);
		
		if (context == null)
		{
			m_node.error("commitTwoPhaseCommit: context not found.");
			return;
		}		
		
		context.setDecision(Decision.Commit);
		saveContext(context);
		
		Vector<Participant> participants = context.getParticipants();	
		for (Participant participant : participants) {
			beginSendCommit(participant.getId(), context.getId());
		}
		
		context.setFinished(true);
		saveContext(context);
	}
	
	public void beginSendCommit(int targetSender, UUID twoPhaseCommitId) {
		Vector<String> params = new Vector<String>(1);
		
		params.add(twoPhaseCommitId.toString());
		
		callMethod(targetSender, "commit", params);
	}
	
	public void receiveCommit(int from, Vector<String> params)
	{
		if (params.size() < 1)
		{
			m_node.error("receiveCommit: wrong number of params.");
			return;
		}
		
		UUID twoPhaseCommitContextId = UUID.fromString(params.get(0));
		
		TwoPhaseCommitContext context = _twoPhaseCommitContexts.get(twoPhaseCommitContextId);
		
		if (context == null) 
		{
			m_node.error("receiveCommit: context not found.");
			return;
		}
		
		context.setDecision(Decision.Commit);
		saveContext(context);
		
		abortOrCommit(context, false);
	}

	public void beginVoteRequest(int targetSender, UUID twoPhaseCommitId, Vector<String> participants)
	{
		Vector<String> params = new Vector<String>();		
		
		params.add(twoPhaseCommitId.toString());
		
		params.add(participants.size() + "");
		params.addAll(participants);
		
		//TODO-licavalc: need to put each of these calls in a separate queue per participant or make this a multicast
		callMethod(targetSender, "vote-request", params);
	}
	
	/**
	 * Method on the participant that handles the Vote Request.
	 * 
	 * @param from - The coordinator
	 * @param params - should receive the two phase commit context id, the number of participants, the list of 
	 * participants, the command and any params to the command.
	 */
	public void receiveVoteRequest(int from, Vector<String> params)
	{
		// TODO-livar: handle case where participant receives multiple vote requests
		
		UUID twoPhaseCommitContextId = UUID.fromString(params.get(0));
		
		TwoPhaseCommitContext newContext;
		newContext = new TwoPhaseCommitContext(twoPhaseCommitContextId, from);
		_twoPhaseCommitContexts.put(newContext.getId(), newContext);
		
		try {
			// Add itself as a participant for book-keeping purposes
			newContext.addParticipant(m_node.addr);
		} catch (FacebookException e) {
			// won't happen - single participant
		}
		
		boolean saved = m_node.saveToDisk();
		
		if (anyTwoPhaseCommitPending(newContext) || !saved)
		{
			newContext.getParticipant(m_node.addr).setVote(Vote.No);
			newContext.setDecision(Decision.Abort);
			saveContext(newContext);
			
			abortOrCommit(newContext, true);
			
			beginSendVote(from, twoPhaseCommitContextId, Vote.No);
			return;
		}		
		
		newContext.getParticipant(m_node.addr).setVote(Vote.Yes);
		saveContext(newContext);
		
		Callback cb = new Callback(this.m_waitForDecisionTimeoutMethod, this, new Object[] { newContext.getId() });
		m_node.addTimeout(cb, newContext.getTimeout());
		
		beginSendVote(from, twoPhaseCommitContextId, Vote.Yes);					
	}
	
	private void startTerminationProtocol(UUID twoPhaseCommitContextId)
	{
		TwoPhaseCommitContext context = _twoPhaseCommitContexts.get(twoPhaseCommitContextId);
		
		if (context == null)
		{
			m_node.error("startTerminationProtocol: context not found.");
			return;
		}
		
		Callback cb = new Callback(this.m_waitForDecisionTimeoutMethod, this, new Object[] { context.getId() });
		m_node.addTimeout(cb, context.getTimeout());
		
		//TODO: this would be better if it were a MULTICAST, just like at startTwoPhaseCommit
		for (Participant participant : context.getParticipants()) {
			if (participant.getId() != m_node.addr)
			{
				beginDecisionRequest(participant.getId(), context.getId());
			}
		}
	}
	
	private void beginDecisionRequest(int targetSender, UUID twoPhaseCommitId) 
	{
		Vector<String> params = new Vector<String>(2);
		
		params.add(twoPhaseCommitId.toString());
		
		callMethod(targetSender, "decision-req", params);
	}
	
	//TODO-luciano: need to add this method to the I2pcParticipant interface and implementation.
	private void receiveDecisionRequest(int from, Vector<String> params)
	{
		if (params.size() < 1)
		{
			m_node.error("receiveDecisionRequest: wrong number of params.");
			return;
		}
		
		UUID twoPhaseCommitContextId = UUID.fromString(params.get(0));
		
		TwoPhaseCommitContext context = _twoPhaseCommitContexts.get(twoPhaseCommitContextId);
		
		if (context == null) 
		{
			m_node.error("receiveDecisionRequest: context not found.");
			return;
		}
		
		if (context.getDecision() == Decision.Abort)
		{
			beginSendAbort(from, twoPhaseCommitContextId);
		}
		else if (context.getDecision() == Decision.Commit)
		{
			beginSendCommit(from, twoPhaseCommitContextId);
		}
	}

	/**
	 * Method on the participant that sends the vote to the coordinator.
	 * 
	 * @param targetSender - the address of the coordinator.
	 * @param twoPhaseCommitId - Identifier of the Two Phase Commit instance that will receive the vote.
	 * @param vote - The Participant actual Vote.
	 */
	public void beginSendVote(int targetSender, UUID twoPhaseCommitId, Vote vote)
	{
		Vector<String> params = new Vector<String>(2);
		
		params.add(twoPhaseCommitId.toString());
		params.add(vote.toString());
		
		callMethod(targetSender, "send-vote", params);
	}
	
	/**
	 * Method on the coordinator that handles Votes from the participants.
	 * 
	 * @param from - the participant who sent the vote.
	 * @param params - the two phase commit context id and the vote from the participant.
	 */
	public void receiveVote(int from, Vector<String> params)
	{
		if (params.size() < 2)
		{
			m_node.error("receiveVote: wrong number of params.");
			return;
		}
		
		UUID twoPhaseCommitContextId = UUID.fromString(params.get(0));
		
		TwoPhaseCommitContext context = _twoPhaseCommitContexts.get(twoPhaseCommitContextId);
		
		if (context == null) 
		{
			m_node.error("receiveVote: context not found.");
			return;
		}
		
		Participant participant = context.getParticipant(from);
		
		if (participant.getVote() == Vote.None)
		{
			String vote = params.get(1);
			
			if (vote.equals("Yes"))
			{
				participant.setVote(Vote.Yes);
			}
			else if (vote.equals("No"))
			{
				participant.setVote(Vote.No);				
			}
			else
			{
				m_node.error("receiveVote: Unknown vote type");
				return;
			}
		}
		else
		{
			m_node.warn("receiveVote: already received vote for participant.");
		}
		
		switch(context.getDecisionBasedOnVotes())
		{
			case Commit:
				commitTwoPhaseCommit(twoPhaseCommitContextId);
				break;
			case Abort:
				abortTwoPhaseCommit(twoPhaseCommitContextId);
				break;
			default:
				m_node.info("receiveVote: haven't decided yet.");
		}		
	}	

	public void onWaitForVotesTimeout(UUID twoPhaseCommitContextId)
	{
		abortTwoPhaseCommit(twoPhaseCommitContextId);
	}
	
	public void onWaitForDecisionTimeout(UUID twoPhaseCommitContextId)
	{
		TwoPhaseCommitContext context = _twoPhaseCommitContexts.get(twoPhaseCommitContextId);
		
		if (context == null)
		{
			m_node.error("startTerminationProtocol: context not found.");
			return;
		}
		
		// timed out and we didn't get to a decision, start timer again
		if (context.getDecision() == Decision.NotDecided)
		{
			startTerminationProtocol(twoPhaseCommitContextId);
		}
		
		// if we already reached a decision, don't have to do anything else.
	}
	
	// TODO: nao precisa desse metodo, o write_message_all de cada node tem que ser capaz de
	// detectar por si soh se ha outro 2pc rolando.  
	private boolean anyTwoPhaseCommitPending(TwoPhaseCommitContext newContext) {
		Enumeration<TwoPhaseCommitContext> twoPhaseCommitContexts = _twoPhaseCommitContexts.elements();
		while(twoPhaseCommitContexts.hasMoreElements())
		{
			TwoPhaseCommitContext twoPhaseCommitContext = twoPhaseCommitContexts.nextElement();
			
			// That means there is a pending two phase commit in progress - abort this one in this case
			if (twoPhaseCommitContext != newContext && twoPhaseCommitContext.getDecision() == Decision.NotDecided)
			{				
				return true;
			}
		}
		
		return false;
	}
	
	private TwoPhaseCommitContext getPendingTwoPhaseCommit()
	{
		Enumeration<TwoPhaseCommitContext> twoPhaseCommitContexts = _twoPhaseCommitContexts.elements();
		while(twoPhaseCommitContexts.hasMoreElements())
		{
			TwoPhaseCommitContext twoPhaseCommitContext = twoPhaseCommitContexts.nextElement();
			
			if (twoPhaseCommitContext.getDecision() == Decision.NotDecided)
			{				
				return twoPhaseCommitContext;
			}
		}
		
		return null;
	}
	
	private void recover()
	{
		recoverContexts();
		
		TwoPhaseCommitContext context = getPendingTwoPhaseCommit();
		if (context == null)
		{
			// no pending 2pc transaction, nothing to do here
			return;
		}
		
		// Means this node is a participant
		if (context.getCoordinatorId() != m_node.addr)
		{
			Participant participant = context.getParticipant(m_node.addr);
			if (context.getDecision() == Decision.Commit && !participant.getFinished())
			{
				abortOrCommit(context, false);
			}
			else if (context.getDecision() == Decision.Abort && !participant.getFinished())
			{
				abortOrCommit(context, true);
			}
			else
			{
				Vote vote = context.getParticipant(m_node.addr).getVote();
				if (vote == Vote.No || vote == Vote.None)
				{
					context.getParticipant(m_node.addr).setVote(Vote.No);
					context.getParticipant(m_node.addr).setDecision(Decision.Abort);
					saveContext(context);
					
					abortOrCommit(context, true);
				}
				else
				{
					startTerminationProtocol(context.getId());
				}
			}
		}
		// its coordinator
		else
		{
			if (context.getDecision() == Decision.NotDecided || (context.getDecision() == Decision.Abort && !context.getFinished()))
			{
				abortTwoPhaseCommit(context.getId());
			}
			else {
				commitTwoPhaseCommit(context.getId());
			}
		}
	}
	
	private void recoverContexts()
	{
		String[] files = m_node.listFiles();
		
		for (int i = 0; i < files.length; i++) {
			if (files[i].startsWith("2pc-"))
			{
				try {
					PersistentStorageReader psr = m_node.getReader(files[i]);
					
					String line;
					StringBuffer buffer = new StringBuffer();
					while ((line = psr.readLine()) != null) {
						buffer.append(line);
					}
					
					TwoPhaseCommitContext context = TwoPhaseCommitContext.deserialize(buffer.toString());
					_twoPhaseCommitContexts.put(context.getId(), context);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
			
	private void saveContext(TwoPhaseCommitContext context) {		
		// Don't append to the log if in recovery mode
		if (!m_inRecovery) {
			try {
				PersistentStorageWriter psw = m_node.getWriter("2pc-" + context.getId(), false);
				psw.write(context.serialize());
				psw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	// Temporary shim to fit current implementation model
	public boolean onCommand(String command)
	{
		/*
		String[] parts = command.split("\\s+");
		
		if (parts.length >= 3)
		{
			int from = Integer.parseInt(parts[0]);
			String methodName = parts[1].toLowerCase();
			
			Vector<String> methodArgs = new Vector<String>();
			for (int i = 2; i < parts.length; i++)
					methodArgs.add(parts[i]);
			
			onMethodCalled(from, methodName, methodArgs);
			return true;
		}
		*/
		
		return false;
	}
	
	// Temporary shim to fit current implementation model
	private void callMethod(int address, String methodName, Vector<String> params)
	{
		I2pcParticipant stub;
		
		if (m_stubs.containsKey(address))
		{
			stub = m_stubs.get(address);
		}
		else
		{
			stub = m_node.connectTo2pcParticipant(address, this);
			m_stubs.put(address, stub);
		}

		String command = ((Integer) m_node.addr).toString(); 
		command += " " + methodName;
		for (String arg : params)
			command += " " + arg;
		
		try 
		{
			stub.backcompat(command);
		}
		catch (RPCException e)
		{
			// Never happens
		}
 	}
	

	/**
	 * RPC: I2pcCoordinator.notifyPrepared()
	 */
	@Override 
	public String notifyPrepared(String transactionId) throws RPCException
	{
		return null;
	}

	/**
	 * RPC: I2pcCoordinator.notifyAborted()
	 */
	@Override 
	public String notifyAborted(String transactionId) throws RPCException
	{
		return null;
	}

	/**
	 * RPC: I2pcCoordinator.queryDecision()
	 */
	@Override 
	public String queryDecision(String transactionId) throws RPCException
	{
		return null;
	}
	

	/**
	 * RPC: I2pcParticipant.startTransaction
	 */
	@Override 
	public String startTransaction(String transactionId) throws RPCException		
	{
		return null;
	}
	
	/**
	 * RPC: I2pcParticipant.requestPrepare
	 */
	@Override 
	public String requestPrepare(String transactionId) throws RPCException
	{
		return null;
	}
	
	/**
	 * RPC: I2pcParticipant.commitTransaction
	 */
	@Override 
	public String commitTransaction(String transactionId) throws RPCException
	{
		return null;
	}
	
	/**
	 * RPC: I2pcParticipant.abortTransaction
	 */
	@Override 
	public String abortTransaction(String transactionId) throws RPCException
	{
		return null;
	}
	
	/**
	 * RPC: I2pcParticipant.backcompat
	 */
	@Override 
	public String backcompat(String blob) throws RPCException
	{
		String[] parts = blob.split("\\s+");
		int from = Integer.parseInt(parts[0]);
		String methodName = parts[1];
		Vector<String> args = new Vector<String>();
		for (int i = 2; i < parts.length; i++)
			args.add(parts[i]);
		
		onMethodCalled(from, methodName, args);
		return null;
	}
	

	/**
	 * RPC: I2pcCoordinatorReply.reply_notifyPrepared
	 */
	public void reply_notifyPrepared(int replyId, int sender, int result, String reply)
	{
		
	}
	
	/**
	 * RPC: I2pcCoordinatorReply.reply_notifyAborted
	 */
	public void reply_notifyAborted(int replyId, int sender, int result, String reply)
	{
		
	}
	
	/**
	 * RPC: I2pcCoordinatorReply.reply_queryDecision
	 */
	public void reply_queryDecision(int replyId, int sender, int result, String reply)
	{
		
	}
	
	
	/**
	 * RPC: I2pcParticipantReply.reply_startTransaction
	 */
	@Override
	public void reply_startTransaction(int replyId, int sender, int result, String reply)
	{
		return;
	}

	/**
	 * RPC: I2pcParticipantReply.reply_requestPrepare
	 */
	@Override
	public void reply_requestPrepare(int replyId, int sender, int result, String reply)
	{
		return;
	}

	/**
	 * RPC: I2pcParticipantReply.reply_commitTransaction
	 */
	@Override
	public void reply_commitTransaction(int replyId, int sender, int result, String reply)
	{
		return;
	}

	/**
	 * RPC: I2pcParticipantReply.reply_abortTransaction
	 */
	@Override
	public void reply_abortTransaction(int replyId, int sender, int result, String reply)
	{
		return;
	}
	
	/**
	 * RPC: I2pcParticipantReply.reply_backcompat
	 */
	@Override
	public void reply_backcompat(int replyId, int sender, int result, String reply)
	{
		
	}
}




