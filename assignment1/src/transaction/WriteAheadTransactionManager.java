package transaction;

public class WriteAheadTransactionManager implements ITransactionManager {

	private ILogFile m_logFile;
	private long m_lastTransactionId;
	
	public WriteAheadTransactionManager(ILogFile logFile) {
		this.m_logFile = logFile;
	}
	
	@Override
	public ITransaction createTransaction() {
		this.m_logFile.append(this.m_lastTransactionId + " START");
		
		WriteAheadTransaction tran = new WriteAheadTransaction(this, this.m_lastTransactionId);

		this.m_lastTransactionId++;
		
		return tran;
	}

	public void notifyTransactionCompleted(
			WriteAheadTransaction transaction, boolean committed) {
		if (committed) {
			this.m_logFile.append(transaction.getTransactionId() + " COMMIT");
		} else {
			this.m_logFile.append(transaction.getTransactionId() + " ROLLBACK");
		}
	}

	public void executeActionInTransaction(WriteAheadTransaction transaction, IAction action) {
		this.m_logFile.append(transaction.getTransactionId() + " " + action.serialize());
		
		try {
			action.execute();
		} catch (Exception ex) {
			transaction.rollback();
		}
	}
}
