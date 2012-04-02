package transaction;

public class WriteAheadTransaction implements ITransaction {

	private WriteAheadTransactionManager m_manager;
	private long m_transactionId;
	
	public WriteAheadTransaction(WriteAheadTransactionManager manager, long transactionId) {
		this.m_manager = manager;
		this.m_transactionId = transactionId;
	}
	
	@Override
	public void commit() {
		this.m_manager.notifyTransactionCompleted(this, true);
	}

	@Override
	public void rollback() {
		this.m_manager.notifyTransactionCompleted(this, false);
	}

	public long getTransactionId() {
		return m_transactionId;
	}

	@Override
	public void executeAction(IAction action) {
		this.m_manager.executeActionInTransaction(this,action);
	}
}
