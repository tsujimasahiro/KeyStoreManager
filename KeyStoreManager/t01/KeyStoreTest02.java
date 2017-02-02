package t01;

import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.KeyStore.SecretKeyEntry;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import sun.nio.cs.StandardCharsets;

public class KeyStoreTest02 {

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) throws Exception {

		String keyStoreType = "JKS";
		String keyStoreUrl = "keyStoreName";

	    KeyStore ks = KeyStore.getInstance(keyStoreType);
	    char[] keyStorePassword = new char[]{123};

	    java.io.FileInputStream fis = null;
	    try {
	        fis = new java.io.FileInputStream(keyStoreUrl);
	        ks.load(fis, keyStorePassword);
	    } finally {
	        if (fis != null) {
	            fis.close();
	        }
	    }

	    Cipher cipher = Cipher.getInstance(algorithm);
	    cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(hexEncodedKey), getIv(hexEncodedIv));
	    byte[] encryptedSecret = cipher.doFinal(secret.getBytes(StandardCharsets.UTF_8));

	    SecretKeySpec keySpec = new SecretKeySpec(encryptedSecret, "AES");
	    KeyStore.SecretKeyEntry entry = new SecretKeyEntry(keySpec);
	    ks.setEntry(alias, entry, new KeyStore.PasswordProtection(keyStorePassword.toCharArray()));

	    try (FileOutputStream fos = new FileOutputStream(keyStoreUrl)) {
	        ks.store(fos, keyStorePassword.toCharArray());
	    }


		KeyStore ks = getKeyStore(keyStoreUrl, keyStorePassword, keyStoreType);
	    KeyStore.SecretKeyEntry secretKeyEntry =
	    (KeyStore.SecretKeyEntry) ks.getEntry(alias, new KeyStore.PasswordProtection(keyStorePassword.toCharArray()));
	    byte[] encrypted = secretKeyEntry.getSecretKey().getEncoded();

	    Cipher cipher = Cipher.getInstance(algorithm);
	    cipher.init(Cipher.DECRYPT_MODE, getSecretKey(hexEncodedKey), getIv(hexEncodedIv));
	    byte[] decrypted = cipher.doFinal(encrypted);

	    return new String(decrypted, StandardCharsets.UTF_8);


	}
}
