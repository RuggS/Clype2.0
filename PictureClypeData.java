package data;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import javafx.scene.image.Image;

public class PictureClypeData extends ClypeData {

	private String imagePath = "";
	private transient  BufferedImage img;
	//private File imgFile;
	private byte[] barr;
	public PictureClypeData(String imgPath, String usern, int t) {
		super(usern, t);
		this.imagePath = imgPath;
		try {
			this.img = ImageIO.read(new File(imagePath));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(img, "png", baos);
			baos.flush();
			barr = baos.toByteArray();
			baos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Object getData() {
		InputStream in = new ByteArrayInputStream(this.barr);
		/*try {
			img = ImageIO.read(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		Image image = new Image(in);
		return image;
	}

	@Override
	public Object getData(String key) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
