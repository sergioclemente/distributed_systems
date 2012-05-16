package paxos.tests;

import paxos.*;

public class PaxosMainTests {
	public static void main(String[] args) {
		proposalNumberTests();
		valueTests();
	}
	
	private static void proposalNumberTests() {
		ProposalNumber p = new ProposalNumber((byte) 1, 2);
		Assert.equals(1, p.getHostIdentifier());
		Assert.equals(2, p.getSequencialNumber());
		p.setSequenceNumber(123);
		Assert.equals(1, p.getHostIdentifier());
		Assert.equals(123, p.getSequencialNumber());		
		print("proposalNumberTests Passed!");
	}
	
	private static void valueTests() {
		ProposedValues values = new ProposedValues();
		Assert.isNull(values.getValue(0));
		values.setValue(0, new ProposedValue(new ProposalNumber((byte)1,2), "abc"));
		Assert.equals("abc", values.getValue(0).getValue());
		print("valueTests Passed!");
	}
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	

}
