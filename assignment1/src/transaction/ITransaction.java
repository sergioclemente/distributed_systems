package transaction;

public interface ITransaction {
	public void commit();
	public void rollback();
	public void executeAction(IAction action);
}
