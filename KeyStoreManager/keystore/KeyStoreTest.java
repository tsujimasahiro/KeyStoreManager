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
	public void キーストアの生成() throws Exception {
		KeyStoreWrapper ks = new KeyStoreWrapper(null, "sirius", "JCEKS");
		Enumeration enm = ks.list();
		while (enm.hasMoreElements()) {
			System.out.println(enm.nextElement());
		}
		ks.save(new File("C:\\tmp\\keystore\\SiriusKeyStore01"), "sirius");
		System.out.println(ks);
		assertEquals("Type:JCEKS\nFilePath:C:\\tmp\\keystore\\SiriusKeyStore01\nPassWord:sirius", ks.toString());
	}

	public void キーストアのロード() throws Exception {
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
	public void キーの登録() throws Exception {
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
		// 指定したエイリアスでキーを参照 null　でないこと
		assertNotNull(ks.getCertificate(alias));
	}

//	@Test
	public void パブリックキーの取得() throws Exception {
		String alias = "SiriusWebKey01";
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\SiriusKeyStore01"), "sirius", "JCEKS");
		Certificate cert = ks.getCertificate(alias);
		RSAPublicKey key = (RSAPublicKey)cert.getPublicKey();
		assertNotNull(key);
	}

//	@Test
	public void プライベートキーの取得() throws Exception {
		String alias = "SiriusWebKey05";
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\SiriusKeyStore01"), "sirius", "JCEKS");
		Key key = ks.getPrivateKey(alias, "sirius");
		key.getEncoded();
		PrivateKey privKey = (PrivateKey)key;
		assertNotNull(privKey);
	}

//	@Test
	public void キーの削除() throws Exception {
		String alias = "SIRIUSKEY01";
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\SiriusKeyStore01"), "sirius", "JCEKS");
		ks.deleteKey(alias);
		ks.save();
		// 指定したエイリアスでキーを参照 null　であること
		assertNull(ks.getCertificate(alias));
	}

//	@Test
	public void 証明書のインポート() throws Exception {
		String alias = "SiriusWebKey04";
		File certFile = new File("C:\\tmp\\keystore\\"+ alias + ".csr");
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\SiriusKeyStore01"), "sirius", "JCEKS");
		ks.importKey(alias, certFile);
		ks.save();
		// 指定したエイリアスでキーを参照 null　でないこと
		assertNotNull(ks.getCertificate(alias));
	}

//	@Test
	public void 証明書のエクスポート() throws Exception {
		String alias = "SiriusWebKey04";
		File certFile = new File("C:\\tmp\\keystore\\"+ alias + "Exp.csr");
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\SiriusKeyStore01"), "sirius", "JCEKS");
		ks.exportKey(alias, certFile);
		// 指定したエイリアスでキーを参照 null　でないこと
		assertNotNull(certFile.exists());
	}

	@Test
	public void 暗号化_復号化RSA() throws Exception {
		// キーストア準備
		String alias = "SiriusWebKey01";
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\SiriusKeyStore01"), "sirius", "JCEKS");

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

//	@Test

	/**
	 * データはAESキーで暗号化する
	 * AESキーは一時キー（セッションキー）として、都度、生成する
	 * AESキーはRSAキーで暗号化する
	 * ①AESキーを生成する
	 * ②AESキーでデータを暗号化
	 * ③キーストアからRSA公開キーを取り出して、AESキーを暗号化
	 * ④キーストアからRSA秘密キーを取り出して、AESキーを復号化
	 * ⑤AESキーでデータを復号化
	 * ⑥元のデータと復号化したデータをアサート
	 */
	public void 暗号化_復号化_RSA_AES() throws Exception {

		// 暗号するデータ
		String msg = "1234567890あいうえお㈱①⑳";
		byte[] msgB = msg.getBytes("MS932");
		System.out.println("暗号前:" + msg);

		/** ①AESキーを生成する */

		// 乱数を元にAESキー（一時キー）生成
		final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		final KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(128, random); // AES256を使うためには管轄ポリシーファイルを設定する必要がある
		byte[] aesKeyB = keyGen.generateKey().getEncoded();
		// 鍵仕様に変換
		SecretKeySpec secKey = new SecretKeySpec(aesKeyB, "AES");

		 /** ②AESキーでデータを暗号化 */

		// 初期化
		Cipher cipherAES = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipherAES.init(Cipher.ENCRYPT_MODE, secKey);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		// 1ブロックを1バイトとして、ブロックごとに暗号化する(CBC)
		for (int idx = 0; idx < msgB.length; idx++) {
			byte[] b1 = {msgB[idx]};
			bos.write(cipherAES.update(b1));
		}
		// 最終ブロックを閉じる
		bos.write(cipherAES.doFinal());

		byte[] encryptedData = bos.toByteArray();
		print("encryptedData:", encryptedData);

		/** ③キーストアからRSA公開キーを取り出して、AESキーを暗号化 */

		// RSAキーをキーストアから取り出す
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\SiriusKeyStore01"), "sirius", "JCEKS");
		String alias = "SiriusWebKey06";
		Certificate cert = ks.getCertificate(alias);
		RSAPublicKey pubKey = (RSAPublicKey)cert.getPublicKey();

		// AESキー（一時キー）をRSAで暗号化
		Cipher cipherRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipherRSA.init(Cipher.ENCRYPT_MODE, pubKey);
		byte[] encryptedKey = cipherRSA.doFinal(secKey.getEncoded()); // RSAはECBなのでブロック分割しない
		print("encryptedKey:", encryptedKey);

		/** ④キーストアからRSA秘密キーを取り出して、AESキーを復号化 */

		// AESキー（一時キー）を復号化
		PrivateKey privKey = (PrivateKey)ks.getPrivateKey(alias, "sirius");
		Cipher cipherRSA2 = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipherRSA2.init(Cipher.DECRYPT_MODE, privKey);
		byte[] decryptedKey = cipherRSA2.doFinal(encryptedKey);
		print("decryptedKey:", decryptedKey);
		assertEquals(new String(decryptedKey), new String(secKey.getEncoded()));

		/** ⑤AESキーでデータを復号化 */

		//  データをAESキー（一時キー）で復号化
		SecretKey secKey2 = new SecretKeySpec(decryptedKey, "AES");
		Cipher cipherAES2 = Cipher.getInstance("AES/CBC/PKCS5Padding");
		// 復号化時に合わせるため初期化ベクタを取り出す
//		byte[] iv = cipherAES.getIV();
//		cipherAES2.init(Cipher.DECRYPT_MODE, secKey2, new IvParameterSpec(iv));
		cipherAES2.init(Cipher.DECRYPT_MODE, secKey2);
		ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
		bos2.write(cipherAES2.update(encryptedData));
		bos2.write(cipherAES2.doFinal());

		/** ⑥元のデータと復号化したデータをアサート */

		// 復号化したデータを表示
		byte[] decryptedData = bos2.toByteArray();
		String decryptedDataStr = new String(decryptedData, "MS932");
		System.out.println("decrypted Data:" + decryptedDataStr);
		// テスト
		assertEquals(msg, decryptedDataStr);
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