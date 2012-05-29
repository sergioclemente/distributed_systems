package paxos;

import util.SerializationUtil;

public class AcceptResponse {
	private byte hostIdentifier;
	private PrepareRequest prepareRequest;
	private PrepareNumber acceptedProposalNumber;
	private boolean accepted;
	
	public AcceptResponse(byte hostIdentifier, PrepareRequest prepareRequest, PrepareNumber acceptedProposalNumber, boolean accepted) {
		this.hostIdentifier = hostIdentifier;
		this.prepareRequest = prepareRequest;
		this.acceptedProposalNumber = acceptedProposalNumber;
		this.accepted = accepted;
	}
	
	public byte getHostIdentifier() {
		return this.hostIdentifier;
	}
	
	public PrepareRequest getPrepareRequest() {
		return this.prepareRequest;
	}
	
	public PrepareNumber getAcceptedProposalNumber() {
		return acceptedProposalNumber;
	}
	
	public boolean getAccepted() {
		return this.accepted;
	}
	
	@Override
	public String toString() {
		return SerializationUtil.serialize(this);
	}
}
