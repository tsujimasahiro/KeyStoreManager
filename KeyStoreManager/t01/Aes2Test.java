package t01;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class Aes2Test {

  public static final String ENCRYPT_KEY = "1234567890123456";
	public static final String ENCRYPT_IV = "abcdefghijklmnop";

	/**
	 * ���C�����\�b�h
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		// �Í������\�b�h�ďo
		System.out.println(encrypt(args[0]));

		// ���������\�b�h�ďo
		System.out.println(decrypt(encrypt(args[0])));
	}

	/**
	 * �Í������\�b�h
	 *
	 * @param text �Í������镶����
	 * @return �Í���������
	 */
	public static String encrypt(String text) {
		// �ϐ�������
		String strResult = null;

		try {
			// ��������o�C�g�z��֕ϊ�
			byte[] byteText = text.getBytes("UTF-8");

			// �Í����L�[�Ə������x�N�g�����o�C�g�z��֕ϊ�
			byte[] byteKey = ENCRYPT_KEY.getBytes("UTF-8");
			byte[] byteIv = ENCRYPT_IV.getBytes("UTF-8");

			// �Í����L�[�Ə������x�N�g���̃I�u�W�F�N�g����
			SecretKeySpec key = new SecretKeySpec(byteKey, "AES");
			IvParameterSpec iv = new IvParameterSpec(byteIv);

			// Cipher�I�u�W�F�N�g����
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

			// Cipher�I�u�W�F�N�g�̏�����
			cipher.init(Cipher.ENCRYPT_MODE, key, iv);

			// �Í����̌��ʊi�[
			byte[] byteResult = cipher.doFinal(byteText);

			// Base64�փG���R�[�h
			strResult = Base64.encodeBase64String(byteResult);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}

		// �Í����������ԋp
		return strResult;
	}

	/**
	 * ���������\�b�h
	 *
	 * @param text ���������镶����
	 * @return ������������
	 */
	public static String decrypt(String text) {
		// �ϐ�������
		String strResult = null;

		try {
			// Base64���f�R�[�h
			byte[] byteText = Base64.decodeBase64(text);

			// �Í����L�[�Ə������x�N�g�����o�C�g�z��֕ϊ�
			byte[] byteKey = ENCRYPT_KEY.getBytes("UTF-8");
			byte[] byteIv = ENCRYPT_IV.getBytes("UTF-8");

			// �������L�[�Ə������x�N�g���̃I�u�W�F�N�g����
			SecretKeySpec key = new SecretKeySpec(byteKey, "AES");
			IvParameterSpec iv = new IvParameterSpec(byteIv);

			// Cipher�I�u�W�F�N�g����
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

			// Cipher�I�u�W�F�N�g�̏�����
			cipher.init(Cipher.DECRYPT_MODE, key, iv);

			// �������̌��ʊi�[
			byte[] byteResult = cipher.doFinal(byteText);

			// �o�C�g�z��𕶎���֕ϊ�
			strResult = new String(byteResult, "UTF-8");

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}

		// �������������ԋp
		return strResult;
	}
}
