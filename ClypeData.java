package data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @author Spencer
 *
 */
public abstract class ClypeData implements Serializable{
	private String userName;
	private int type;
	private Date date;
	
	/**
	 * 
	 * @param username
	 * @param type
	 */
	public ClypeData(String usern, int t) {
		userName = usern;
		type = t;
		date = new Date();
	}
	
	/**
	 *constructor uses default username Anon
	 * @param type
	 */
	public ClypeData(int t) {
		this("Anon", t);
	}
	
	/**
	 *constructor uses default username Anon and defualt type 3 
	 */
	public ClypeData() {
		this("Anon",3);
	}
	
	/**
	 * 
	 * @return type
	 */
	public int GetType() {
		return type;
	}
	
	/**
	 * 
	 * @return username
	 */
	public String getUserName() {
		return userName;
	}
	
	/**
	 * 
	 * @return date
	 */
	public Date getDate() {
		return date;
	}
	
	/**
	 * 
	 * @param inputStr
	 * @param key
	 * @return Vignere encrypted string
	 */
	protected String encrypt(String inputStr, String key) {
		char[] cipher = new char[inputStr.length()];
		char[] newkey = new char[inputStr.length()];
		
		int k = 0;
		for(int i = 0; i < inputStr.length(); i++) {
			if(k == key.length()) {
				k = 0;
			}
			newkey[i] = key.charAt(k);
			k++;
		}
		
		for(int i = 0; i < inputStr.length(); i++) {
			cipher[i] = (char)(((inputStr.charAt(i) + newkey[i]) % 26) + 'A');
		}
		
		return cipher.toString();
	}
	
	/**
	 * 
	 * @param inputStr
	 * @param key
	 * @return decrypted string
	 */
	protected String decrypt(String inputStr, String key) {
		char[] decrypted = new char[inputStr.length()];
		char[] newkey = new char[inputStr.length()];
		
		int k = 0;
		for(int i = 0; i < inputStr.length(); i++) {
			if(k == key.length()) {
				k = 0;
			}
			newkey[i] = key.charAt(k);
			k++;
		}
		
		for(int i = 0; i < inputStr.length(); i++) {
			decrypted[i] = (char)((((inputStr.charAt(i) - newkey[i]) + 26) % 26) + 'A');
		}
		
		return decrypted.toString();
	}
	
	
	/**
	 * 
	 * @return Data
	 */
	public abstract Object getData();
	
	/**
	 * 
	 * @param key
	 * @return Data
	 */
	public abstract Object getData(String key);
}
