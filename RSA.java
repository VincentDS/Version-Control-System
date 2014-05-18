import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;
public class RSA {

	  /**
	   * Generate key which contains a pair of private and public key using 1024
	   * bytes. Store the set of keys in Prvate.key and Public.key files.
	   * 
	   * @throws NoSuchAlgorithmException
	   * @throws IOException
	   * @throws FileNotFoundException
	   */
	public static PublicKey generateKeys(String directory) {
		PublicKey publicKey = null;
		try {
			final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(1024);
			final KeyPair key = keyGen.generateKeyPair();

			File privateKeyFile = new File(directory + File.separator + ".my_private.key");

			// Create files to store public and private key
			/*	      if (privateKeyFile.getParentFile() != null) {
	        privateKeyFile.getParentFile().mkdirs();
	      } */
			privateKeyFile.createNewFile();

			/*	      if (publicKeyFile.getParentFile() != null) {
	        publicKeyFile.getParentFile().mkdirs();
	      } */

			// Saving the Private key in a file
			ObjectOutputStream privateKeyOS = new ObjectOutputStream(
					new FileOutputStream(privateKeyFile));
			privateKeyOS.writeObject(key.getPrivate());
			privateKeyOS.close();

			publicKey = key.getPublic();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return publicKey;
	}

	  public static void receivePublicKey(String directory, ObjectInputStream input) {
		try {
			PublicKey publicKey = (PublicKey) input.readObject();
			// Saving the received Public key in a file
			File publicKeyFile = new File(directory + File.separator + ".received_public.key");
			ObjectOutputStream publicKeyOS = new ObjectOutputStream(new FileOutputStream(publicKeyFile));
			publicKeyOS.writeObject(publicKey);
			publicKeyOS.close();		
		} catch (Exception e) {
			e.printStackTrace();
		}
	  }

	  /**
	   * Encrypt the plain text using public key.
	   * 
	   * @param text
	   *          : original plain text
	   * @param key
	   *          :The public key
	   * @return Encrypted text
	   * @throws java.lang.Exception
	   */
	  public static byte[] encrypt(byte[] toEncrypt, PublicKey key) {
	    byte[] encryptedBytes = null;
	    try {
	      // get an RSA cipher object and print the provider
	      final Cipher cipher = Cipher.getInstance("RSA");
	      // encrypt the plain text using the public key
	      cipher.init(Cipher.ENCRYPT_MODE, key);
	      encryptedBytes = cipher.doFinal(toEncrypt);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return encryptedBytes;
	  }

	  /**
	   * Decrypt text using private key.
	   * 
	   * @param text
	   *          :encrypted text
	   * @param key
	   *          :The private key
	   * @return plain text
	   * @throws java.lang.Exception
	   */
	  public static byte[] decrypt(byte[] toDecrypt, PrivateKey key) {
	    byte[] decryptedBytes = null;
	    try {
	      // get an RSA cipher object and print the provider
	      final Cipher cipher = Cipher.getInstance("RSA");

	      // decrypt the text using the private key
	      cipher.init(Cipher.DECRYPT_MODE, key);
	      decryptedBytes = cipher.doFinal(toDecrypt);

	    } catch (Exception ex) {
	      ex.printStackTrace();
	    }
	    return decryptedBytes;
	  }


}
