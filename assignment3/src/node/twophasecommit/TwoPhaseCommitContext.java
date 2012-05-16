package node.twophasecommit;

import java.util.UUID;
import java.util.Vector;
import settings.MessagingSettings;
import util.NodeUtility;

import com.google.gson.Gson;
import node.facebook.FacebookException;

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
	private boolean	_inProgress;
	
	public TwoPhaseCommitContext(UUID id, int coordinator)
	{
		_id = id;
		_coordinator = coordinator;
		_decision = Decision.NotDecided;
		_participants = new Vector<Participant>();
		_inProgress = false;
		_finished = false;
	}
	
	public TwoPhaseCommitContext(int coordinator)
	{
		this(UUID.randomUUID(), coordinator);
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
	
	public void addParticipant(int participantId) throws FacebookException {
		if (_inProgress) {
			// Cannot add more participants after 2PC protocol started  
			throw new FacebookException(FacebookException.COMMIT_IN_PROGRESS);
		}
		
		for (Participant p : _participants) {
			if (p.getId() == participantId) {
				throw new FacebookException(FacebookException.PARTICIPANT_ALREADY_INCLUDED);
			}
		}
		
		_participants.add(new Participant(participantId));
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
	
	public boolean getInProgress() {
		return _inProgress;
	}
	
	public void setInProgress(boolean value) {
		_inProgress = value;
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
			if (participant.getVote() == Vote.No)
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
		return NodeUtility.serialize(this);
	}
	
	public static TwoPhaseCommitContext deserialize(String json)
	{
		Gson gson = new Gson();
		return gson.fromJson(json, TwoPhaseCommitContext.class);
	}

	public int getTimeout() {
		return 2 * MessagingSettings.MaxTimeout;
	}
}

