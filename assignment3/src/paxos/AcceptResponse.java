package paxos;

import util.SerializationUtil;

public class AcceptResponse {
	private byte hostIdentifier;
	private PrepareRequest prepareRequest;
	private PrepareNumber acceptedProposalNumber;
	private boolean accepted;
	private PaxosValue value;
	
	public AcceptResponse(byte hostIdentifier, PrepareRequest prepareRequest, PrepareNumber acceptedProposalNumber, boolean accepted, PaxosValue value) {
		this.hostIdentifier = hostIdentifier;
		this.prepareRequest = prepareRequest;
		this.acceptedProposalNumber = acceptedProposalNumber;
		this.accepted = accepted;
		this.value = value;
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
	
	public PaxosValue getValue() {
		return this.value;
	}
	
	@Override
	public String toString() {
		return SerializationUtil.serialize(this);
	}
}
