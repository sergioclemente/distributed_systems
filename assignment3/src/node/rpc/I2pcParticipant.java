package node.rpc;

public interface I2pcParticipant 
{
	// Tells the participant to start a transaction with the given unique ID
	String startTransaction(String coordId, String transactionId) throws RPCException;		
	
	// Asks the participant to prepare for commit and reply with a vote.
	// The participant must invoke either I2pcCoordinator.notifyPrepared (vote = yes)
	// or I2pcCoordinator.notifyAborted (vote = no)
	String requestPrepare(String coordId, String transactionId) throws RPCException;
	
	// Tells the participant to commit the transaction (all participants voted yes)
	String commitTransaction(String coordId, String transactionId) throws RPCException;
	
	// Tells the participant to undo the changes and abort the transaction
	String abortTransaction(String coordId, String transactionId) throws RPCException;
}
