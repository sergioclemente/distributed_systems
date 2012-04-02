package transaction;

import java.io.IOException;

public interface ILogFile {
	public void append(String str) throws IOException;
}
