package t01;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesTest3 {
	public static class EncryptedData {
		public byte[] iv;
		public byte[] data;
	}

	public static void main(String[] args) throws Exception {
		// 鍵を生成する。
		// この鍵を保存して暗号文を交換する２者間で安全に共有する。
		final byte[] key = generateKey();
		print("[Key]", key);

		// 暗号化対象の平文
		final byte[] input = "0123456789012345".getBytes();
		print("[Input]", input);

		// 鍵で平文を暗号化する。
		// 復号化するためには、「鍵」と「IV(initial vector)」と「暗号文」が必要。
		// 「鍵」は事前に安全に共有されていると仮定して、ここでは「IV」と「暗号文」を求める。
		final EncryptedData encrypted = encrypt(key, input);

		// 試しに暗号化したデータを表示してみる。
		print("[IV]", encrypted.iv);
		print("[Encrypted]", encrypted.data);

		// 「鍵」と「IV」と「暗号文」を渡して復号化する。
		final byte[] decrypted = decrypt(key, encrypted.iv, encrypted.data);

		// 試しに復号化したデータを表示してみる。
		print("[Decrypted]", decrypted);

		// さらに、同じ鍵で同じ平文を暗号化・復号化してみる。
		final EncryptedData encrypted2 = encrypt(key, input);
		print("[IV(2)]", encrypted2.iv);
		print("[Encrypted(2)]", encrypted2.data);
		final byte[] decrypted2 = decrypt(key, encrypted.iv, encrypted.data);
		print("[Decrypted(2)]", decrypted2);
		System.out.println(new String(decrypted2));
	}

	public static byte[] generateKey() throws NoSuchAlgorithmException {
		final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		//random.setSeed(getRandomSeedBytes()); // ここで本物の乱数で初期化する
		final KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(128, random);
		return keyGen.generateKey().getEncoded();
	}

	public static EncryptedData encrypt(byte[] key, byte[] input) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		//random.setSeed(getRandomSeedBytes()); // ここで本物の乱数で初期化する
		final SecretKey secretKey = new SecretKeySpec(key, "AES");
		final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, random);
		final EncryptedData result = new EncryptedData();
		result.iv = cipher.getIV();
		result.data = cipher.doFinal(input);
		return result;
	}

	public static byte[] decrypt(byte[] key, byte[] iv, byte[] input) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException {
		final SecretKey secretKey = new SecretKeySpec(key, "AES");
		final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
		return cipher.doFinal(input);
	}

	public static void print(String tag, byte[] bs) {
		System.out.print(tag);
		for (int i = 0; i < bs.length; ++i) {
			if (i % 16 == 0) {
				System.out.println();
			}
			System.out.print(String.format(" %02X", bs[i]));
		}
		System.out.println();
	}
}
