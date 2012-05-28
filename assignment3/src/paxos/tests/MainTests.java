package paxos.tests;

import paxos.*;

public class MainTests {
	public static void main(String[] args) {
		valueTest();
		proposalNumberTest();
		createPrepareRequestTest();
		processPrepareResponseSunnyPathTest();
		processPrepareResponseRainyPathTest();
		processPrepareResponseResendPrepareTest();
		processPrepareAcceptSunnyTests();
		processPrepareAcceptRejectTest();
		processPrepareAcceptRePrepareTest();
		processPrepareRePrepareMultiTest();
		processLearnTest();
		processLearnFromMinorityTest();
		processConcurrentPrepareTest();
	}
	
	private static void processConcurrentPrepareTest() {
		TestDriver driver = new TestDriver(5,5,5);
		driver.prepare(0, 0);
		driver.accept(0, 0, "abc", new int[] {1,2}); // the first two will get the accept
		driver.prepare(1, 0);
		try {
			driver.accept(1, 0, "cde");
			Assert.isTrue(false);
		}catch (PaxosException ex) {
			Assert.equals(PaxosException.CANNOT_ACCEPT_WITH_DIFFERENT_VALUE, ex.getErrorCode());
		}
		print("processConcurrentPrepareTest Passed!");
	}
	
	private static void processLearnTest() {
		TestDriver driver = new TestDriver(5,5,5);
		driver.prepare(0, 0);
		Assert.isTrue(driver.accept(0, 0, "abc"));
		Assert.isNull(driver.getLearnedValue(0, 0)); // Didn't learn yet
		driver.learn(0, 0);
		driver.learn(1, 0);
		driver.learn(2, 0);
		LearnedValue learnedValue = driver.getLearnedValue(0, 0);
		Assert.isNotNull(learnedValue);
		Assert.equals("abc", learnedValue.getContent());
		print("processLearnTest Passed!");
	}
	
	private static void processLearnFromMinorityTest() {
		TestDriver driver = new TestDriver(5,5,5);
		driver.prepare(0, 0);
		Assert.isTrue(driver.accept(0, 0, "abc"));
		Assert.isNull(driver.getLearnedValue(0, 0)); // Didn't learn yet
		driver.learn(0, 0);
		driver.learn(1, 0);
		LearnedValue learnedValue = driver.getLearnedValue(0, 0);
		Assert.isNull(learnedValue);
		print("processLearnFromMinorityTest Passed!");
	}

	private static void processPrepareRePrepareMultiTest() {
		TestDriver driver = new TestDriver(5,5,5);
		driver.prepare(0, 0);
		driver.prepare(1, 0);
		Assert.isFalse(driver.accept(0, 0, "foo"));
		driver.prepare(0, 0);
		driver.prepare(0, 0);
		Assert.isTrue(driver.accept(0, 0, "foo"));
		print("processPrepareRePrepareMultiTest Passed!");
	}
	
	private static void processPrepareAcceptRePrepareTest() {
		TestDriver driver = new TestDriver(5,5,5);
		driver.prepare(0, 0);
		driver.prepare(1, 0);
		Assert.isFalse(driver.accept(0, 0, "foo"));
		driver.prepare(0, 0);
		Assert.isTrue(driver.accept(0, 0, "foo"));
		print("processPrepareAcceptRePrepareTest Passed!");
	}

	private static void processPrepareAcceptRejectTest() {
		TestDriver driver = new TestDriver(5,5,5);
		driver.prepare(0, 0);
		driver.prepare(1, 0);
		Assert.isFalse(driver.accept(0, 0, "foo"));
		print("processPrepareAcceptRejectTest Passed!");
	}
	
	private static void processPrepareAcceptSunnyTests() {
		TestDriver driver = new TestDriver(5,5,5);
		driver.prepare(0, 0);
		Assert.isTrue(driver.accept(0, 0, "foo"));
		print("processPrepareAcceptSunnyTests Passed!");
	}

	private static void createPrepareRequestTest() {
		byte hostIdentifier = 0;
		Proposer proposer = new Proposer(hostIdentifier, 4);
		
		PrepareRequest prepareRequestSlot0 = proposer.createPrepareRequest(0);
		PrepareNumber prepareNumberSlot0 = prepareRequestSlot0.getNumber();
		
		Assert.equals(0, prepareRequestSlot0.getSlotNumber());
		Assert.equals(hostIdentifier, prepareNumberSlot0.getHostIdentifier());
		
		try {
			proposer.createPrepareRequest(0);
		} catch (PaxosException ex) {
			Assert.isTrue(false);
		}
		
		PrepareRequest prepareRequestSlot1 = proposer.createPrepareRequest(1);
		PrepareNumber prepareNumberSlot1 = prepareRequestSlot1.getNumber();

		Assert.equals(1, prepareRequestSlot1.getSlotNumber());
		Assert.equals(hostIdentifier, prepareNumberSlot1.getHostIdentifier());
		
		Assert.isTrue(prepareNumberSlot0.getSequenceNumber() < prepareNumberSlot1.getSequenceNumber());
		print("createPrepareRequestTest Passed!");
	}
	
