package util;

import java.io.File;

import edu.washington.cs.cse490h.lib.Node;

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
}
