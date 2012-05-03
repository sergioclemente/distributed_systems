package node.twophasecommit;

import java.util.UUID;
import java.util.Vector;

import com.google.gson.Gson;

public class TwoPhaseCommitContext
{	
	private UUID _id;
	
	private int _coordinator;
	
	private Vector<Participant> _participants;
	
	private String _command;
	
	private Vector<String> _params;
	
	private Decision _decision;
	
	public TwoPhaseCommitContext(int coordinator, Vector<String> participants, String command, Vector<String> params)
	{
		_id = UUID.randomUUID();
		_command = command;
		_params = params;
		_coordinator = coordinator;
		_decision = Decision.NotDecided;
		
		_participants = new Vector<Participant>(participants.size());		
		for (String participant : participants) {
			_participants.add(new Participant(participant));
		}		
	}
	
	public UUID getId() {
		return _id;
	}
	
	public int getCoordinator() {
		return _coordinator;
	}
	
	public Vector<Participant> getParticipants() {
		return _participants;
	}
	
	public Vector<Participant> GetAllParticipantsWhoVotedYes()
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
	
	public String getCommand() {
		return _command;
	}
	
	public Vector<String> getParams() {
		return _params;
	}
	
	public Decision getDecision() {
		return _decision;
	}
	
	public void setDecision(Decision _decision) {
		this._decision = _decision;
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
	
	public String toJson()
	{
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}

enum Vote {
	Yes,
	No,
	None
}

enum Decision {
	Commit,
	Abort,
	NotDecided
}
