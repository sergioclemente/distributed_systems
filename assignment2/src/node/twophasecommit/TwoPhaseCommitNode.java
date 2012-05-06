package node.twophasecommit;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.UUID;
import java.util.Vector;

import node.rpc.RPCNode;
import node.rpc.RPCException;
import node.rpc.I2pcCoordinator;
import node.rpc.I2pcCoordinatorReply;
import node.rpc.I2pcParticipant;
import node.rpc.I2pcParticipantReply;


// TODO: this is not a node anymore, rename to something else. ThoPhaseCommitCoordinator?
public class TwoPhaseCommitNode implements I2pcCoordinator, I2pcParticipant, I2pcCoordinatorReply, I2pcParticipantReply {
	//TODO-licavalc: Deal with participants that are already in a 2pc process. They should just vote No and abort.	
	
	private RPCNode m_node;
	private Hashtable<Integer, I2pcParticipant> m_stubs = new Hashtable<Integer, I2pcParticipant>();
	
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
		
		//TODO-licavalc: load state from log file
		_twoPhaseCommitContexts = new Hashtable<UUID, TwoPhaseCommitContext>();
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
	}

	
	/**
	 * Starts a new two phase commit process. It tries to execute the passed command on the passed list of 
	 * participants.
	 * 
	 * @param participants - The list of participants that will participate in the two phase commit.
	 * @param command - The command to be executed.
	 * @param params - Any params to the command above.
	 */
	public void startTwoPhaseCommit(Vector<String> participants, String command, Vector<String> params)
	{
		TwoPhaseCommitContext newContext = new TwoPhaseCommitContext(m_node.addr, participants, command, params);
		_twoPhaseCommitContexts.put(newContext.getId(), newContext);
		
		if (anyTwoPhaseCommitPending(newContext))
		{
			//TODO-licavalc: is just the fact that the coordinator decided enough? Do we have to wait for it to notify participants? I don't think so, right?
			abortTwoPhaseCommit(newContext.getId());
			return;
		}
		
		//TODO-licavalc: Need a timeout for this.
		for (String participant : participants) {
			beginVoteRequest(Integer.parseInt(participant), newContext.getId(), participants, command, params);
		}
				
		saveContext(newContext);
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
		
		Vector<Participant> participantsWhoVotedYes = context.GetAllParticipantsWhoVotedYes();	
		for (Participant participant : participantsWhoVotedYes) {
			beginSendAbort(participant.getId(), context.getId());
		}
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
			error("receiveAbort: wrong number of params.");
			return;
		}
		
		UUID twoPhaseCommitContextId = UUID.fromString(params.get(0));
		
		TwoPhaseCommitContext context = _twoPhaseCommitContexts.get(twoPhaseCommitContextId);
		
		if (context == null) 
		{
			error("receiveAbort: context not found.");
			return;
		}
		
		context.setDecision(Decision.Abort);
		saveContext(context);
		
		// No need to do anything else, as we didn't really write anything in the write ahead log. We may need to 
		// change this in the future.
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
			error("receiveCommit: wrong number of params.");
			return;
		}
		
		UUID twoPhaseCommitContextId = UUID.fromString(params.get(0));
		
		TwoPhaseCommitContext context = _twoPhaseCommitContexts.get(twoPhaseCommitContextId);
		
		if (context == null) 
		{
			error("receiveCommit: context not found.");
			return;
		}
		
		context.setDecision(Decision.Commit);
		saveContext(context);
		
		//TODO-luciano: Luciano, you need to call this using your new call mechanism I guess.
		//TODO-licavalc: we need to call this too when recovering
		onMethodCalled(from, context.getCommand(), context.getParams());
	}

	public void beginVoteRequest(int targetSender, UUID twoPhaseCommitId, Vector<String> participants, String command, 
								 Vector<String> commandParams)
	{
		Vector<String> params = new Vector<String>();		
		
		params.add(twoPhaseCommitId.toString());
		
		params.add(participants.size() + "");
		params.addAll(participants);
		
		params.add(command);
		params.addAll(commandParams);
		
		//TODO-licavalc: need to put each of these calls in a separate queue per participant or make this a broadcast
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
		UUID twoPhaseCommitContextId = UUID.fromString(params.get(0));
		
		int participantsSize = Integer.valueOf(params.get(1));
		Vector<String> participants = new Vector<String>(params.subList(2, participantsSize + 2));		
		
		String command = params.get(participantsSize + 2);
		Vector<String> commandParams = new Vector<String>();
		if (params.size() > participantsSize + 3)
		{
			commandParams.addAll(params.subList(participantsSize + 2, params.size()));
		}
		
		TwoPhaseCommitContext newContext = 
				new TwoPhaseCommitContext(twoPhaseCommitContextId, from ,participants, command, commandParams);
		_twoPhaseCommitContexts.put(newContext.getId(), newContext);
		
		if (anyTwoPhaseCommitPending(newContext))
		{
			newContext.setDecision(Decision.Abort);
			saveContext(newContext);
			
			beginSendVote(from, twoPhaseCommitContextId, Vote.No);
			return;
		}
		
		//TODO-licavalc: DON'T REALLY NEED THIS NOW: run the command at this point, but don't commit it. Needed to properly figure out the Vote.
		
		newContext.getParticipant(addr).setVote(Vote.Yes);
		saveContext(newContext);
		
		beginSendVote(from, twoPhaseCommitContextId, Vote.Yes);
		
		//TODO-licavalc: If the participant Votes YES, start a timer to receive the decision, and initiates termination algorithm on timeout.		
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
	
	//TODO-licavalc: Need to do the right thing with write/read from log. Right now, we append an updated version of the whole json version of the object in the log.
	//TODO-licavalc: How to store the state of the participant? Just store the participant JSON, use the TwoPhaseCommitContext for participants as well?	
	private void saveContext(TwoPhaseCommitContext context) {
		//TODO-luciano: updateFileContents() doesn't exist as part of RPCNode anymore. Commenting out for now.
		/*
		try {			
			String logMessage =	String.format("start-2pc %s", context.toJson());
			
			updateFileContents("TwoPhaseCommit.txt", logMessage, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
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




