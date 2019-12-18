package data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javafx.scene.media.Media;

public class AudioClypeData extends ClypeData {

	private transient Media audioFile;
	//private String filePath = "";
	private File file;
	private transient FileInputStream fis;
	private byte[] barr;
	
	
	public AudioClypeData(String path, String usern, int t) {
		super(usern, t);
		file = new File(path);
		try {
			fis = new FileInputStream(file);
			barr = new byte[(int) file.length()];
			fis.read(barr);
			fis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
	}


	@Override
	public Object getData() {
		// TODO Auto-generated method stub
		return file;
	}


	@Override
	public Object getData(String key) {
		// TODO Auto-generated method stub
		return null;
	}
		
}
