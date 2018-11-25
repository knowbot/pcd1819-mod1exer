package merkleClient;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Helper class used to compute the hash of a 'text'-based transaction.
 * Nothing to be done here!
 */
public final class HashUtil {
	
	/**
	 * Method used to compute the MD5 hash of a given string which represents
	 * the transaction information.
	 * 
	 * @param message String: data representing the transaction 
	 * 
	 * @return String: hex representation of the transation hash.
	 */
	public static String md5Java(String message) { 
		String digest = null; 
		try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				
				byte[] hash = md.digest(message.getBytes("UTF-8")); //converting byte array to Hexadecimal String 
				StringBuilder sb = new StringBuilder(2*hash.length); 
				for(byte b : hash) {
					sb.append(String.format("%02x", b&0xff)); 
				} 
				digest = sb.toString(); 
			} catch (UnsupportedEncodingException ex) { 
			} catch (NoSuchAlgorithmException e) {} 

		return digest; 
	}

}
