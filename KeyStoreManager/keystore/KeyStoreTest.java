package keystore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.security.Key;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Enumeration;

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

	@Test
	public void キーの登録() throws Exception {
		String alias = "SiriusWebKey06";
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