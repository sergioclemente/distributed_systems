package paxos;

public class PaxosException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public static final int PREVIOUS_SEQUENCE_NUMBER_HIGHER_THAN_CURRENT = 0;
	public static final int PREVIOUS_SLOT_NUMBER_DIFFERENT_THAN_CURRENT = 1;
	public static final int CONTENT_IS_NULL = 2;
	public static final int VALUE_IS_NULL = 3;
	public static final int PREVIOUS_VALUE_DIFFERENT_THAN_CURRENT = 4;
	public static final int CANNOT_CREATE_PREPARE_REQUEST_WITH_PENDING_RESPONSES = 5;
	public static final int ALREADY_RECEIVED_RESPONSE_FROM_THIS_ACCEPTOR = 6;
	public static final int INVALID_STATE_NOT_WAITING_FOR_REPARE_RESPONSE = 7;
	public static final int REQUEST_NUMBER_DIDNT_MATCH = 8;
	public static final int CANNOT_CREATE_PREPARE_REQUEST_WITHOUT_PENDING_RESPONSES = 9;
	public static final int CANNOT_CREATE_PREPARE_RESEND_WITHOUT_PENDING_RESPONSES = 10;
	public static final int CANNOT_CREATE_ACCEPT_REQUEST = 11;
	
	private int errorCode;
	public PaxosException(int errorCode) {
		this.errorCode = errorCode;
	}
	
	public int getErrorCode() {
		return this.errorCode;
	}
	
	public String getErrorMessage() {
		return getErrorMessage(this.errorCode);
	}
	
	public static String getErrorMessage(int errorCode) {
		switch (errorCode) {
			case PREVIOUS_SEQUENCE_NUMBER_HIGHER_THAN_CURRENT:
				return "Previous sequence number higher than current";
			case PREVIOUS_SLOT_NUMBER_DIFFERENT_THAN_CURRENT:
				return "Previous slot number different than current";
			case CONTENT_IS_NULL:
				return "Content is null";
			case VALUE_IS_NULL:
				return "Value is null";
			case PREVIOUS_VALUE_DIFFERENT_THAN_CURRENT:
				return "Previous value different than current";
			case CANNOT_CREATE_PREPARE_REQUEST_WITH_PENDING_RESPONSES:
				return "Cannot create prepare request with pending responses";
			case ALREADY_RECEIVED_RESPONSE_FROM_THIS_ACCEPTOR:
				return "Already received response from this acceptor";
			case INVALID_STATE_NOT_WAITING_FOR_REPARE_RESPONSE:
				return "Invalid state. Not watiting for prepare response";
			case REQUEST_NUMBER_DIDNT_MATCH:
				return "Request id didn't match";
			case CANNOT_CREATE_PREPARE_REQUEST_WITHOUT_PENDING_RESPONSES:
				return "Cannot create prepare request without pending responses";
			case CANNOT_CREATE_PREPARE_RESEND_WITHOUT_PENDING_RESPONSES:
				return "Cannot create resend without pending responses";
			case CANNOT_CREATE_ACCEPT_REQUEST:
				return "Cannot create accept request";
			default:
				return "unknown";
		}
	}
}
