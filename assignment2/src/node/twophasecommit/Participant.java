package node.twophasecommit;

public class Participant
{
	private int _participant;
	
	private Vote _vote;
	
	private Decision _decision;
	
	public Participant(String participant)
	{
		_participant = Integer.parseInt(participant);
		_vote = Vote.None;
		_decision = Decision.NotDecided;
	}
	
	public int getId() {
		return _participant;
	}
	
	public Vote getVote() {
		return _vote;
	}
	
	public void setVote(Vote _vote) {
		this._vote = _vote;
	}
	
	public Decision getDecision() {
		return _decision;
	}
	
	public void setDecision(Decision _decision) {
		this._decision = _decision;
	}
}
