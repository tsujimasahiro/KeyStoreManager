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
		// ���𐶐�����B
		// ���̌���ۑ����ĈÍ�������������Q�ҊԂň��S�ɋ��L����B
		final byte[] key = generateKey();
		print("[Key]", key);

		// �Í����Ώۂ̕���
		final byte[] input = "0123456789012345".getBytes();
		print("[Input]", input);

		// ���ŕ������Í�������B
		// ���������邽�߂ɂ́A�u���v�ƁuIV(initial vector)�v�Ɓu�Í����v���K�v�B
		// �u���v�͎��O�Ɉ��S�ɋ��L����Ă���Ɖ��肵�āA�����ł́uIV�v�Ɓu�Í����v�����߂�B
		final EncryptedData encrypted = encrypt(key, input);

		// �����ɈÍ��������f�[�^��\�����Ă݂�B
		print("[IV]", encrypted.iv);
		print("[Encrypted]", encrypted.data);

		// �u���v�ƁuIV�v�Ɓu�Í����v��n���ĕ���������B
		final byte[] decrypted = decrypt(key, encrypted.iv, encrypted.data);

		// �����ɕ����������f�[�^��\�����Ă݂�B
		print("[Decrypted]", decrypted);

		// ����ɁA�������œ����������Í����E���������Ă݂�B
		final EncryptedData encrypted2 = encrypt(key, input);
		print("[IV(2)]", encrypted2.iv);
		print("[Encrypted(2)]", encrypted2.data);
		final byte[] decrypted2 = decrypt(key, encrypted.iv, encrypted.data);
		print("[Decrypted(2)]", decrypted2);
		System.out.println(new String(decrypted2));
	}

	public static byte[] generateKey() throws NoSuchAlgorithmException {
		final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		//random.setSeed(getRandomSeedBytes()); // �����Ŗ{���̗����ŏ���������
		final KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(128, random);
		return keyGen.generateKey().getEncoded();
	}

	public static EncryptedData encrypt(byte[] key, byte[] input) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		//random.setSeed(getRandomSeedBytes()); // �����Ŗ{���̗����ŏ���������
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
