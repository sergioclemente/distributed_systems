package util;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.washington.cs.cse490h.lib.Node;
import edu.washington.cs.cse490h.lib.PersistentStorageReader;
import edu.washington.cs.cse490h.lib.Utility;

public class SerializationUtil {
    public static String serialize(Object obj) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(obj);
    }
    
	public static <T> Object deserialize(String str, Class<T> c) {
		Gson g = new Gson();
		return g.fromJson(str, c);
	}
	
	public static <T> Object deserializeFromFile(Node node, String filename, Class<T> c) {
		try {
			if (Utility.fileExists(node, filename)) {
				PersistentStorageReader psr = node.getReader(filename);
				char[] buffer = new char[1024];
				StringBuffer sb = new StringBuffer();
				do {
					int len = psr.read(buffer, 0, 1024);
					if (len > 0) {
						sb.append(buffer, 0, len);
					} else {
						break;
					}
				} while (true);
				
				return deserialize(sb.toString(), c);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}