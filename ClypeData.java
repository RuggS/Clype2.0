package data;
import java.io.Serializable;
import java.util.*;
/**
 * Superclass that represents data sent between the client and the server
 * 
 * @author Chris Undercoffer
 */
public abstract class ClypeData implements Serializable {
	
	private String  userName;
	private int type;
	private Date date;
	private static final int GET_USERS = 0;
	private static final int LOG_OUT = 1;
	private static final int SEND_FILE = 2;
	private static final int SEND_MESSAGE = 3;
	/**
	 * Constructor that initializes userName and type to an input. Initializes date to the time of instantiation
	 * @param userName 
	 * @param type
	 */
	public ClypeData(String userName, int type) {
		this.userName = userName;
		this.type = type;
		this.date = new Date();
	}
	/**
	 * Constructor that initializes type to a given input, username is set to "Anon"
	 * @param type
	 */
	public ClypeData(int type) {
		this("Anon", type);
	}
	/**
	 * Constructor that initializes username to "Anon" and type to 0
	 */
	public ClypeData() {
		this("Anon", 0);
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}
	/** 
	 * @return the message or file content if called from MessageClypeData or FileClypeData
	 */
	public abstract String getData();
	/**
	 * 
	 * @param key key to decrypt message
	 * @return the decrypted message
	 */
	public abstract String getData(String key);
	/**
	 * Method to encrypt a string using the Vigenere cipher
	 * @param inputStringToEncrypt message to be encrypted
	 * @param key key to encrypt message Vigenere cipher
	 * @return the encrypted message
	 */
	public String encrypt(String inputStringToEncrypt, String key) {
		
		int len = inputStringToEncrypt.length();
		int orig_len = key.length();
		int j = 0;
		String newKey = "";
		for(int i = 0; newKey.length() < len; ++i) {
			if(j == orig_len)
				j = 0;
			if(inputStringToEncrypt.charAt(i) == ' ') {
				newKey += ' ';
			}
			newKey += (key.charAt(j));
			++j;
		}

		String encrypted_message = "";
		String orig_str = inputStringToEncrypt;
		inputStringToEncrypt = inputStringToEncrypt.toUpperCase();
		for(int i = 0; i < inputStringToEncrypt.length(); ++i) {
			int x = ' ';
			if(inputStringToEncrypt.charAt(i) != ' ') {
				x = (inputStringToEncrypt.charAt(i) + newKey.charAt(i)) % 26;
			}
			if(orig_str.charAt(i) > 64 && orig_str.charAt(i) < 91) {
				x += 'A';
			}
			if(orig_str.charAt(i) > 96 && orig_str.charAt(i) < 123) {
				x += 'a';
			}			
			
			encrypted_message += (char)(x);
		}
		return encrypted_message;
	}
	/**
	 * Method to decrypt a string that has been encrypted using the Vigenere cipher
	 * @param inputStringToDecrypt message to be decrypted
	 * @param key key to decrypt message
	 * @return the decrypted message
	 */
	public String decrypt(String inputStringToDecrypt, String key) {
		
		int len = inputStringToDecrypt.length();
		int orig_len = key.length();
		int j = 0;
		String newKey = "";
		for(int i = 0; newKey.length() < len; ++i) {
			if(j == orig_len)
				j = 0;
			if(inputStringToDecrypt.charAt(i) == ' ') {
				newKey += ' ';
			}
			newKey += (key.charAt(j));
			++j;
		}
		
		String decrypted_message = "";
		String orig_str = inputStringToDecrypt;
		inputStringToDecrypt = inputStringToDecrypt.toUpperCase();
		for(int i = 0; i < inputStringToDecrypt.length() && i < newKey.length(); ++i) {
			int x = ' ';
			if(inputStringToDecrypt.charAt(i) != ' ') {
				x = (inputStringToDecrypt.charAt(i) - newKey.charAt(i)+26) % 26;
			}
			if(orig_str.charAt(i) > 64 && orig_str.charAt(i) < 91) {
				x += 'A';
			}
			if(orig_str.charAt(i) > 96 && orig_str.charAt(i) < 123) {
				x += 'a';
			}	
			decrypted_message += (char)(x);
		}
		return decrypted_message;
	}
}
