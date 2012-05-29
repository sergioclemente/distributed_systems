package paxos;

import util.SerializationUtil;

public class PrepareResponse {
	private byte hostIdentifier;
	private PrepareRequest prepareRequest;
	private boolean promised;
	private PrepareNumber highestAcceptedProposalSoFar;
	private PaxosValue content;
	
	public PrepareResponse(byte hostIdentifier, PrepareRequest prepareRequest, boolean promised, PrepareNumber highestAcceptedProposalSoFar, PaxosValue content) {
		this.hostIdentifier = hostIdentifier;
		this.prepareRequest = prepareRequest;
		this.highestAcceptedProposalSoFar = highestAcceptedProposalSoFar;
		this.content = content;
		this.promised = promised;
	}

	public byte getHostIdentifier() {
		return hostIdentifier;
	}
	
	public PrepareRequest getPrepareRequest() {
		return this.prepareRequest;
	}
	
	public PrepareNumber getHighestAcceptedProposalSoFar() {
		return highestAcceptedProposalSoFar;
	}
	
	public PaxosValue getContent() {
		return this.content;
	}
	
	/**
	 * Returns true if the acceptor promised to accept the proposal.
	 */
	public boolean promised(PrepareNumber currentProposalNumber) {
		return promised && currentProposalNumber.compareTo(highestAcceptedProposalSoFar) >= 0;
	}
	
	
	@Override
	public String toString() {
		return SerializationUtil.serialize(this);
	}
}
