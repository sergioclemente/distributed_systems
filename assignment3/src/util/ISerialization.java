package util;

public interface ISerialization {
	void saveState(String name, Object value);
	Object restoreState(String name);
}
