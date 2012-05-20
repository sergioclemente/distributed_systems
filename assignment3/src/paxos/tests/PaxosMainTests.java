package paxos.tests;

import paxos.*;

public class PaxosMainTests {
	public static void main(String[] args) {
		valueTests();
		proposalNumberTests();
		createPrepareRequestTests();
		processPrepareResponseSunnyPathTests();
		processPrepareResponseRainyPathTests();
		processPrepareResponseResendPrepare();
	}
	
	private static void createPrepareRequestTests() {
		byte[] acceptorsHosts = {1,2,3,4};
		byte hostIdentifier = 0;
		Proposer proposer = new Proposer(hostIdentifier, acceptorsHosts);
		
		PrepareRequest prepareRequestSlot0 = proposer.createPrepareRequest(0);
		PrepareNumber prepareNumberSlot0 = prepareRequestSlot0.getNumber();
		
		Assert.equals(0, prepareRequestSlot0.getSlotNumber());
		Assert.equals(hostIdentifier, prepareNumberSlot0.getHostIdentifier());
		
		try {
			proposer.createPrepareRequest(0);
			Assert.isTrue(false);
		} catch (PaxosException ex) {
			
		}
		
		PrepareRequest prepareRequestSlot1 = proposer.createPrepareRequest(1);
		PrepareNumber prepareNumberSlot1 = prepareRequestSlot1.getNumber();

		Assert.equals(1, prepareRequestSlot1.getSlotNumber());
		Assert.equals(hostIdentifier, prepareNumberSlot1.getHostIdentifier());
		
		Assert.isTrue(prepareNumberSlot0.getSequenceNumber() < prepareNumberSlot1.getSequenceNumber());
		print("createPrepareRequestTests Passed!");
	}
	
	private static void processPrepareResponseSunnyPathTests() {
		byte[] acceptorsHosts = {1,2,3,4};
		byte hostIdentifier = 0;
		Proposer proposer = new Proposer(hostIdentifier, acceptorsHosts);
		
		PrepareRequest prepareRequest = proposer.createPrepareRequest(0);
		
		// Simulate the responses
		PrepareResponse response1 = new PrepareResponse((byte)1,prepareRequest,new PrepareNumber((byte)1,0));
		PrepareResponse response2 = new PrepareResponse((byte)2,prepareRequest,new PrepareNumber((byte)2,0));
		PrepareResponse response3 = new PrepareResponse((byte)3,prepareRequest,new PrepareNumber((byte)3,0));
		PrepareResponse response4 = new PrepareResponse((byte)4,prepareRequest,new PrepareNumber((byte)4,0));
		
		Assert.isFalse(proposer.processPrepareResponse(response1));
		Assert.isFalse(proposer.processPrepareResponse(response2));
		Assert.isTrue(proposer.processPrepareResponse(response3));
		Assert.isTrue(proposer.processPrepareResponse(response4));
		
		print("processPrepareResponseSunnyPathTests Passed!");
	}
	
	private static void processPrepareResponseRainyPathTests() {
		byte[] acceptorsHosts = {1,2,3,4};
		byte hostIdentifier = 0;
		Proposer proposer = new Proposer(hostIdentifier, acceptorsHosts);
		
		PrepareRequest prepareRequest = proposer.createPrepareRequest(0);
		
		// Simulate the responses
		PrepareResponse response1 = new PrepareResponse((byte)1,prepareRequest,new PrepareNumber((byte)2,2));
		PrepareResponse response2 = new PrepareResponse((byte)2,prepareRequest,new PrepareNumber((byte)2,2));
		PrepareResponse response3 = new PrepareResponse((byte)3,prepareRequest,new PrepareNumber((byte)2,2));
		PrepareResponse response4 = new PrepareResponse((byte)4,prepareRequest,new PrepareNumber((byte)2,2));
		
		Assert.isFalse(proposer.processPrepareResponse(response1));
		Assert.isFalse(proposer.processPrepareResponse(response2));
		Assert.isFalse(proposer.processPrepareResponse(response3));
		Assert.isFalse(proposer.processPrepareResponse(response4));
		
		print("processPrepareResponseRainyPathTests Passed!");
	}
	
	private static void processPrepareResponseResendPrepare() {
		byte[] acceptorsHosts = {1,2,3,4};
		byte hostIdentifier = 0;
		Proposer proposer = new Proposer(hostIdentifier, acceptorsHosts);
		
		PrepareRequest prepareRequest = proposer.createPrepareRequest(0);
		
		// Simulate the responses
		PrepareResponse response1 = new PrepareResponse((byte)1,prepareRequest,new PrepareNumber((byte)1,0));
		PrepareResponse response2 = new PrepareResponse((byte)2,prepareRequest,new PrepareNumber((byte)2,0));
		PrepareResponse response3 = new PrepareResponse((byte)3,prepareRequest,new PrepareNumber((byte)2,2));
		PrepareResponse response4 = new PrepareResponse((byte)4,prepareRequest,new PrepareNumber((byte)2,2));
		
		Assert.isFalse(proposer.processPrepareResponse(response1));
		Assert.isFalse(proposer.processPrepareResponse(response2));
		Assert.isFalse(proposer.processPrepareResponse(response3));
		Assert.isFalse(proposer.processPrepareResponse(response4));
		
		Assert.isTrue(proposer.shouldResendPrepareRequest(prepareRequest));
		PrepareRequest prepareRequestResend = proposer.createPrepareRequestResend(0);
		PrepareResponse responseResend1 = new PrepareResponse((byte)1,prepareRequestResend,new PrepareNumber((byte)1,0));
		PrepareResponse responseResend2 = new PrepareResponse((byte)2,prepareRequestResend,new PrepareNumber((byte)2,0));
		PrepareResponse responseResend3 = new PrepareResponse((byte)3,prepareRequestResend,new PrepareNumber((byte)3,2));
		PrepareResponse responseResend4 = new PrepareResponse((byte)4,prepareRequestResend,new PrepareNumber((byte)4,2));

		Assert.isFalse(proposer.processPrepareResponse(responseResend1));
		Assert.isFalse(proposer.processPrepareResponse(responseResend2));
		Assert.isTrue(proposer.processPrepareResponse(responseResend3));
		Assert.isTrue(proposer.processPrepareResponse(responseResend4));
		
		print("processPrepareResponseResendPrepare Passed!");
	}

	private static void proposalNumberTests() {
		PrepareNumber p = new PrepareNumber((byte) 1, 2);
		Assert.equals(1, p.getHostIdentifier());
		Assert.equals(2, p.getSequenceNumber());
		p.setSequenceNumber(123);
		Assert.equals(1, p.getHostIdentifier());
		Assert.equals(123, p.getSequenceNumber());		
		print("proposalNumberTests Passed!");
	}
	
	private static void valueTests() {
		AcceptedValues values = new AcceptedValues();
		Assert.isNull(values.getAt(0));
		values.setAt(0, new AcceptedValue(0, "abc", new PrepareNumber((byte)1,2)));
		Assert.equals("abc", values.getAt(0).getContent());
		print("valueTests Passed!");
	}
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	

}
