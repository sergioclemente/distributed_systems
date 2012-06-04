package paxos;

import java.util.LinkedHashMap;
import java.util.Map;

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

		this.promisedNumbers = new LinkedHashMap<Integer, PrepareNumber>();
		this.acceptedNumbers = new LinkedHashMap<Integer, PrepareNumber>();
		this.acceptedValues = new LinkedHashMap<Integer, PaxosValue>();

		if (this.serialization != null) {

			LinkedHashMap<String, LinkedHashMap<String, Object>> promisedNumbers;
			promisedNumbers = (LinkedHashMap<String, LinkedHashMap<String, Object>>) this.serialization.restoreState("promisedNumbers");
			if (promisedNumbers != null) {
				for (Map.Entry<String, LinkedHashMap<String, Object>> entry : promisedNumbers.entrySet()) {
					LinkedHashMap<String, Object> fields = entry.getValue();
					Integer slotNumber = Integer.valueOf(entry.getKey()); 
					long value = ((Double) fields.get("value")).longValue();
					this.promisedNumbers.put(slotNumber, new PrepareNumber(value));
				}
			}
					
			LinkedHashMap<String, LinkedHashMap<String, Object>> acceptedNumbers;
			acceptedNumbers = (LinkedHashMap<String, LinkedHashMap<String, Object>>) this.serialization.restoreState("acceptedNumbers");
			if (acceptedNumbers != null) {
				for (Map.Entry<String, LinkedHashMap<String, Object>> entry : acceptedNumbers.entrySet()) {
					LinkedHashMap<String, Object> fields = entry.getValue();
					Integer slotNumber = Integer.valueOf(entry.getKey()); 
					long value = ((Double) fields.get("value")).longValue();
					this.acceptedNumbers.put(slotNumber, new PrepareNumber(value));
				}
			}
					
			LinkedHashMap<String, LinkedHashMap<String, Object>> acceptedValues;
			acceptedValues = (LinkedHashMap<String, LinkedHashMap<String, Object>>) this.serialization.restoreState("acceptedValues");
			if (acceptedValues != null) {
				for (Map.Entry<String, LinkedHashMap<String, Object>> entry : acceptedValues.entrySet()) {
					LinkedHashMap<String, Object> fields = entry.getValue();
					Integer slotNumber = Integer.valueOf(entry.getKey()); 
					byte proposer = ((Double) fields.get("proposer")).byteValue();
					String command = (String) fields.get("command");
					PaxosValue v = new PaxosValue(proposer, command);
					this.acceptedValues.put(slotNumber, v);
				}
			}
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
			this.serialization.saveState("promisedNumbers", this.promisedNumbers);
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