	private static void processPrepareResponseSunnyPathTest() {
		byte hostIdentifier = 0;
		Proposer proposer = new Proposer(hostIdentifier, 4);
		
		PrepareRequest prepareRequest = proposer.createPrepareRequest(0);
		
		// Simulate the responses
		PrepareResponse response1 = new PrepareResponse((byte)1,prepareRequest,new PrepareNumber((byte)1,0), null);
		PrepareResponse response2 = new PrepareResponse((byte)2,prepareRequest,new PrepareNumber((byte)2,0), null);
		PrepareResponse response3 = new PrepareResponse((byte)3,prepareRequest,new PrepareNumber((byte)3,0), null);
		PrepareResponse response4 = new PrepareResponse((byte)4,prepareRequest,new PrepareNumber((byte)4,0), null);
		
		Assert.isFalse(proposer.processPrepareResponse(response1));
		Assert.isFalse(proposer.processPrepareResponse(response2));
		Assert.isTrue(proposer.processPrepareResponse(response3));
		Assert.isTrue(proposer.processPrepareResponse(response4));
		
		print("processPrepareResponseSunnyPathTest Passed!");
	}
	
	private static void processPrepareResponseRainyPathTest() {
		byte hostIdentifier = 0;
		Proposer proposer = new Proposer(hostIdentifier, 4);
		
		PrepareRequest prepareRequest = proposer.createPrepareRequest(0);
		
		// Simulate the responses
		PrepareResponse response1 = new PrepareResponse((byte)1,prepareRequest,new PrepareNumber((byte)2,2), null);
		PrepareResponse response2 = new PrepareResponse((byte)2,prepareRequest,new PrepareNumber((byte)2,2), null);
		PrepareResponse response3 = new PrepareResponse((byte)3,prepareRequest,new PrepareNumber((byte)2,2), null);
		PrepareResponse response4 = new PrepareResponse((byte)4,prepareRequest,new PrepareNumber((byte)2,2), null);
		
		Assert.isFalse(proposer.processPrepareResponse(response1));
		Assert.isFalse(proposer.processPrepareResponse(response2));
		Assert.isFalse(proposer.processPrepareResponse(response3));
		Assert.isFalse(proposer.processPrepareResponse(response4));
		
		print("processPrepareResponseRainyPathTest Passed!");
	}
	
	private static void processPrepareResponseResendPrepareTest() {
		byte hostIdentifier = 0;
		Proposer proposer = new Proposer(hostIdentifier, 4);
		
		PrepareRequest prepareRequest = proposer.createPrepareRequest(0);
		
		// Simulate the responses
		PrepareResponse response1 = new PrepareResponse((byte)1,prepareRequest,new PrepareNumber((byte)1,0), null);
		PrepareResponse response2 = new PrepareResponse((byte)2,prepareRequest,new PrepareNumber((byte)2,0), null);
		PrepareResponse response3 = new PrepareResponse((byte)3,prepareRequest,new PrepareNumber((byte)2,2), null);
		PrepareResponse response4 = new PrepareResponse((byte)4,prepareRequest,new PrepareNumber((byte)2,2), null);
		
		Assert.isFalse(proposer.processPrepareResponse(response1));
		Assert.isFalse(proposer.processPrepareResponse(response2));
		Assert.isFalse(proposer.processPrepareResponse(response3));
		Assert.isFalse(proposer.processPrepareResponse(response4));
		
		Assert.isTrue(proposer.shouldResendPrepareRequest(0));
		PrepareRequest prepareRequestResend = proposer.createPrepareRequest(0);
		PrepareResponse responseResend1 = new PrepareResponse((byte)1,prepareRequestResend,new PrepareNumber((byte)1,0), null);
		PrepareResponse responseResend2 = new PrepareResponse((byte)2,prepareRequestResend,new PrepareNumber((byte)2,0), null);
		PrepareResponse responseResend3 = new PrepareResponse((byte)3,prepareRequestResend,new PrepareNumber((byte)3,2), null);
		PrepareResponse responseResend4 = new PrepareResponse((byte)4,prepareRequestResend,new PrepareNumber((byte)4,2), null);

		Assert.isFalse(proposer.processPrepareResponse(responseResend1));
		Assert.isFalse(proposer.processPrepareResponse(responseResend2));
		Assert.isTrue(proposer.processPrepareResponse(responseResend3));
		Assert.isTrue(proposer.processPrepareResponse(responseResend4));
		
		print("processPrepareResponseResendPrepareTest Passed!");
	}

	private static void proposalNumberTest() {
		PrepareNumber p = new PrepareNumber((byte) 1, 2);
		Assert.equals(1, p.getHostIdentifier());
		Assert.equals(2, p.getSequenceNumber());
		p.setSequenceNumber(123);
		Assert.equals(1, p.getHostIdentifier());
		Assert.equals(123, p.getSequenceNumber());		
		print("proposalNumberTest Passed!");
	}
	
	private static void valueTest() {
		LearnedValues values = new LearnedValues();
		Assert.isNull(values.getAt(0));
		values.setAt(0, new LearnedValue(0, "abc", new PrepareNumber((byte)1,2)));
		Assert.equals("abc", values.getAt(0).getContent());
		print("valueTest Passed!");
	}
	
	private static void print(String msg) {
		System.out.println(msg);
	}
}
