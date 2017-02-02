package t01;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import sun.misc.HexDumpEncoder;

/**
 * RSA Examination.
 *
 * @author kagyuu
 * @version $Revision$
 */
public class RSAExam {
	/** sources of the example document */
	private static String[] WISDOMS = new String[] {
		"Insanity: doing the same thing over and over again and expecting different results.", "Albert Einstein",
		"Happiness is nothing more than good health and a bad memory.", "Albert Schweitzer",
		"Our problems are man-made, therefore they may be solved by man.", "J.F.K",
		"Obstacles are those frightful things you see when you take your eyes off your goal. ", "Henry Ford" };

	/** Example plain document */
//	private static String EXAMPLE = WISDOMS[0] + WISDOMS[1];
	private static String EXAMPLE = "12345678901234561701";

	/** KEY LENGTH */
	private static int KEY_LENGTH = 2048;

	/**
	 * Main Routine.
	 *
	 * @param args
	 *            Commandline Argument
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException {

		// Create Key Pair
		KeyPair keyPair = createKeyPair();

//		System.out.println("Private Key: " + ((RSAKey) keyPair.getPrivate()).getModulus().bitLength() + "bit.");
//		System.out.println("Public Key: " + ((RSAKey) keyPair.getPublic()).getModulus().bitLength() + "bit.");

		// Raw data
//		System.out.println("\nRAW DATA");

		byte[] rawText = EXAMPLE.getBytes();

		// 共通暗号化のための、パスワードとソルト
		char[] password = "hello, world!!".toCharArray();
		SecureRandom secRandom = SecureRandom.getInstance("SHA1PRNG");
		// ソルトは乱数で設定する
		byte salt[] = new byte[3];
		secRandom.nextBytes(salt);
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec spec = new PBEKeySpec(password, salt, 65536, 128); // キーは128Bit
		SecretKey tmp = factory.generateSecret(spec);
		rawText = tmp.getEncoded();

		hexDump(rawText);

		try {

			long start = System.nanoTime();
			// Encrypt data
			byte[] encryptedText = encrypt(keyPair.getPublic(), rawText);
//			System.out.println("\nENCRYPTED DATA");
			hexDump(encryptedText);

			long end = System.nanoTime();
			System.out.println("Time:" + (end - start) / 1000000f + "ms");

			// Decrypt data
			byte[] decryptedText = decrypt(keyPair.getPrivate(), encryptedText);
//			System.out.println("\nDECRYPTED DATA");
			hexDump(decryptedText);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Create Key Pair.
	 *
	 * @return key pair
	 */
	private static KeyPair createKeyPair() {
		KeyPair keyPair = null;

		try {
			KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
			keygen.initialize(KEY_LENGTH);

			keyPair = keygen.generateKeyPair();

//			System.out.print("PUBLIC KEY (");
//			System.out.print(keyPair.getPublic().getAlgorithm() + " ");
//			System.out.println(keyPair.getPublic().getFormat() + ")");
			hexDump(keyPair.getPublic().getEncoded());

//			System.out.print("\nPRIVATE KEY (");
//			System.out.print(keyPair.getPrivate().getAlgorithm() + " ");
//			System.out.println(keyPair.getPrivate().getFormat() + ")");
			hexDump(keyPair.getPrivate().getEncoded());
		} catch (NoSuchAlgorithmException e) {
			// should not happen
			e.printStackTrace();
			System.exit(-1);
		}

		return keyPair;
	}

	/**
	 * Decrypt data.
	 *
	 * @param key
	 *            decrypt key
	 * @param data
	 *            encrypted data
	 *
	 * @return decrypted data
	 *
	 * @throws InvalidKeyException
	 *             key is wrong
	 * @throws IllegalBlockSizeException
	 *             data is wrong ( too long )
	 */
	private static byte[] decrypt(Key key, byte[] data) throws InvalidKeyException, IllegalBlockSizeException {
		byte[] decrypted = null;

		try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, key);
			decrypted = cipher.doFinal(data);
		} catch (NoSuchAlgorithmException e) {
			// should not happen
			e.printStackTrace();
			System.exit(-1);
		} catch (NoSuchPaddingException e) {
			// should not happen
			e.printStackTrace();
			System.exit(-1);
		} catch (BadPaddingException e) {
			// should not happen
			e.printStackTrace();
			System.exit(-1);
		}

		return decrypted;
	}

	/**
	 * Encrypt data.
	 *
	 * @param key
	 *            encrypt key
	 * @param data
	 *            data
	 *
	 * @return encrypted data
	 *
	 * @throws InvalidKeyException
	 *             key is wrong
	 * @throws IllegalBlockSizeException
	 *             data is wrong ( too long )
	 */
	private static byte[] encrypt(Key key, byte[] data) throws InvalidKeyException, IllegalBlockSizeException {
		byte[] encrypted = null;

		try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			encrypted = cipher.doFinal(data);

		} catch (NoSuchAlgorithmException e) {
			// should not happen
			e.printStackTrace();
			System.exit(-1);
		} catch (NoSuchPaddingException e) {
			// should not happen
			e.printStackTrace();
			System.exit(-1);
		} catch (BadPaddingException e) {
			// should not happen
			e.printStackTrace();
			System.exit(-1);
		}

		return encrypted;
	}

	/**
	 * Dump Byte Array.
	 *
	 * @param dump
	 *            byte array.
	 */
	private static void hexDump(byte[] dump) {
		HexDumpEncoder hexDump = new HexDumpEncoder();
		System.out.println(hexDump.encode(dump));
	}
	/**
	 * バイト列を16進数文字列に変換する.
	 *
	 * @param data
	 *            バイト列
	 * @return 16進数文字列
	 */
	private static String toHexString(byte[] data) {
		StringBuilder buf = new StringBuilder();
		for (byte d : data) {
			buf.append(String.format("%02X", d));
		}
		return buf.toString();
	}

}