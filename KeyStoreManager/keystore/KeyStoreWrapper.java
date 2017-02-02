package keystore;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import sun.security.x509.CertAndKeyGen;
import sun.security.x509.X500Name;

/**
 * @author tsuji
 *
 */
public class KeyStoreWrapper {
	private char[] storePass;

	private File storeFile;

	private KeyStore keyStore;

	// /////////////////////////////////////////////////////////////////////////

	/**
	 * デフォルトコンストラクタ
	 *
	 * @throws IOException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 */
	public KeyStoreWrapper() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
		this(getDefaultKeyStorePath(), "changeit", "JKS");
	}

	/**
	 * コンストラクタ
	 *　
	 * @throws IOException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 */
	public KeyStoreWrapper(File storeFile, String storePass, String keyStoreType) throws IOException,
			KeyStoreException, CertificateException, NoSuchAlgorithmException {
		this.storeFile = storeFile;
		this.storePass = storePass.toCharArray();
		this.keyStore = createKeyStore(this.storeFile, this.storePass, keyStoreType);
	}

	/**
	 * キーストアの取得
	 * @return
	 */
	public KeyStore getKeyStore() {
		return keyStore;
	}

	/**
	 * キーストアの設定
	 * @return
	 */
	public void setKeyStore(KeyStore keyStore) {
		this.keyStore = keyStore;
	}

	/**
	 * キーストアの保存
	 * @throws IOException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 */
	public void save() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
		this.save0(this.storeFile, this.storePass);
	}

	/**
	 * キーストアの保存
	 * @param storeFile
	 * @param storePass
	 * @throws IOException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 */
	public void save(File storeFile, String storePass) throws IOException, KeyStoreException, CertificateException,
			NoSuchAlgorithmException {
		this.storeFile = storeFile;
		this.save0(this.storeFile, storePass.toCharArray());
	}

	/**
	 * キーストアの保存
	 * @param storeFile
	 * @param storePass
	 * @throws IOException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 */
	private void save0(File storeFile, char[] storePass) throws IOException, KeyStoreException, CertificateException,
			NoSuchAlgorithmException {
		FileOutputStream fos = new FileOutputStream(storeFile);
		try {
			this.keyStore.store(fos, storePass);
		} finally {
			fos.close();
		}
	}

	/**
	 * キーのインポート
	 * @param alias
	 * @param certFile
	 * @throws IOException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 */
	public void importKey(String alias, File certFile) throws IOException, KeyStoreException, CertificateException {
		this.keyStore.setCertificateEntry(alias, createX509Certificate(certFile));
	}

	/**
	 * キーのインポート
	 * @param alias
	 * @param certificate
	 * @throws IOException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 */
	public void importKey(String alias, byte[] certificate) throws IOException, KeyStoreException, CertificateException {
		this.keyStore.setCertificateEntry(alias, createX509Certificate(new ByteArrayInputStream(certificate)));
	}

	/**
	 * キーの取得
	 * @param alias
	 * @return
	 * @throws KeyStoreException
	 */
	public Certificate getCertificate(String alias) throws KeyStoreException {
		return this.keyStore.getCertificate(alias);
	}

	/**
	 * キーの取得
	 * @param keyPass
	 * @param alias
	 * @return
	 * @throws UnrecoverableKeyException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 */
	public Key getPrivateKey(String alias, String keyPass) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
		KeyStore keyStore = this.keyStore;
		return keyStore.getKey(alias, keyPass.toCharArray());
}

	/**
	 * キーのエクスポート
	 * @param alias
	 * @param exportFile
	 * @throws IOException
	 * @throws KeyStoreException
	 * @throws CertificateEncodingException
	 */
	public void exportKey(String alias, File exportFile) throws IOException, KeyStoreException,
			CertificateEncodingException {
		FileOutputStream fos = new FileOutputStream(exportFile);
		try {
			exportKey(alias, fos);
		} finally {
			fos.close();
		}
	}

	/**
	 * キーのエクスポート
	 * @param alias
	 * @param out
	 * @throws IOException
	 * @throws KeyStoreException
	 * @throws CertificateEncodingException
	 */
	public void exportKey(String alias, OutputStream out) throws IOException, KeyStoreException,
			CertificateEncodingException {
		Certificate cert = this.keyStore.getCertificate(alias);
		out.write(cert.getEncoded());
	}

	/**
	 * キーの削除
	 * @param alias
	 * @throws KeyStoreException
	 */
	public void deleteKey(String alias) throws KeyStoreException {
		this.keyStore.deleteEntry(alias);
	}

	/**
	 * キーのエイリアスの一覧
	 * @return
	 * @throws KeyStoreException
	 */
	public Enumeration list() throws KeyStoreException {
		return this.keyStore.aliases();
	}

	/**
	 * キーストアの文字列化
	 * @return　String
	 */
	public String toString() {
		return "Type:" + this.keyStore.getType() + "\n" + "FilePath:" + storeFile.getAbsolutePath() + "\n"
				+ "PassWord:" + String.valueOf(storePass);
	}

	/**
	 * キーストアのデフォルトファイルパスの取得
	 * @return
	 */
	private static File getDefaultKeyStorePath() {
		return new File(System.getProperty("java.home") + "/lib/security/cacerts");
	}

	/**
	 * キーストアの生成
	 * @param keyStorefile
	 * @param keyStorePassword
	 * @param keyStoreType
	 * @return
	 * @throws IOException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 */
	private static KeyStore createKeyStore(File keyStorefile, char[] keyStorePassword, String keyStoreType)
			throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {

		KeyStore ks = KeyStore.getInstance(keyStoreType == null ? KeyStore.getDefaultType() : keyStoreType);
		FileInputStream fis = null;
		if (keyStorefile != null) {
			fis = new FileInputStream(keyStorefile);
		}
		try {
			ks.load(fis, keyStorePassword);
			return ks;
		} finally {
			if (fis != null)
				fis.close();
		}
	}

	/**
	 * X.509 証明書の生成
	 * @param certFile
	 * @return
	 * @throws IOException
	 * @throws CertificateException
	 */
	private static X509Certificate createX509Certificate(File certFile) throws IOException, CertificateException {
		FileInputStream in = new FileInputStream(certFile);
		try {
			return createX509Certificate(in);
		} finally {
			in.close();
		}
	}

	/**
	 * X.509 証明書の生成
	 * @param in
	 * @return
	 * @throws IOException
	 * @throws CertificateException
	 */
	private static X509Certificate createX509Certificate(InputStream in) throws IOException, CertificateException {
		CertificateFactory cf = CertificateFactory.getInstance("X509");
		return (X509Certificate) cf.generateCertificate(in);
	}

	/**
	 * キーの登録
	 * @param keyStore
	 * @param keyPass
	 * @param alias
	 * @param params
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws CertificateException
	 * @throws SignatureException
	 */
	public X509Certificate[] entryKey(String keyPass, String alias, GenKeyParams params)
			throws NoSuchAlgorithmException, NoSuchProviderException, KeyStoreException, IOException,
			InvalidKeyException, CertificateException, SignatureException {
		if (keyStore.containsAlias(alias))
			throw new KeyStoreException("alias <" + alias + "> already exists");
		if (keyPass == null || keyPass.length() < 6)
			throw new InvalidKeyException("keyPass '" + keyPass + "'");

		CertAndKeyGen keypair = new CertAndKeyGen(params.keyAlgName, params.sigAlgName, null);
		X500Name x500Name = new X500Name(params.commonName, params.organizationUnit, params.organizationName,
				params.localityName, params.stateName, params.country);

		keypair.generate(params.keySize);
		PrivateKey privKey = keypair.getPrivateKey();
		X509Certificate[] chain = new X509Certificate[1];

		chain[0] = keypair.getSelfCertificate(x500Name, (long) params.validity);

		keyStore.setKeyEntry(alias, privKey, keyPass.toCharArray(), chain);
		return chain;
	}

	/**
	 * キー生成時のパラメタクラス
	 * @author tsuji
	 *
	 */
	static class GenKeyParams {
		private String commonName; // cn
		private String organizationUnit; // ou
		private String organizationName; // o
		private String localityName; // l
		private String stateName; // st
		private String country; // c
		private long validity = 10 * 365 * 24 * 60 * 60; // 10 years

		private String keyAlgName = "RSA";

		private int keySize = 2048;

		private String sigAlgName = "SHA256withRSA";

		public GenKeyParams(String commonName, String organizationUnit, String organizationName, String localityName,
				String stateName, String country) {
			this.commonName = commonName;
			this.organizationUnit = organizationUnit;
			this.organizationName = organizationName;
			this.localityName = localityName;
			this.stateName = stateName;
			this.country = country;
		}

		public void setKeyAlgName(String keyAlgName) throws NoSuchAlgorithmException {
			this.keyAlgName = keyAlgName;

			if (sigAlgName == null) {
				if (keyAlgName.equalsIgnoreCase("DSA")) {
					sigAlgName = "SHA1WithDSA";
				} else if (keyAlgName.equalsIgnoreCase("RSA")) {
					sigAlgName = "MD5WithRSA";
				} else {
					throw new NoSuchAlgorithmException("Cannot derive signature algorithm='" + keyAlgName + "'");
				}
			}
		}

		public void setSigAlgName(String sigAlgName) {
			this.sigAlgName = sigAlgName;
		}
	}

}