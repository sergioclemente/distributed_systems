package node.twophasecommit;

public class Participant
{
	private int _participant;
	private Vote _vote;
	private Decision _decision;
	private int _replyId;
	
	
	/**
	 * Tells if the 2pc has decided and run, should only be set after either abort or commit runs successfully.
	 */
	private boolean _finished;
	
	public Participant(int participantId)
	{
		_participant = participantId;
		_vote = Vote.None;
		_decision = Decision.NotDecided;
		_replyId = 0;
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
	
	public boolean getFinished()
	{
		return _finished;
	}
	
	public void setFinished(boolean finished)
	{
		_finished = finished;
	}
	
	public int getReplyId()
	{
		return _replyId;
	}
	
	public void setReplyId(int id)
	{
		_replyId = id;
	}
}
