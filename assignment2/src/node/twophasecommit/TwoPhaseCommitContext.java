package node.twophasecommit;

import java.util.UUID;
import java.util.Vector;

import com.google.gson.Gson;

public class TwoPhaseCommitContext
{	
	private UUID _id;
	
	private int _coordinator;
	
	private Vector<Participant> _participants;
	
	private Decision _decision;
	
	/**
	 * Tells if the 2pc has decided and run, should only be set after all the abort or commit messages have been sent.
	 */
	private boolean _finished;
	
	public TwoPhaseCommitContext(UUID id, int coordinator, Vector<String> participants)
	{
		_id = id;
		_coordinator = coordinator;
		_decision = Decision.NotDecided;
		
		_participants = new Vector<Participant>(participants.size());		
		for (String participant : participants) {
			_participants.add(new Participant(participant));
		}	
	}
	
	public TwoPhaseCommitContext(int coordinator, Vector<String> participants)
	{
		this(UUID.randomUUID(), coordinator, participants);
	}
	
	public UUID getId() {
		return _id;
	}
	
	public int getCoordinatorId() {
		return _coordinator;
	}
	
	public Vector<Participant> getParticipants() {
		return _participants;
	}
	
	public Vector<Participant> getAllParticipantsWhoVotedYes()
	{
		Vector<Participant> participantsWhoVotedYes = new Vector<Participant>();
		
		for (Participant participant : _participants) {
			if (participant.getVote() == Vote.Yes)
			{
				participantsWhoVotedYes.add(participant);
			}
		}
		
		return participantsWhoVotedYes;
	}
	
	public Participant getParticipant(int participantId)
	{
		for (Participant participant : _participants) {
			if (participant.getId() == participantId)
			{
				return participant;
			}
		}
		
		return null;
	}	
	
	public Decision getDecision() {
		return _decision;
	}
	
	public void setDecision(Decision _decision) {
		this._decision = _decision;
	}
	
	public boolean getFinished()
	{
		return _finished;
	}
	
	public void setFinished(boolean finished)
	{
		_finished = finished;
	}
	
	public Decision getDecisionBasedOnVotes()
	{
		Decision decision = Decision.Commit;
		
		for (Participant participant : _participants) {
			if (participant.getVote() != Vote.No)
			{
				decision = Decision.Abort;
				break;
			}
			else if (participant.getVote() == Vote.None)
			{
				decision = Decision.NotDecided;
			}
		}

		return decision;
	}
	
	public String serialize()
	{
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
	public static TwoPhaseCommitContext deserialize(String json)
	{
		Gson gson = new Gson();
		return gson.fromJson(json, TwoPhaseCommitContext.class);
	}

	public int getTimeout() {
		// TODO-licavalc: calculate the right timeout based on the number of participants, unless we implement multicast or non-blocing rpc calls
		return 0;
	}
}

