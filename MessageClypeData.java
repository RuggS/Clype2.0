package data;

/**
 * 
 * @author Spencer
 *
 */
public class MessageClypeData extends ClypeData{

	String message;
	
	/**
	 * 
	 * @param usernname
	 * @param message
	 * @param type
	 */	
	public MessageClypeData(String usern, String m, int t) {
		super(usern, t);
		message = m;	
	}
	
	/**
	 * 
	 * @param usern
	 * @param m
	 * @param key
	 * @param t
	 */
	public MessageClypeData(String usern, String m, String key, int t) {
		super(usern, t);
		
		message = encrypt(m, key);	
	}
	
	/**
	 * uses default username of Anon and default type 3
	 */
	public MessageClypeData() {
		super();
		message = "";
	}
	
	@Override
	public int hashCode() {
		return 0;
	}
	
	/**
	 * compares message, type, and username
	 */
	@Override
	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}
		
		if(!(o instanceof MessageClypeData)) {
			return false;
		}
		MessageClypeData m = (MessageClypeData) o;
		return this.message == m.message && this.getType() == m.getType() && this.getUserName() == m.getUserName();
	}
	
	@Override
	public String toString() {
		return "This is a message to user '" + super.getUserName() + "' with type " + super.getType() + 
				", date of " + super.getDate() + ", and message of '" + this.message + "'.";
	}
	
	/**
	 * @return message
	 */
	@Override
	public Object getData() {
		
		return message;
	}
	
	@Override
	public Object getData(String key) {
		
		return decrypt(message, key);
	}

}
