package util;

import java.io.File;
import java.io.IOException;

import node.facebook.FacebookShardState;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import edu.washington.cs.cse490h.lib.Node;
import edu.washington.cs.cse490h.lib.PersistentStorageReader;
import edu.washington.cs.cse490h.lib.PersistentStorageWriter;
import edu.washington.cs.cse490h.lib.Utility;

public class NodeUtility {
    /**
     * Returns the list of files (and directories) on a particular node.
     * 
     * @param n - the node from whom we are going to list the files.
     *  
     * @return - The list of files in the node.
     */
    public static String[] listFiles(Node n)
    {
    	File f = new File("storage/" + n.addr + "");
        if (!f.exists() || !f.isDirectory()) {
            return new String[0];
        }
        
        return f.list();
    }
    
    
	public static boolean saveStateInFile(Node node, Object obj, String file) {
		boolean success = false;
		try {
			String content = NodeUtility.serialize( obj);
			
			PersistentStorageWriter psw = node.getWriter(file, false);
			psw.write(content);
			psw.close();
			success = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return success;
	}
    
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
