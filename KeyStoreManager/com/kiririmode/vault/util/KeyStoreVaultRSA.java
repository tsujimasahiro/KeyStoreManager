package com.kiririmode.vault.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.KeyStore.SecretKeyEntry;
import java.security.cert.CertificateException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class KeyStoreVaultRSA implements Vault {

	private String keyStoreUrl;
	private String keyStorePassword;
	private String keyStoreType;
	private String algorithm;
	private String hexEncodedKey;
	private String hexEncodedIv;

	public KeyStoreVaultRSA(String keyStoreUrl, String keyStorePassword, String keyStoreType, String algorithm,
			String hexEncodedKey, String hexEncodedIv) {
		this.keyStoreUrl = keyStoreUrl;
		this.keyStorePassword = keyStorePassword;
		this.keyStoreType = keyStoreType;
		this.algorithm = algorithm;
		this.hexEncodedKey = hexEncodedKey;
		this.hexEncodedIv = hexEncodedIv;
	}

	public KeyStore createKeyStore(String keyStoreUrl, String keyStorePassword, String keyStoreType)
			throws IOException, GeneralSecurityException {
		KeyStore ks = KeyStore.getInstance(keyStoreType);
		ks.load(null, keyStorePassword == null ? null : keyStorePassword.toCharArray());
		return ks;
	}

	public KeyStore getKeyStore(String keyStoreUrl, String keyStorePassword, String keyStoreType) throws IOException,
			GeneralSecurityException {
		File keyStoreFile = new File(keyStoreUrl);
		FileInputStream fis = new FileInputStream(keyStoreFile);
		try {
			KeyStore ks = KeyStore.getInstance(keyStoreType);
			ks.load(fis, keyStorePassword == null ? null : keyStorePassword.toCharArray());
			return ks;
		} finally {
			fis.close();
		}
	}

	@Override
	public void entry(String alias, String secret, KeyStore ks) throws VaultException {

		try {

			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(hexEncodedKey), getIv(hexEncodedIv));
			// cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(hexEncodedKey),
			// SecureRandom.getInstance("SHA1PRNG"));
			byte[] encryptedSecret = cipher.doFinal(secret.getBytes("MS932"));

			SecretKeySpec keySpec = new SecretKeySpec(encryptedSecret, "RSA/ECB/PKCS1Padding");
			KeyStore.SecretKeyEntry entry = new SecretKeyEntry(keySpec);
			ks.setEntry(alias, entry, new KeyStore.PasswordProtection(keyStorePassword.toCharArray()));

		} catch (Exception e) {
			throw new VaultException(String.format("store failed : keyStore[%s], alias: [%s]", keyStoreUrl, alias), e);
		}
	}

	public void store(KeyStore ks) throws FileNotFoundException, KeyStoreException, IOException,
			NoSuchAlgorithmException, CertificateException {
		FileOutputStream fos = new FileOutputStream(keyStoreUrl);
		try {
			ks.store(fos, keyStorePassword.toCharArray());
		} finally {
			fos.close();
		}
	}

	@Override
	public String retrieve(String alias) throws VaultException {
		try {
			KeyStore ks = getKeyStore(keyStoreUrl, keyStorePassword, keyStoreType);
			KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) ks.getEntry(alias,
					new KeyStore.PasswordProtection(keyStorePassword.toCharArray()));
			byte[] encrypted = secretKeyEntry.getSecretKey().getEncoded();
			System.out.println("一次符号化形式の名前:" + secretKeyEntry.getSecretKey().getFormat());

			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.DECRYPT_MODE, getSecretKey(hexEncodedKey), getIv(hexEncodedIv));
			byte[] decrypted = cipher.doFinal(encrypted);

			return new String(decrypted, "MS932");
		} catch (Exception e) {
			throw new VaultException(String.format("retrieve failed: keyStore[%s], alias: [%s]", keyStoreUrl, alias), e);
		}
	}

	private Key getSecretKey(String hexEncodedKey) throws Exception {
		// 16進数で表現された文字列を解釈して、binaryのbyte配列に変換する
		return new SecretKeySpec(DatatypeConverter.parseHexBinary(hexEncodedKey), "AES");
		// return new SecretKeySpec(generateKey(), "AES");
	}

	private AlgorithmParameterSpec getIv(String hexEncodedIv) {
		return new IvParameterSpec(DatatypeConverter.parseHexBinary(hexEncodedIv));
	}

	public static byte[] generateKey() throws NoSuchAlgorithmException {
		final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		// random.setSeed(getRandomSeedBytes()); // ここで本物の乱数で初期化する
		final KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(128, random);
		return keyGen.generateKey().getEncoded();
	}

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
