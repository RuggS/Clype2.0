package data;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 
 * @author Spencer
 *
 */
public class FileClypeData extends ClypeData{
	
	String fileName;
	String fileContents = "";
	
	/**
	 * 
	 * @param username
	 * @param filename
	 * @param type
	 */
	public FileClypeData(String usern, String filen, int type) {
		super(usern, type);
		fileName = filen;
	}
	
	/**
	 * Constructor uses default username and type
	 */
	public FileClypeData() {
		super();
		
	}
	
	/**
	 * sets file name
	 * @param filename
	 */
	public void setFileName(String f) {
		fileName = f;
	}
	
	/**
	 * 
	 * @return filename
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	public void readFileContents() throws IOException{
		try {
			FileReader reader = new FileReader(fileName);//filename must be in format of similar to ./src/document.txt
			boolean doneReadingFile = false;
			
			while (!doneReadingFile) {

				int charint = reader.read();

				doneReadingFile = charint == -1;

				if(!doneReadingFile) {
					fileContents += (char)charint;
				}
			}
			reader.close();
				
		}catch(FileNotFoundException fnfe) {
			System.err.println("File not found");
		}catch(IOException ioe) {
			System.err.println("An IO exception has occurred");
		}
	}
	
	/**
	 * 
	 * @param key
	 * @throws IOException
	 */
	public void readFileContents(String key) throws IOException{
		try {
			FileReader reader = new FileReader(fileName);
			boolean doneReadingFile = false;
			
			while (!doneReadingFile) {
				
				int charint = reader.read();

				doneReadingFile = charint == -1;

				if(!doneReadingFile) {
					fileContents += (char)charint;
				}
			}
			fileContents = decrypt(fileContents, key);
				
		}catch(FileNotFoundException fnfe) {
			System.err.println("File not found");
		}catch(IOException ioe) {
			System.err.println("An IO exception has occurred");
		}
	}
	
	
	/**
	 * 
	 */
	public void writeFileContents() {
		FileWriter writer = null;
		try {
			writer = new FileWriter(fileName);

			
			writer.write(fileContents);

			writer.close();
		}catch (IOException ioe) {
			System.err.println("IO Exception");
		}catch (ArrayIndexOutOfBoundsException ariobe) {
			System.err.println("Array index out of bounds exception");
			try {
				writer.close();
			} catch (IOException e) {
			}
		}
	}
	
	/**
	 * 
	 * @param key
	 */
	public void writeFileContents(String key) {
		FileWriter writer = null;
		String decrypted = decrypt(fileContents, key);
		try {
			writer = new FileWriter(fileName);

			
			writer.write(decrypted);

			writer.close();
		}catch (IOException ioe) {
			System.err.println("IO Exception");
		}catch (ArrayIndexOutOfBoundsException ariobe) {
			System.err.println("Array index out of bounds exception");
			try {
				writer.close();
			} catch (IOException e) {
			}
		}
	}

	@Override
	public int hashCode() {
		return 0;
	}
	
	/**
	 * compares filename, filecontents, usename, and type
	 */
	@Override
	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}
		
		if(!(o instanceof FileClypeData)) {
			return false;
		}
		FileClypeData f = (FileClypeData) o;
		return this.fileName == f.fileName && this.GetType() == f.GetType() && 
				this.getUserName() == f.getUserName() && this.fileContents == f.fileContents;
	}
	
	@Override
	public String toString() {
		return "This is a file to user '" + super.getUserName() + "' with type " + super.GetType() + 
				", date of " + super.getDate() + ", and file name of '" + 
				this.fileName + " contaning " + this.fileContents + "'.";
	}
	
	@Override
	public Object getData() {
		
		return fileContents;
	}
	
	@Override
	public Object getData(String key) {
		
		return decrypt(fileContents, key);
	}

}
