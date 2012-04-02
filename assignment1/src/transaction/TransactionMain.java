package transaction;

import java.util.Vector;

public class TransactionMain {
	private class LogFile implements ILogFile {
		private Vector m_lines = new Vector();

		@Override
		public void append(String str) {
			this.m_lines.add(str);
		}
	}
	
	private class MutableInteger {
		private int m_value;
		
		public MutableInteger(int value) {
			this.m_value = value;
		}
		
		public void setValue(int value) {
			this.m_value = value;
		}
		
		public int getValue() {
			return this.m_value;
		}
	}
	
	private class UpdateIntegerVariable implements IAction {
		private MutableInteger m_integer;
		private int m_newValue;
		
		public UpdateIntegerVariable(MutableInteger integer, int newValue) {
			this.m_integer = integer;
			this.m_newValue = newValue;
		}
		
		@Override
		public void execute() throws Exception {
			this.m_integer.setValue(m_newValue);
		}

		@Override
		public void restore(String serialized) throws Exception {
			String[] parts = serialized.split(" ");
			if (parts.length != 2 || parts[0] != "SET") {
				throw new Exception("Invalid serialized command");
			} else {
				this.m_integer.setValue(Integer.parseInt(parts[1]));
			}
		}

		@Override
		public String serialize() {
			return "SET " + m_newValue;
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TransactionMain transMain = new TransactionMain();
		transMain.executeTests();
		
		
	}

	private void executeTests() {
		TransactionMain.LogFile logFile = new TransactionMain.LogFile();
		
		WriteAheadTransactionManager manager = new WriteAheadTransactionManager(logFile);
		
		ITransaction transaction = null;
		try	{
			transaction = manager.createTransaction();
			
			transaction.executeAction(new UpdateIntegerVariable(new MutableInteger(1), 2));
			
			transaction.commit();
		} catch (Exception ex) {
			if (transaction != null) {
				transaction.rollback();
			}
		}
	}

}
