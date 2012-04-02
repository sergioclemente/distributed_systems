package transaction;

public interface IAction {
	public void execute() throws Exception;
	public void restore(String serialized) throws Exception;
	public String serialize();
}
