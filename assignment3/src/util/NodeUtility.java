package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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
    
    public static void recoverFromCrash(Node node, String filename) {
		try {
			String tempfilename = getTempFileName(filename);
			
			if (Utility.fileExists(node, tempfilename)) {
				PersistentStorageReader psr_bck = node.getReader(tempfilename);
				
				if (!psr_bck.ready()) {
					psr_bck.close();
					
					PersistentStorageWriter f = node.getWriter(tempfilename, false);
					f.delete();
				} else {
					String oldContents = readAll(psr_bck);
					PersistentStorageWriter psw = node.getWriter(filename, false);
					psw.write(oldContents);
					psw.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
    
	public static boolean saveStateInFile(Node node, Object obj, String file) {
		boolean success = false;
		try {
			String content = SerializationUtil.serialize(obj);
			
			return updateFileContents(node, file, content);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return success;
	}
	
	public static String getTempFileName(String filename){
		return filename + ".temp";
	}
	
	public static boolean updateFileContents(Node node, String filename, String newContent) throws IOException {
		try {
			// read old file
			String oldContent = readAllLines(node, filename);
			
			
			String tempfilename = getTempFileName(filename);
			// write old to backup
			if (oldContent != null) {
				PersistentStorageWriter psw_bck = node.getWriter(tempfilename, false);
				psw_bck.write(oldContent);
				psw_bck.close();
			}

			// update new
			PersistentStorageWriter psw = node.getWriter(filename, false);
			psw.write(newContent);
			psw.close();

			// delete temporary file
			PersistentStorageWriter f = node.getWriter(tempfilename, false);
			f.delete();
			
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	// some helpers for transaction
	public static String readAllLines(Node node, String filename)  {
		try {
			PersistentStorageReader psr = node.getReader(filename);
			return readAll(psr);
		} catch (FileNotFoundException e) {
			return null;
		}

	}
	
	private static String readAll(PersistentStorageReader psr) {
		StringBuffer sb = new StringBuffer();

		String line;
		try {
			while ((line=psr.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}
}
