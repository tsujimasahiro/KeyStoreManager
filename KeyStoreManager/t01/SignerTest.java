package jp.seraphyware.signertest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateIssuerName;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateSubjectName;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

public class SignerTest {

	/**
	 * RSAによる署名と、その検証方法の例. [参考]
	 * http://stackoverflow.com/questions/13207378/saving
	 * -certificate-chain-in-a-pkcs12-keystore
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		String keyName = "signerTest";
		char[] password = "password".toCharArray();

		// ※※※ マシンA, Bがあり、まずはAからはじまると想定する ※※※

		// (初回) RSAキーペアを生成し、キーストア(PKCS12)に保存する。
		byte[] keyStoreBuf; // キーストア
		byte[] x509Buf; // 公開キーをおさめたX509証明書
		ByteArrayOutputStream osPkcs12 = new ByteArrayOutputStream();
				ByteArrayOutputStream osCert = new ByteArrayOutputStream();
			createPKCS12(keyName, password, osPkcs12, osCert);
			osPkcs12.close();
			osCert.close();

			keyStoreBuf = osPkcs12.toByteArray();
			x509Buf = osCert.toByteArray();
		}

		// (二回目以降) キーストアからRSAキーペアを復元する。
		KeyPair keyPair;
		ByteArrayInputStream bis = new ByteArrayInputStream(keyStoreBuf);
		keyPair = loadPKCS12(keyName, password, bis);

		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

		// 署名するコンテンツを生成する
		StringBuilder messageBuf = new StringBuilder();
		for (int idx = 0; idx < 100; idx++) {
			messageBuf.append(String.format("こんにちは、世界! %03d\r\n", idx));
		}
		String message = messageBuf.toString();

		// RSA非公開キーで署名器を生成する.
		Signature signer = Signature.getInstance("SHA1withRSA");
		signer.initSign(privateKey);

		// 署名する.
		signer.update(message.getBytes("UTF-8"));
		byte[] sign = signer.sign();

		// ※※※ ここでマシンBにX509証明書と署名つきデータを転送すると想定 ※※※

		// X509証明書から、RSA公開キーを復元する.
		RSAPublicKey publicKey2;
		try (InputStream is = new ByteArrayInputStream(x509Buf)) {
			X509Certificate x509 = loadX509(is);
			publicKey2 = (RSAPublicKey) x509.getPublicKey();
		}

		// RSA公開キーで署名器を生成する.
		Signature verifier = Signature.getInstance("SHA1withRSA");
		verifier.initVerify(publicKey2);

		// 受け取ったメッセージと署名が改ざんされていないかチェックする.
		verifier.update(message.getBytes("UTF-8"));
		boolean result = verifier.verify(sign);

		// 結果の表示
		System.out.println("verify=" + result);
	}

	/**
	 * RSA暗号のキーペアを生成し、それをPKCS12(*.p12)形式で出力する.<br>
	 * 同時に、公開キーのみをおさめたX509証明書をder形式で出力する.<br>
	 * (出来上がったPKCS12, DERの出力ファイルはWindowsで開くことも可能.)<br>
	 * [参考]
	 * http://stackoverflow.com/questions/3313020/write-x509-certificate-into
	 * -pem-formatted-string-in-java
	 *
	 * @param keyName
	 *            キー名
	 * @param password
	 *            PKCS12のパスワード
	 * @param osPkcs12
	 *            PCCS12の出力先ストリーム
	 * @param osX509
	 *            X509証明書の出力先ストリーム
	 * @return 生成されたRSAキーペア
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public static KeyPair createPKCS12(
			String keyName,
			char[] password,
			OutputStream osPkcs12,
			OutputStream osX509
			) throws IOException, GeneralSecurityException {
		// RSA暗号化キーの生成
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(1024, new SecureRandom()); // 1024bit
		KeyPair keyPair = keyGen.generateKeyPair();

		// 秘密キー
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

		// 公開キー
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

		// X.509証明書作成
		X509Certificate x509cert = createCertificate(keyName, publicKey, privateKey);

		// 公開キーチェイン
		Certificate[] chain = { x509cert };

		// RSAキーペアをWindowsで開けるPKCS12形式で保存する.
		// ※ Windows上で証明書を管理するには certmgr.msc を用いる。
		KeyStore keyStore = KeyStore.getInstance("pkcs12");
		keyStore.load(null, null);
		keyStore.setKeyEntry(keyName, privateKey, password, chain);
		keyStore.store(osPkcs12, password);

		// 公開キーを格納した証明書をDER形式で出力する.
		try (OutputStreamWriter wr = new OutputStreamWriter(osX509)) {
			wr.write("-----BEGIN CERTIFICATE-----\r\n");
			wr.write(DatatypeConverter.printBase64Binary(x509cert.getEncoded()));
			wr.write("\r\n-----END CERTIFICATE-----\r\n");
		}

		return keyPair;
	}

	/**
	 * X509を生成する.<br>
	 * [参考]<br>
	 * 非公開メソッドの使用のため、Eclipseのコンパイル時にエラーとしないように設定する.
	 * http://svn.netlabs.org/repos/
	 * java/tags/rc/openjdk/jdk/test/sun/security/rsa/GenKeyStore.java
	 * もしくは、代替ライブラリとしては http://www.bouncycastle.org/ などがある。
	 *
	 * @param suffix
	 * @param publicKey
	 * @param privateKey
	 * @return X509Certificate
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws SignatureException
	 * @throws NoSuchProviderException
	 * @throws InvalidKeyException
	 * @throws Exception
	 */
	private static X509Certificate createCertificate(String suffix, PublicKey publicKey, PrivateKey privateKey)
			throws IOException, GeneralSecurityException {
		X500Name name = new X500Name("CN=Dummy Certificate " + suffix);
		String algorithm = "SHA1with" + publicKey.getAlgorithm();
		Date date = new Date();
		AlgorithmId algID = AlgorithmId.get(algorithm);

		X509CertInfo certInfo = new X509CertInfo();

		certInfo.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V1));
		certInfo.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(1));
		certInfo.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algID));
		certInfo.set(X509CertInfo.SUBJECT, new CertificateSubjectName(name));
		certInfo.set(X509CertInfo.ISSUER, new CertificateIssuerName(name));
		certInfo.set(X509CertInfo.KEY, new CertificateX509Key(publicKey));
		certInfo.set(X509CertInfo.VALIDITY, new CertificateValidity(date, date));

		X509CertImpl cert = new X509CertImpl(certInfo);
		cert.sign(privateKey, algorithm);

		return cert;
	}

	/**
	 * PKCS12形式からPublic/Private両キーを取得する.<br>
	 * [参考]<br>
	 * http://www.java2s.com/Code/Java/Security/RetrievingaKeyPairfromaKeyStore.
	 * htm
	 *
	 * @param keyName
	 *            キー名
	 * @param password
	 *            パスワード
	 * @param is
	 *            入力ストリーム
	 * @return キーペア
	 * @throws IOException
	 *             失敗
	 * @throws GeneralSecurityException
	 *             失敗
	 */
	public static KeyPair loadPKCS12(String keyName, char[] password, InputStream is) throws IOException,
			GeneralSecurityException {
		KeyStore keyStore = KeyStore.getInstance("pkcs12");
		keyStore.load(is, password);
		PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyName, password);

		X509Certificate cert = (X509Certificate) keyStore.getCertificate(keyName);
		PublicKey publicKey = (RSAPublicKey) cert.getPublicKey();

		return new KeyPair(publicKey, privateKey);
	}

	/**
	 * DER形式からX509証明書を取得する.<br>
	 * [参考] http://d.hatena.ne.jp/nahate/20090518
	 * http://stackoverflow.com/questions
	 * /8454677/create-privatekey-and-publickey
	 * -from-a-string-base64-encoding-with-der-format
	 *
	 * @param is
	 *            入力ストリーム
	 * @return X509証明書
	 * @throws IOException
	 *             失敗
	 * @throws GeneralSecurityException
	 *             失敗
	 */
	public static X509Certificate loadX509(InputStream is) throws IOException, GeneralSecurityException {
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		return (X509Certificate) certFactory.generateCertificate(is);
	}
}
