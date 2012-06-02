package paxos;

import java.util.LinkedHashMap;
import util.ISerialization;

public class Acceptor {
	private byte hostIdentifier;
	private ISerialization serialization;
	private LinkedHashMap<Integer, PrepareNumber> promisedNumbers;
	private LinkedHashMap<Integer, PrepareNumber> acceptedNumbers;
	private LinkedHashMap<Integer, PaxosValue> acceptedValues;

	public Acceptor(byte hostIdentifier, ISerialization serialization) {
		this.hostIdentifier = hostIdentifier;
		this.serialization = serialization;

		if (this.serialization != null) {
			this.promisedNumbers = (LinkedHashMap<Integer, PrepareNumber>) this.serialization.restoreState("promisedNumbers");
			if (this.promisedNumbers == null)
				this.promisedNumbers = new LinkedHashMap<Integer, PrepareNumber>();
			
			this.acceptedNumbers = (LinkedHashMap<Integer, PrepareNumber>) this.serialization.restoreState("acceptedNumbers");
			if (this.acceptedNumbers == null)
				this.acceptedNumbers = new LinkedHashMap<Integer, PrepareNumber>();
			
			this.acceptedValues = (LinkedHashMap<Integer, PaxosValue>) this.serialization.restoreState("acceptedValues");
			if (this.acceptedValues == null)
				this.acceptedValues = new LinkedHashMap<Integer, PaxosValue>();
			
		} else {
			this.promisedNumbers = new LinkedHashMap<Integer, PrepareNumber>();
			this.acceptedNumbers = new LinkedHashMap<Integer, PrepareNumber>();
			this.acceptedValues = new LinkedHashMap<Integer, PaxosValue>();
		}
	}

	/**
	 * processPrepareRequest()
	 */
	public PrepareResponse processPrepareRequest(PrepareRequest prepareRequest) {
		boolean promise;
		int slotNumber = prepareRequest.getSlotNumber();

		if (this.promisedNumbers.containsKey(slotNumber)) {
			PrepareNumber pn = this.promisedNumbers.get(slotNumber);
			if (pn.getValue() > prepareRequest.getNumber().getValue()) {
				// We've already promised to accept a higher numbered request,
				// so deny this one
				promise = false;
			} else {
				// This is the highest numbered request seen so far, remember it
				this.promisedNumbers.put(slotNumber, prepareRequest.getNumber());
				promise = true;
			}
		} else {
			// This is the first request, so promise to accept it
			this.promisedNumbers.put(slotNumber, prepareRequest.getNumber());
			promise = true;
		}

		PaxosValue currentAcceptedValue = null;
		PrepareNumber currentAcceptedProposal = null;
		if (this.acceptedValues.containsKey(slotNumber)) {
			currentAcceptedValue = this.acceptedValues.get(slotNumber);
			currentAcceptedProposal = this.acceptedNumbers.get(slotNumber);
		}

		PrepareResponse prepareResponse;
		prepareResponse = new PrepareResponse(this.hostIdentifier,
				prepareRequest, promise, currentAcceptedProposal,
				currentAcceptedValue);

		if (this.serialization != null) {
			this.serialization.saveState("prepareNumbers", this.promisedNumbers);
		}

		return prepareResponse;
	}

	/**
	 * processAccept()
	 */
	public AcceptResponse processAccept(AcceptRequest acceptRequest) {
		PrepareRequest prepareRequest = acceptRequest.getPrepareRequest();
		PrepareNumber requestNumber = prepareRequest.getNumber();
		int slotNumber = prepareRequest.getSlotNumber();

		if (!this.promisedNumbers.containsKey(slotNumber)) {
			throw new PaxosException(PaxosException.UNEXPECTED_SLOT_NUMBER);
		}

		boolean accepted = false;
		if (requestNumber.getValue() >= this.promisedNumbers.get(slotNumber).getValue()) {
			this.acceptedValues.put(slotNumber, acceptRequest.getValue());
			this.acceptedNumbers.put(slotNumber, requestNumber.clone());
			accepted = true;
			
			if (this.serialization != null) {
				this.serialization.saveState("acceptedValues", this.acceptedValues);
				this.serialization.saveState("acceptedNumbers", this.acceptedNumbers);
			}
		}

		return new AcceptResponse(this.hostIdentifier, prepareRequest,
				requestNumber.clone(), accepted, acceptRequest.getValue());
	}

	/**
	 * createLearnRequest()
	 */
	public LearnRequest createLearnRequest(int slotNumber) {
		if (!this.acceptedValues.containsKey(slotNumber)) {
			throw new PaxosException(PaxosException.VALUE_IS_NULL);
		}
		
		PaxosValue value = this.acceptedValues.get(slotNumber);
		PrepareNumber pn = this.acceptedNumbers.get(slotNumber);
		LearnedValue learnedValue = new LearnedValue(slotNumber, value, pn); 
		return new LearnRequest(slotNumber, this.hostIdentifier, learnedValue);
	}

}
