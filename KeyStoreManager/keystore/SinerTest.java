package keystore;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import sign.SignImpl;

public class SinerTest {

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

	// @Test
	public void 署名の取得() throws IOException,
			GeneralSecurityException {
		String message = "This is Signature Demo.";
		String alg = "MD5withRSA";
		SignImpl signImpl = new SignImpl();

		String alias = "SiriusWebKey05";
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\rsakeystore02"), "sirius", "JKS");
		Key key = ks.getPrivateKey(alias, "sirius");
		PrivateKey privKey = (PrivateKey) key;

		byte[] sign = signImpl.getSign(message, alg, privKey);

		assertTrue(sign.length > 0);
	}

	@Test
	public void 署名の検証_一致() throws IOException,
			GeneralSecurityException {
		String message = "This is Signature Demo.";
		String alg = "SHA256withRSA";

		String alias = "SiriusWebKey05";
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\rsakeystore02"), "sirius", "JKS");
		Key key = ks.getPrivateKey(alias, "sirius");
		PrivateKey privKey = (PrivateKey) key;
		Certificate cert = ks.getCertificate(alias);
		PublicKey pubkey = cert.getPublicKey();

		SignImpl signImpl = new SignImpl();
		byte[] sign = signImpl.getSign(message, alg, privKey);
		// メッセージと署名の検証
		boolean result = signImpl.verifySign(message, alg, sign, pubkey);

		assertTrue(result);
	}

	@Test
	public void 署名の検証_不一致() throws IOException,
			GeneralSecurityException {
		String message = "This is Signature Demo.";
		String alg = "SHA256withRSA";

		String alias = "SiriusWebKey05";
		KeyStoreWrapper ks = new KeyStoreWrapper(new File("C:\\tmp\\keystore\\rsakeystore02"), "sirius", "JKS");
		Key key = ks.getPrivateKey(alias, "sirius");
		PrivateKey privKey = (PrivateKey) key;

		String alias2 = "SiriusWebKey06";
		Certificate cert = ks.getCertificate(alias2);
		PublicKey pubkey = cert.getPublicKey();

		SignImpl signImpl = new SignImpl();
		byte[] sign = signImpl.getSign(message, alg, privKey);
		// メッセージと署名の検証
		boolean result = signImpl.verifySign(message, alg, sign, pubkey);

		assertNotNull(sign);
		assertNotNull(pubkey);
		assertFalse(result);
	}

	/**
	 * キーを16進数で出力
	 *
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