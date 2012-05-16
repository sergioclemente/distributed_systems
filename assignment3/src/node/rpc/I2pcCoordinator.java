package node.rpc;

public interface I2pcCoordinator 
{
	// Notifies the coordinator that this participant is ready to commit (vote = yes)
	String notifyPrepared(String transactionId) throws RPCException;
	
	// Notifies the coordinator that this participant is aborting the transaction
	// (equivalent to vote = no, but can be called at any point, e.g. during crash
	// recovery)
	String notifyAborted(String transactionId) throws RPCException;
	
	// Asks the coordinator to respond with the decision made for this transaction.
	// The decision should be "commit", "abort" or "undecided". This could be called
	// for instance when the participant crashed after voting yes but before receiving
	// a decision from the coordinator.
	String queryDecision(String transactionId) throws RPCException;
}
