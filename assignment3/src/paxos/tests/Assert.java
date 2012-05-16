package paxos.tests;

public class Assert {
	public static void isTrue(boolean value) {
		if (!value) {
			throw new RuntimeException("Excepted true value");
		}
	}
	
	public static void isFalse(boolean value) {
		if (value) {
			throw new RuntimeException("Excepted false value");
		}
	}
	
	public static void equals(int expected, int actual) {
		if (expected != actual) {
			throw new RuntimeException("Excepted equal values. Expected:" + expected + " Actual:" + actual);
		}
	}
	
	public static void isNull(Object object) {
		if (object != null) {
			throw new RuntimeException("Expected null object");
		}
	}
	
	public static void isNotNull(Object object) {
		if (object == null) {
			throw new RuntimeException("Expected not null object");
		}
	}	
	
	public static void equals(Object expected, Object actual) {
		boolean equal = false;
		
		if (expected == null && actual == null) {
			equal = true;
		} else {
			if (expected == null && actual != null) {
				equal = false;
			} else if (expected != null && actual == null) {
				equal = false;
			} else {
				equal = expected.equals(actual);
			}
		}
		
		if (!equal) {
			throw new RuntimeException("Excepted equal values");
		}
	}
}
