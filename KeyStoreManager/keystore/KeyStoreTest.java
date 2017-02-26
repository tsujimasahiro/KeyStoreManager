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
	public void キーストアの生成() throws Exception {
		KeyStoreWrapper ks = new KeyStoreWrapper(null, "sirius", "JKS");
		Enumeration enm = ks.list();
		while (enm.hasMoreElements()) {
			System.out.println(enm.nextElement());
		}
		ks.save(new File("C:\\tmp\\keystore\\rsakeystore02"), "sirius");
		System.out.println(ks);
		assertEquals("Type:JKS\nFilePath:C:\\tmp\\keystore\\rsakeystore02\nPassWord:sirius", ks.toString());
	}

	public void キーストアのロード() throws Exception {
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
	public void キーの登録() throws Exception {
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
		// 指定したエイリアスでキーを参照 null　でないこと
		assertNotNull(ks.getCertificate(alias));
	}

//	@Test
	public void パブリックキーの取得() throws Exception {
		String alias = "SiriusWebKey01";
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\rsakeystore02"), "sirius", "JKS");
		Certificate cert = ks.getCertificate(alias);
		RSAPublicKey key = (RSAPublicKey)cert.getPublicKey();
		assertNotNull(key);
	}

//	@Test
	public void プライベートキーの取得() throws Exception {
		String alias = "SiriusWebKey05";
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\rsakeystore02"), "sirius", "JKS");
		Key key = ks.getPrivateKey(alias, "sirius");
		key.getEncoded();
		PrivateKey privKey = (PrivateKey)key;
		assertNotNull(privKey);
	}

//	@Test
	public void キーの削除() throws Exception {
		String alias = "SIRIUSKEY01";
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\rsakeystore02"), "sirius", "JKS");
		ks.deleteKey(alias);
		ks.save();
		// 指定したエイリアスでキーを参照 null　であること
		assertNull(ks.getCertificate(alias));
	}

//	@Test
	public void 証明書のインポート() throws Exception {
		String alias = "SiriusWebKey04";
		File certFile = new File("C:\\tmp\\keystore\\"+ alias + ".csr");
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\rsakeystore02"), "sirius", "JKS");
		ks.importKey(alias, certFile);
		ks.save();
		// 指定したエイリアスでキーを参照 null　でないこと
		assertNotNull(ks.getCertificate(alias));
	}

//	@Test
	public void 証明書のエクスポート() throws Exception {
		String alias = "SiriusWebKey04";
		File certFile = new File("C:\\tmp\\keystore\\"+ alias + "Exp.csr");
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\rsakeystore02"), "sirius", "JKS");
		ks.exportKey(alias, certFile);
		// 指定したエイリアスでキーを参照 null　でないこと
		assertNotNull(certFile.exists());
	}

//	@Test
	public void 暗号化_復号化RSA() throws Exception {
		// キーストア準備
		String alias = "SiriusWebKey06";
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\rsakeystore02"), "sirius", "JKS");

		// 暗号化
		String msg = "1234567890あいうえお㈱①⑳";
		System.out.println("暗号前:" + msg);
		Certificate cert = ks.getCertificate(alias);
		RSAPublicKey pubKey = (RSAPublicKey)cert.getPublicKey();
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		byte[] encryptedData = cipher.doFinal(msg.getBytes("MS932"));
		print("encryptedData:", encryptedData);
		// 復号化
		PrivateKey privKey = (PrivateKey)ks.getPrivateKey(alias, "sirius");
		cipher.init(Cipher.DECRYPT_MODE, privKey);
		byte[] decryptedData = cipher.doFinal(encryptedData);
		print("srcData:", msg.getBytes("MS932"));
		print("decryptedData:", decryptedData);
		String msg2 = new String(decryptedData, 0, decryptedData.length, "MS932");
		System.out.println("復号化後:" + msg2);

		assertEquals(msg, msg2);
	}

	@Test
	public void 暗号化_復号化_RSA_AES() throws Exception {
		// キーストア準備
		String alias = "SiriusWebKey06";
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\rsakeystore02"), "sirius", "JKS");

		// 暗号化
		String msg = "1234567890あいうえお㈱①⑳";
		byte[] msgB = msg.getBytes("MS932");
		System.out.println("暗号前:" + msg);

		//メッセージをAESで暗号化
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

		//AESキー（一時キー）を値をRSAで暗号化
		Certificate cert = ks.getCertificate(alias);
		RSAPublicKey pubKey = (RSAPublicKey)cert.getPublicKey();
		Cipher cipherRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipherRSA.init(Cipher.ENCRYPT_MODE, pubKey);
		byte[] encryptedKey = cipherRSA.doFinal(secKey.getEncoded());
		print("encryptedKey:", encryptedKey);

		// キーを復号化
		PrivateKey privKey = (PrivateKey)ks.getPrivateKey(alias, "sirius");
		Cipher cipherRSA2 = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipherRSA2.init(Cipher.DECRYPT_MODE, privKey);
		byte[] decryptedKEｙ = cipherRSA2.doFinal(encryptedKey);
		print("decryptedKey:", decryptedKEｙ);
		assertEquals(new String(decryptedKEｙ), new String(secKey.getEncoded()));

		//  データを復号化
		SecretKey secKey2 = new SecretKeySpec(decryptedKEｙ, "AES");
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
		// random.setSeed(getRandomSeedBytes()); // ここで本物の乱数で初期化する
		final KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(128, random);
		return keyGen.generateKey().getEncoded();
	}

		/**
	 * キーを16進数で出力
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