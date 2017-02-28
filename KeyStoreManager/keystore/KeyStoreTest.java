package keystore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Enumeration;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import keystore.KeyStoreWrapper.GenKeyParams;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import sun.security.jca.Providers;

public class KeyStoreTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

//	@Test
	public void �L�[�X�g�A�̐���() throws Exception {
		KeyStoreWrapper ks = new KeyStoreWrapper(null, "sirius", "JCEKS");
		Enumeration enm = ks.list();
		while (enm.hasMoreElements()) {
			System.out.println(enm.nextElement());
		}
		ks.save(new File("C:\\tmp\\keystore\\SiriusKeyStore01"), "sirius");
		System.out.println(ks);
		assertEquals("Type:JCEKS\nFilePath:C:\\tmp\\keystore\\SiriusKeyStore01\nPassWord:sirius", ks.toString());
	}

	public void �L�[�X�g�A�̃��[�h() throws Exception {
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\SiriusKeyStore01"), "sirius", "JCEKS");
		Enumeration enm = ks.list();
		while (enm.hasMoreElements()) {
			System.out.println(enm.nextElement());
		}
		ks.save(new File("C:\\tmp\\keystore\\SiriusKeyStore01"), "sirius");
		System.out.println(ks);
		assertEquals("Type:JCEKS\nFilePath:C:\\tmp\\keystore\\SiriusKeyStore01\nPassWord:sirius", ks.toString());
	}

//	@Test
	public void �L�[�̓o�^() throws Exception {
		String alias = "SiriusWebKey01";
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\SiriusKeyStore01"), "sirius", "JCEKS");
		GenKeyParams params = new GenKeyParams(alias, "Sirius CA Services", "fip", "nakanoshima", "Osaka",
				"JP");
		params.setKeyAlgName("RSA");
		params.setSigAlgName("SHA256withRSA");
		ks.entryKey("sirius", alias, params);
		ks.save();
		Certificate cert = ks.getCertificate(alias);
		print(alias + ":", cert.getEncoded());
		ks.exportKey(alias, new File("C:\\tmp\\keystore\\"+ alias + ".csr"));
		// �w�肵���G�C���A�X�ŃL�[���Q�� null�@�łȂ�����
		assertNotNull(ks.getCertificate(alias));
	}

//	@Test
	public void �p�u���b�N�L�[�̎擾() throws Exception {
		String alias = "SiriusWebKey01";
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\SiriusKeyStore01"), "sirius", "JCEKS");
		Certificate cert = ks.getCertificate(alias);
		RSAPublicKey key = (RSAPublicKey)cert.getPublicKey();
		assertNotNull(key);
	}

//	@Test
	public void �v���C�x�[�g�L�[�̎擾() throws Exception {
		String alias = "SiriusWebKey05";
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\SiriusKeyStore01"), "sirius", "JCEKS");
		Key key = ks.getPrivateKey(alias, "sirius");
		key.getEncoded();
		PrivateKey privKey = (PrivateKey)key;
		assertNotNull(privKey);
	}

//	@Test
	public void �L�[�̍폜() throws Exception {
		String alias = "SIRIUSKEY01";
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\SiriusKeyStore01"), "sirius", "JCEKS");
		ks.deleteKey(alias);
		ks.save();
		// �w�肵���G�C���A�X�ŃL�[���Q�� null�@�ł��邱��
		assertNull(ks.getCertificate(alias));
	}

//	@Test
	public void �ؖ����̃C���|�[�g() throws Exception {
		String alias = "SiriusWebKey04";
		File certFile = new File("C:\\tmp\\keystore\\"+ alias + ".csr");
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\SiriusKeyStore01"), "sirius", "JCEKS");
		ks.importKey(alias, certFile);
		ks.save();
		// �w�肵���G�C���A�X�ŃL�[���Q�� null�@�łȂ�����
		assertNotNull(ks.getCertificate(alias));
	}

//	@Test
	public void �ؖ����̃G�N�X�|�[�g() throws Exception {
		String alias = "SiriusWebKey04";
		File certFile = new File("C:\\tmp\\keystore\\"+ alias + "Exp.csr");
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\SiriusKeyStore01"), "sirius", "JCEKS");
		ks.exportKey(alias, certFile);
		// �w�肵���G�C���A�X�ŃL�[���Q�� null�@�łȂ�����
		assertNotNull(certFile.exists());
	}

	@Test
	public void �Í���_������RSA() throws Exception {
		// �L�[�X�g�A����
		String alias = "SiriusWebKey01";
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\SiriusKeyStore01"), "sirius", "JCEKS");

		// �Í���
		String msg = "1234567890�������������@�S";
		System.out.println("�Í��O:" + msg);
		Certificate cert = ks.getCertificate(alias);
		RSAPublicKey pubKey = (RSAPublicKey)cert.getPublicKey();
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		byte[] encryptedData = cipher.doFinal(msg.getBytes("MS932"));
		print("encryptedData:", encryptedData);
		// ������
		PrivateKey privKey = (PrivateKey)ks.getPrivateKey(alias, "sirius");
		cipher.init(Cipher.DECRYPT_MODE, privKey);
		byte[] decryptedData = cipher.doFinal(encryptedData);
		print("srcData:", msg.getBytes("MS932"));
		print("decryptedData:", decryptedData);
		String msg2 = new String(decryptedData, 0, decryptedData.length, "MS932");
		System.out.println("��������:" + msg2);

		assertEquals(msg, msg2);
	}

//	@Test

	/**
	 * �f�[�^��AES�L�[�ňÍ�������
	 * AES�L�[�͈ꎞ�L�[�i�Z�b�V�����L�[�j�Ƃ��āA�s�x�A��������
	 * AES�L�[��RSA�L�[�ňÍ�������
	 * �@AES�L�[�𐶐�����
	 * �AAES�L�[�Ńf�[�^���Í���
	 * �B�L�[�X�g�A����RSA���J�L�[�����o���āAAES�L�[���Í���
	 * �C�L�[�X�g�A����RSA�閧�L�[�����o���āAAES�L�[�𕜍���
	 * �DAES�L�[�Ńf�[�^�𕜍���
	 * �E���̃f�[�^�ƕ����������f�[�^���A�T�[�g
	 */
	public void �Í���_������_RSA_AES() throws Exception {

		// �Í�����f�[�^
		String msg = "1234567890�������������@�S";
		byte[] msgB = msg.getBytes("MS932");
		System.out.println("�Í��O:" + msg);

		/** �@AES�L�[�𐶐����� */

		// ����������AES�L�[�i�ꎞ�L�[�j����
		final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		final KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(128, random); // AES256���g�����߂ɂ͊Ǌ��|���V�[�t�@�C����ݒ肷��K�v������
		byte[] aesKeyB = keyGen.generateKey().getEncoded();
		// ���d�l�ɕϊ�
		SecretKeySpec secKey = new SecretKeySpec(aesKeyB, "AES");

		 /** �AAES�L�[�Ńf�[�^���Í��� */

		// ������
		Cipher cipherAES = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipherAES.init(Cipher.ENCRYPT_MODE, secKey);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		// 1�u���b�N��1�o�C�g�Ƃ��āA�u���b�N���ƂɈÍ�������(CBC)
		for (int idx = 0; idx < msgB.length; idx++) {
			byte[] b1 = {msgB[idx]};
			bos.write(cipherAES.update(b1));
		}
		// �ŏI�u���b�N�����
		bos.write(cipherAES.doFinal());

		byte[] encryptedData = bos.toByteArray();
		print("encryptedData:", encryptedData);

		/** �B�L�[�X�g�A����RSA���J�L�[�����o���āAAES�L�[���Í��� */

		// RSA�L�[���L�[�X�g�A������o��
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\SiriusKeyStore01"), "sirius", "JCEKS");
		String alias = "SiriusWebKey06";
		Certificate cert = ks.getCertificate(alias);
		RSAPublicKey pubKey = (RSAPublicKey)cert.getPublicKey();

		// AES�L�[�i�ꎞ�L�[�j��RSA�ňÍ���
		Cipher cipherRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipherRSA.init(Cipher.ENCRYPT_MODE, pubKey);
		byte[] encryptedKey = cipherRSA.doFinal(secKey.getEncoded()); // RSA��ECB�Ȃ̂Ńu���b�N�������Ȃ�
		print("encryptedKey:", encryptedKey);

		/** �C�L�[�X�g�A����RSA�閧�L�[�����o���āAAES�L�[�𕜍��� */

		// AES�L�[�i�ꎞ�L�[�j�𕜍���
		PrivateKey privKey = (PrivateKey)ks.getPrivateKey(alias, "sirius");
		Cipher cipherRSA2 = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipherRSA2.init(Cipher.DECRYPT_MODE, privKey);
		byte[] decryptedKey = cipherRSA2.doFinal(encryptedKey);
		print("decryptedKey:", decryptedKey);
		assertEquals(new String(decryptedKey), new String(secKey.getEncoded()));

		/** �DAES�L�[�Ńf�[�^�𕜍��� */

		//  �f�[�^��AES�L�[�i�ꎞ�L�[�j�ŕ�����
		SecretKey secKey2 = new SecretKeySpec(decryptedKey, "AES");
		Cipher cipherAES2 = Cipher.getInstance("AES/CBC/PKCS5Padding");
		// ���������ɍ��킹�邽�ߏ������x�N�^�����o��
//		byte[] iv = cipherAES.getIV();
//		cipherAES2.init(Cipher.DECRYPT_MODE, secKey2, new IvParameterSpec(iv));
		cipherAES2.init(Cipher.DECRYPT_MODE, secKey2);
		ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
		bos2.write(cipherAES2.update(encryptedData));
		bos2.write(cipherAES2.doFinal());

		/** �E���̃f�[�^�ƕ����������f�[�^���A�T�[�g */

		// �����������f�[�^��\��
		byte[] decryptedData = bos2.toByteArray();
		String decryptedDataStr = new String(decryptedData, "MS932");
		System.out.println("decrypted Data:" + decryptedDataStr);
		// �e�X�g
		assertEquals(msg, decryptedDataStr);
	}

		/**
	 * �L�[��16�i���ŏo��
	 * @param tag
	 * @param bs
	 */
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