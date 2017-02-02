package t01;

import java.security.KeyStore;
import java.security.PrivateKey;

public class KeyStoreTest01 {

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) throws Exception {

//	    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
	    KeyStore ks = KeyStore.getInstance("JKS");

	    // get user password and file input stream
	    char[] password = new char[]{123};

	    java.io.FileInputStream fis = null;
	    try {
	        fis = new java.io.FileInputStream("keyStoreName");
	        ks.load(fis, password);
	    } finally {
	        if (fis != null) {
	            fis.close();
	        }
	    }

		// get my private key
		KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) ks.getEntry("privateKeyAlias", new KeyStore.PasswordProtection(password));
		PrivateKey myPrivateKey = pkEntry.getPrivateKey();


		// save my secret key
		javax.crypto.SecretKey mySecretKey = null;
		KeyStore.SecretKeyEntry skEntry = new KeyStore.SecretKeyEntry(mySecretKey);
		ks.setEntry("secretKeyAlias", skEntry, new KeyStore.PasswordProtection(password));

		// store away the keystore
		java.io.FileOutputStream fos = null;
		try {
			fos = new java.io.FileOutputStream("newKeyStoreName");
			ks.store(fos, password);
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}
}
