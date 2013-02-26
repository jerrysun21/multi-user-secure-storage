package jerrysun21.nujnah.multi;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MultiHelper {
	
	// Encrypt string using SHA1
    public static String toSHA1 (String input) {
    	MessageDigest md = null;
    	try {
			md = MessageDigest.getInstance("SHA-1");
			md.update(input.getBytes("utf8"));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return byteArrayToHexString(md.digest());
    }
    
    private static String byteArrayToHexString(byte[] array) {
    	String result = "";
		for (int i=0; i < array.length; i++) {
			result +=
					Integer.toString( ( array[i] & 0xff ) + 0x100, 16).substring( 1 );
		}
		return result;
    }
}
