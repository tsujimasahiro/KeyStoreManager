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
	 * RSA�ɂ�鏐���ƁA���̌��ؕ��@�̗�. [�Q�l]
	 * http://stackoverflow.com/questions/13207378/saving
	 * -certificate-chain-in-a-pkcs12-keystore
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		String keyName = "signerTest";
		char[] password = "password".toCharArray();

		// ������ �}�V��A, B������A�܂���A����͂��܂�Ƒz�肷�� ������

		// (����) RSA�L�[�y�A�𐶐����A�L�[�X�g�A(PKCS12)�ɕۑ�����B
		byte[] keyStoreBuf; // �L�[�X�g�A
		byte[] x509Buf; // ���J�L�[�������߂�X509�ؖ���
		ByteArrayOutputStream osPkcs12 = new ByteArrayOutputStream();
				ByteArrayOutputStream osCert = new ByteArrayOutputStream();
			createPKCS12(keyName, password, osPkcs12, osCert);
			osPkcs12.close();
			osCert.close();

			keyStoreBuf = osPkcs12.toByteArray();
			x509Buf = osCert.toByteArray();
		}

		// (���ڈȍ~) �L�[�X�g�A����RSA�L�[�y�A�𕜌�����B
		KeyPair keyPair;
		ByteArrayInputStream bis = new ByteArrayInputStream(keyStoreBuf);
		keyPair = loadPKCS12(keyName, password, bis);

		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

		// ��������R���e���c�𐶐�����
		StringBuilder messageBuf = new StringBuilder();
		for (int idx = 0; idx < 100; idx++) {
			messageBuf.append(String.format("����ɂ��́A���E! %03d\r\n", idx));
		}
		String message = messageBuf.toString();

		// RSA����J�L�[�ŏ�����𐶐�����.
		Signature signer = Signature.getInstance("SHA1withRSA");
		signer.initSign(privateKey);

		// ��������.
		signer.update(message.getBytes("UTF-8"));
		byte[] sign = signer.sign();

		// ������ �����Ń}�V��B��X509�ؖ����Ə������f�[�^��]������Ƒz�� ������

		// X509�ؖ�������ARSA���J�L�[�𕜌�����.
		RSAPublicKey publicKey2;
		try (InputStream is = new ByteArrayInputStream(x509Buf)) {
			X509Certificate x509 = loadX509(is);
			publicKey2 = (RSAPublicKey) x509.getPublicKey();
		}

		// RSA���J�L�[�ŏ�����𐶐�����.
		Signature verifier = Signature.getInstance("SHA1withRSA");
		verifier.initVerify(publicKey2);

		// �󂯎�������b�Z�[�W�Ə����������񂳂�Ă��Ȃ����`�F�b�N����.
		verifier.update(message.getBytes("UTF-8"));
		boolean result = verifier.verify(sign);

		// ���ʂ̕\��
		System.out.println("verify=" + result);
	}

	/**
	 * RSA�Í��̃L�[�y�A�𐶐����A�����PKCS12(*.p12)�`���ŏo�͂���.<br>
	 * �����ɁA���J�L�[�݂̂������߂�X509�ؖ�����der�`���ŏo�͂���.<br>
	 * (�o���オ����PKCS12, DER�̏o�̓t�@�C����Windows�ŊJ�����Ƃ��\.)<br>
	 * [�Q�l]
	 * http://stackoverflow.com/questions/3313020/write-x509-certificate-into
	 * -pem-formatted-string-in-java
	 *
	 * @param keyName
	 *            �L�[��
	 * @param password
	 *            PKCS12�̃p�X���[�h
	 * @param osPkcs12
	 *            PCCS12�̏o�͐�X�g���[��
	 * @param osX509
	 *            X509�ؖ����̏o�͐�X�g���[��
	 * @return �������ꂽRSA�L�[�y�A
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public static KeyPair createPKCS12(
			String keyName,
			char[] password,
			OutputStream osPkcs12,
			OutputStream osX509
			) throws IOException, GeneralSecurityException {
		// RSA�Í����L�[�̐���
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(1024, new SecureRandom()); // 1024bit
		KeyPair keyPair = keyGen.generateKeyPair();

		// �閧�L�[
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

		// ���J�L�[
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

		// X.509�ؖ����쐬
		X509Certificate x509cert = createCertificate(keyName, publicKey, privateKey);

		// ���J�L�[�`�F�C��
		Certificate[] chain = { x509cert };

		// RSA�L�[�y�A��Windows�ŊJ����PKCS12�`���ŕۑ�����.
		// �� Windows��ŏؖ������Ǘ�����ɂ� certmgr.msc ��p����B
		KeyStore keyStore = KeyStore.getInstance("pkcs12");
		keyStore.load(null, null);
		keyStore.setKeyEntry(keyName, privateKey, password, chain);
		keyStore.store(osPkcs12, password);

		// ���J�L�[���i�[�����ؖ�����DER�`���ŏo�͂���.
		try (OutputStreamWriter wr = new OutputStreamWriter(osX509)) {
			wr.write("-----BEGIN CERTIFICATE-----\r\n");
			wr.write(DatatypeConverter.printBase64Binary(x509cert.getEncoded()));
			wr.write("\r\n-----END CERTIFICATE-----\r\n");
		}

		return keyPair;
	}

	/**
	 * X509�𐶐�����.<br>
	 * [�Q�l]<br>
	 * ����J���\�b�h�̎g�p�̂��߁AEclipse�̃R���p�C�����ɃG���[�Ƃ��Ȃ��悤�ɐݒ肷��.
	 * http://svn.netlabs.org/repos/
	 * java/tags/rc/openjdk/jdk/test/sun/security/rsa/GenKeyStore.java
	 * �������́A��փ��C�u�����Ƃ��Ă� http://www.bouncycastle.org/ �Ȃǂ�����B
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
	 * PKCS12�`������Public/Private���L�[���擾����.<br>
	 * [�Q�l]<br>
	 * http://www.java2s.com/Code/Java/Security/RetrievingaKeyPairfromaKeyStore.
	 * htm
	 *
	 * @param keyName
	 *            �L�[��
	 * @param password
	 *            �p�X���[�h
	 * @param is
	 *            ���̓X�g���[��
	 * @return �L�[�y�A
	 * @throws IOException
	 *             ���s
	 * @throws GeneralSecurityException
	 *             ���s
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
	 * DER�`������X509�ؖ������擾����.<br>
	 * [�Q�l] http://d.hatena.ne.jp/nahate/20090518
	 * http://stackoverflow.com/questions
	 * /8454677/create-privatekey-and-publickey
	 * -from-a-string-base64-encoding-with-der-format
	 *
	 * @param is
	 *            ���̓X�g���[��
	 * @return X509�ؖ���
	 * @throws IOException
	 *             ���s
	 * @throws GeneralSecurityException
	 *             ���s
	 */
	public static X509Certificate loadX509(InputStream is) throws IOException, GeneralSecurityException {
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		return (X509Certificate) certFactory.generateCertificate(is);
	}
}
