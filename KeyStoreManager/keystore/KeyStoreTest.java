package keystore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
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
		KeyStoreWrapper ks = new KeyStoreWrapper(null, "sirius", "JKS");
		Enumeration enm = ks.list();
		while (enm.hasMoreElements()) {
			System.out.println(enm.nextElement());
		}
		ks.save(new File("C:\\tmp\\keystore\\rsakeystore02"), "sirius");
		System.out.println(ks);
		assertEquals("Type:JKS\nFilePath:C:\\tmp\\keystore\\rsakeystore02\nPassWord:sirius", ks.toString());
	}

	public void �L�[�X�g�A�̃��[�h() throws Exception {
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\rsakeystore02"), "sirius", "JKS");
		Enumeration enm = ks.list();
		while (enm.hasMoreElements()) {
			System.out.println(enm.nextElement());
		}
		ks.save(new File("C:\\tmp\\keystore\\rsakeystore02"), "sirius");
		System.out.println(ks);
		assertEquals("Type:JKS\nFilePath:C:\\tmp\\keystore\\rsakeystore02\nPassWord:sirius", ks.toString());
	}

//	@Test
	public void �L�[�̓o�^() throws Exception {
		String alias = "SiriusWebKey01";
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\rsakeystore02"), "sirius", "JKS");
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
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\rsakeystore02"), "sirius", "JKS");
		Certificate cert = ks.getCertificate(alias);
		RSAPublicKey key = (RSAPublicKey)cert.getPublicKey();
		assertNotNull(key);
	}

//	@Test
	public void �v���C�x�[�g�L�[�̎擾() throws Exception {
		String alias = "SiriusWebKey05";
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\rsakeystore02"), "sirius", "JKS");
		Key key = ks.getPrivateKey(alias, "sirius");
		key.getEncoded();
		PrivateKey privKey = (PrivateKey)key;
		assertNotNull(privKey);
	}

//	@Test
	public void �L�[�̍폜() throws Exception {
		String alias = "SIRIUSKEY01";
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\rsakeystore02"), "sirius", "JKS");
		ks.deleteKey(alias);
		ks.save();
		// �w�肵���G�C���A�X�ŃL�[���Q�� null�@�ł��邱��
		assertNull(ks.getCertificate(alias));
	}

//	@Test
	public void �ؖ����̃C���|�[�g() throws Exception {
		String alias = "SiriusWebKey04";
		File certFile = new File("C:\\tmp\\keystore\\"+ alias + ".csr");
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\rsakeystore02"), "sirius", "JKS");
		ks.importKey(alias, certFile);
		ks.save();
		// �w�肵���G�C���A�X�ŃL�[���Q�� null�@�łȂ�����
		assertNotNull(ks.getCertificate(alias));
	}

//	@Test
	public void �ؖ����̃G�N�X�|�[�g() throws Exception {
		String alias = "SiriusWebKey04";
		File certFile = new File("C:\\tmp\\keystore\\"+ alias + "Exp.csr");
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\rsakeystore02"), "sirius", "JKS");
		ks.exportKey(alias, certFile);
		// �w�肵���G�C���A�X�ŃL�[���Q�� null�@�łȂ�����
		assertNotNull(certFile.exists());
	}

//	@Test
	public void �Í���_������RSA() throws Exception {
		// �L�[�X�g�A����
		String alias = "SiriusWebKey06";
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\rsakeystore02"), "sirius", "JKS");

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

	@Test
	public void �Í���_������_RSA_AES() throws Exception {
		// �L�[�X�g�A����
		String alias = "SiriusWebKey06";
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\rsakeystore02"), "sirius", "JKS");

		// �Í���
		String msg = "1234567890�������������@�S";
		byte[] msgB = msg.getBytes("MS932");
		System.out.println("�Í��O:" + msg);

		//���b�Z�[�W��AES�ňÍ���
		byte[] aesKeyB = generateKey();
		SecretKeySpec secKey = new SecretKeySpec(aesKeyB, "AES");
		Cipher cipherAES = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipherAES.init(Cipher.ENCRYPT_MODE, secKey);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (int idx = 0; idx < msgB.length; idx++) {
			byte[] b1 = {msgB[idx]};
			bos.write(cipherAES.update(b1));
		}
		bos.write(cipherAES.doFinal());
		byte[] encryptedData = bos.toByteArray();
		print("encryptedData:", encryptedData);

		byte[] iv = cipherAES.getIV();

		//AES�L�[�i�ꎞ�L�[�j��l��RSA�ňÍ���
		Certificate cert = ks.getCertificate(alias);
		RSAPublicKey pubKey = (RSAPublicKey)cert.getPublicKey();
		Cipher cipherRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipherRSA.init(Cipher.ENCRYPT_MODE, pubKey);
		byte[] encryptedKey = cipherRSA.doFinal(secKey.getEncoded());
		print("encryptedKey:", encryptedKey);

		// �L�[�𕜍���
		PrivateKey privKey = (PrivateKey)ks.getPrivateKey(alias, "sirius");
		Cipher cipherRSA2 = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipherRSA2.init(Cipher.DECRYPT_MODE, privKey);
		byte[] decryptedKE�� = cipherRSA2.doFinal(encryptedKey);
		print("decryptedKey:", decryptedKE��);
		assertEquals(new String(decryptedKE��), new String(secKey.getEncoded()));

		//  �f�[�^�𕜍���
		SecretKey secKey2 = new SecretKeySpec(decryptedKE��, "AES");
		Cipher cipherAES2 = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipherAES2.init(Cipher.DECRYPT_MODE, secKey2, new IvParameterSpec(iv));
		ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
		bos2.write(cipherAES2.update(encryptedData));
		bos2.write(cipherAES2.doFinal());
		byte[] decryptedData = bos2.toByteArray();
		String decryptedDataStr = new String(decryptedData, 0, decryptedData.length, "MS932");
		System.out.println("decrypted Data:" + decryptedDataStr);
		assertEquals(msg, decryptedDataStr);
	}

	public static byte[] generateKey() throws NoSuchAlgorithmException {
		final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		// random.setSeed(getRandomSeedBytes()); // �����Ŗ{���̗����ŏ���������
		final KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(128, random);
		return keyGen.generateKey().getEncoded();
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