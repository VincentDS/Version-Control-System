import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;


/*
 *  Files ---> Algorithm (SHA-256) ---> Message Digest
 *  
 */
public class SHA1 {
	MessageDigest md;
	byte[] data;
	
	public SHA1(byte[] data) {
		this.data = data;
	    try {
			md = MessageDigest.getInstance("SHA-1"); //creates a new (empty) message-digest object 
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	

	public String getSHA1() {
		byte[] hash = md.digest(data);
	    Formatter formatter = new Formatter();
	    for (byte b : hash) {
	        formatter.format("%02x", b);
	    }
	    String res = formatter.toString();
	    formatter.close();
	    return res;
	}
}
