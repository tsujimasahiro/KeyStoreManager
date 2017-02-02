package t01;

// keytool -genkeypair -keysize 2048 -keyalg RSA -sigalg SHA256withRSA -alias sample -keystore sample -storepass sample
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;

public class CipherTest {

	private static PrivateKey privateKey;
	private static PublicKey publicKey;

	public static void main(String[] args) throws Exception {
		getKeyPair();

		String s = "ABCDE";
		byte[] encryptData = encrypt(s);
		byte[] decryptData = decrypt(encryptData);

		System.out.print("encryptData : ");
		printEncryptData(encryptData);
		System.out.println("decryptData : " + new String(decryptData));
	}

	// 暗号
	private static byte[] encrypt(String data) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		return cipher.doFinal(data.getBytes());
	}

	// 復号
	private static byte[] decrypt(byte[] data) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		return cipher.doFinal(data);
	}

	// encryptData の表示用メソッド
	public static void printEncryptData(byte[] b) {
		for (int i = 0; i < b.length; i++) {
			String h = Integer.toHexString(b[i] & 0xff);
			System.out.print(h + " ");
			// System.out.format("%02x ", b[i]);
		}
		System.out.println();
	}

	private static void getKeyPair() throws Exception {
		String ksType = "JKS";
		String keyStoreFile = "C:\\tmp\\SiriusWebKeyStore";
//		String publicKeyFile = "C:\\tmp\\SiriusWebKeyStore";
		String keyStorePass = "sirius";
		String alias = "SiriusWebKey99";
		String privateKeyPass = "sirius";

		KeyStore ks = KeyStore.getInstance(ksType);
		ks.load(new FileInputStream(keyStoreFile), keyStorePass.toCharArray());

		privateKey = (PrivateKey) ks.getKey(alias, privateKeyPass.toCharArray());

		InputStream inStream = new FileInputStream("C:\\tmp\\SiriusWebKey99.cer");
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate certificate = (X509Certificate)cf.generateCertificate(inStream);

		// キーストアから証明書を取得する場合
//		X509Certificate certificate = (X509Certificate) ks.getCertificate(alias);
//		X509Certificate certificate = X509Certificate.getInstance;

		publicKey = certificate.getPublicKey();

		//Windows キーストアから取得
//		KeyStore winks = KeyStore.getInstance("WINDOWS-MY");
//		winks.load(null, null);
//		Certificate cert = winks.getCertificate("SiriusWebKey99");
//		publicKey = cert.getPublicKey();
//		privateKey = (PrivateKey)ks.getKey("SiriusWebKey99", "sirius".toCharArray());

		System.out.println("********** PrivateKey **********");
		System.out.println(privateKey.toString());
		System.out.println();
		System.out.println("********** PublicKey **********");
		System.out.println(publicKey.toString());
	}
}
