package com.kiririmode.vault.util;

import java.io.BufferedReader;
import java.io.Console;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyStore;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import com.kiririmode.vault.cmd.SimpleCommandLineParser;

public class Main {

	/** KeyStore の形式 */
	private static final String KEYSTORE_TYPE = "JCEKS";
	/** KeyStore で保持する秘匿情報に対して適用する暗号化アルゴリズム */
	private static final String ENCRYPT_ALGORITHM = "AES/CBC/PKCS5Padding";

	/** キーストアのパスを指定するためのコマンドラインオプション用キー */
	private static final String OPT_KEYSTORE_PATH = "keystore";
	/** 秘匿情報を格納したプロパティファイルのパスを指定するためのコマンドラインオプション用キー */
	private static final String OPT_PROPERTY_PATH = "secret";
	/** 暗号化用の秘密鍵や初期化ベクトルを定義したプロパティファイルのパスを指定するためのコマンドラインオプション用キー */
	private static final String OPT_VAULT_PROPERTY_PATH = "vault";

	private static final String KEY_VAULT_IV = "vault.iv";
	private static final String KEY_VAULT_KEY = "vault.key";

	public static void main(String[] args) throws Exception {

		try {
			// コマンドライン引数のパース
			SimpleCommandLineParser parser = new SimpleCommandLineParser(args);
			Map<String, String> optMap = parser.parseOption(OPT_KEYSTORE_PATH, OPT_PROPERTY_PATH,
					OPT_VAULT_PROPERTY_PATH);

			// コマンドラインで指定された値を保持
			String keyStorePath = "";
			if (optMap.get(OPT_KEYSTORE_PATH) != null) {
				keyStorePath = optMap.get(OPT_KEYSTORE_PATH);
			} else {
				throw new NullPointerException("-keystore keystorePath is missing");
			}
			String propertyPath = "";
			if (optMap.get(OPT_PROPERTY_PATH) != null) {
				propertyPath = optMap.get(OPT_PROPERTY_PATH);
			} else {
				throw new NullPointerException("-secret propertyPath is missing");
			}
			String vaultPropertyPath = "";
			if (optMap.get(OPT_VAULT_PROPERTY_PATH) != null) {
				vaultPropertyPath = optMap.get(OPT_VAULT_PROPERTY_PATH);
			} else {
				throw new NullPointerException("-vault propertyPath is missing");
			}
			Console console = null;
			if (optMap.get(OPT_VAULT_PROPERTY_PATH) != null) {
				console = System.console();
			} else {
				throw new NullPointerException("console cannot be retrieved");
			}

			// Vault の秘密鍵等を保持するプロパティファイル
			Properties vaultProp = readProperties(vaultPropertyPath);
			char[] password = console.readPassword("keystore password: ");
			KeyStoreVault vault = new KeyStoreVault(keyStorePath, new String(password), KEYSTORE_TYPE,
					ENCRYPT_ALGORITHM, vaultProp.getProperty(KEY_VAULT_KEY), vaultProp.getProperty(KEY_VAULT_IV));

			// 秘匿情報を保持するプロパティファイル

			// キーストア作成・取得
			KeyStore ks = vault.getKeyStore(keyStorePath, new String(password), KEYSTORE_TYPE);
			Enumeration en = vault.list(keyStorePath, new String(password), KEYSTORE_TYPE);
			while (en.hasMoreElements()) {
				System.out.println(en.nextElement());
			}
			// KeyStore ks = vault.createKeyStore(keyStorePath, new
			// String(password), KEYSTORE_TYPE);

			// キー登録
			Properties secretProp = readProperties(propertyPath);
			for (String alias : secretProp.stringPropertyNames()) {
				vault.entry(alias, secretProp.getProperty(alias), ks);
				System.out.println("alias:" + alias);
			}
			// キーストア登録
			vault.store(ks);
			// System.out.println(vault.retrieve("intra"));

		} catch (NullPointerException e) {
			System.err.println(e.getMessage());
			usage();
		}
	}

	static Properties readProperties(String filePath) throws IOException {
		Properties prop = null;
		// BufferedReader br = Files.newBufferedReader(new
		// File(filePath).toPath());
		BufferedReader br = new BufferedReader(new FileReader(filePath));

		try {
			prop = new Properties();
			prop.load(br);
		} finally {
			br.close();
		}
		return prop;
	}

	static void usage() {
		System.err
				.println("java -jar jar-${version}.jar -secret <property file path> -vault <property file path> -keystore <keystore path>");
	}
}
