package node.twophasecommit;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.UUID;
import java.util.Vector;

import node.rpc.RPCNode;

public class TwoPhaseCommitNode extends RPCNode {
	//TODO-licavalc: Deal with participants that are already in a 2pc process. They should just vote No and abort.	
	
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
	public TwoPhaseCommitNode()
	{
		//TODO-licavalc: load state from log file
		_twoPhaseCommitContexts = new Hashtable<UUID, TwoPhaseCommitContext>();
	}
	
	@Override
	protected void onMethodCalled(int from, String methodName, Vector<String> params) {
		if (methodName.equals("vote-request"))
		{
			receiveVoteRequest(from, params);
		}
		else if (methodName.equals("receive-vote"))
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
		else
		{
			super.onMethodCalled(from, methodName, params);
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
		TwoPhaseCommitContext newContext = new TwoPhaseCommitContext(addr ,participants, command, params);
		_twoPhaseCommitContexts.put(newContext.getId(), newContext);
		
		Enumeration<TwoPhaseCommitContext> twoPhaseCommitContexts = _twoPhaseCommitContexts.elements();
		while(twoPhaseCommitContexts.hasMoreElements())
		{
			TwoPhaseCommitContext twoPhaseCommitContext = twoPhaseCommitContexts.nextElement();
			
			// That means there is a pending two phase commit in progress - abort this one in this case
			if (twoPhaseCommitContext != newContext && twoPhaseCommitContext.getDecision() == Decision.NotDecided)
			{
				//TODO-licavalc: is just the fact that the coordinator decided enough? Do we have to wait for it to notify participants? I don't think so, right?
				abortTwoPhaseCommit(newContext.getId());
				return;
			}
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
			error("abortTwoPhaseCommit: context not found.");
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
		// TODO Auto-generated method stub
	}

	private void commitTwoPhaseCommit(UUID twoPhaseCommitContextId) {
		TwoPhaseCommitContext context = _twoPhaseCommitContexts.get(twoPhaseCommitContextId);
		
		if (context == null)
		{
			error("commitTwoPhaseCommit: context not found.");
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
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		
		//TODO-licavalc: send the participant vote to the coordinator
		//TODO-licavalc: If the participant Votes YES, start a timer to receive the decision, and initiates termination algorithm on timeout.
		//TODO-licavalc: DON'T REALLY NEED THIS NOW: run the command at this point, but don't commit it. Needed to properly figure out the Vote.
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
		
		callMethod(targetSender, "receive-vote", params);
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
			error("receiveVote: wrong number of params.");
			return;
		}
		
		UUID twoPhaseCommitContextId = UUID.fromString(params.get(0));
		
		TwoPhaseCommitContext context = _twoPhaseCommitContexts.get(twoPhaseCommitContextId);
		
		if (context == null) 
		{
			error("receiveVote: context not found.");
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
				error("receiveVote: Unknown vote type");
				return;
			}
		}
		else
		{
			warn("receiveVote: already received vote for participant.");
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
				info("receiveVote: haven't decided yet.");
		}		
	}	
	
	//TODO-licavalc: Need to do the right thing with write/read from log. Right now, we append an updated version of the whole json version of the object in the log.
	//TODO-licavalc: How to store the state of the participant? Just store the participant JSON, use the TwoPhaseCommitContext for participants as well?	
	private void saveContext(TwoPhaseCommitContext context) {
		//TODO: updateFileContents() doesnt exist as part of RPCNode anymore. Commenting out for now.
		/*
		try {			
			String logMessage =	String.format("start-2pc %s", context.toJson());
			
			updateFileContents("TwoPhaseCommit.txt", logMessage, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}
}




