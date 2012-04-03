package storage;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.lang.NullArgumentException;

public class StorageSystem {
	public void createFile (String fileName) throws StorageException, IOException
	{
		if (fileName == null)
		{
			throw new NullArgumentException("fileName");
		}
		
		File newFile = new File(fileName);
		
		if (!newFile.createNewFile())
		{
			throw new StorageException(StorageException.FILE_ALREADY_EXISTS);
		}
	}
	
	public String getFile(String fileName) throws StorageException, IOException
	{
		try
		{
			FileReader fileReader = new FileReader(fileName);			
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			
			StringBuffer contents = new StringBuffer();
			
			String line = bufferedReader.readLine();  
			while (line != null)
			{
				contents.append(line);
				
				line = bufferedReader.readLine();
			}
			
			return contents.toString();
		}
		catch (FileNotFoundException fnfe)
		{
			throw new StorageException(StorageException.FILE_DOES_NOT_EXISTS);
		}
	}
	
	//TODO-livar: verify that the file size doesn't exceed the lenght of the message minus the headers
	public void putFile(String fileName, String contents) throws StorageException, IOException
	{
		try {
			DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(fileName, false));
			
			outputStream.write(contents.getBytes());
			
			outputStream.close();
		} catch (FileNotFoundException e) {
			throw new StorageException(StorageException.FILE_DOES_NOT_EXISTS);
		}
	}
	
	//TODO-livar: verify that the file size doesn't exceed the lenght of the message minus the headers
	public void appendToFile(String fileName, String contents) throws StorageException, IOException
	{
		try {
			DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(fileName, true));
			
			outputStream.write(contents.getBytes());
			
			outputStream.close();
		} catch (FileNotFoundException e) {
			throw new StorageException(StorageException.FILE_DOES_NOT_EXISTS);
		}
	}
	
	public void deleteFile(String fileName) throws StorageException
	{
		File file = new File(fileName);
		
		if (!file.exists())
		{
			throw new StorageException(StorageException.FILE_DOES_NOT_EXISTS);
		}
		
		file.delete();
	}
}
