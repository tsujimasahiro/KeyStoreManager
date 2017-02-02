package t01;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * RSAによるキー交換とAES暗号化のサンプル
 *
 * @author seraphy
 */
public class RSASample {

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) throws Exception {
		// ※※※ マシンA, Bがあり、まずはAからはじまると想定する ※※※

		// 暗号化キーを安全に二点間で交換するためのRSA暗号化キーを生成する.
		KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
		// 1024bit - 88bit = 117byte (最大平文サイズ)
		// PKCS#1のパディングで11byte使う
		keygen.initialize(2048);
		KeyPair keyPair = keygen.generateKeyPair();

		// 秘密キー
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		// 公開キー
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

		// 秘密キーと公開キーを表示
		Key[] keys = new Key[] { privateKey, publicKey };
		for (Key key : keys) {
			String algo = key.getAlgorithm();
			String format = key.getFormat();
			byte[] bin = key.getEncoded();
			String encoded = toHexString(bin);
			System.out.println("algo=" + algo + "/format=" + format + "/key=" + encoded);
		}

		// RSA PublicKeyをファイルに保存する.
		byte[] byteModules = publicKey.getModulus().toByteArray();
		byte[] bytePublicExponent = publicKey.getPublicExponent().toByteArray();

		// public-keyをバイナリで転送する場合、BigEndianであることに注意.
		FileOutputStream fos = new FileOutputStream("public.key");
		DataOutputStream dos = new DataOutputStream(fos);
		// java はデフォルトで BigEndian で処理される 他の言語とやりとりする場合は要注意
		dos.writeInt(byteModules.length); // mod値の長さ
		dos.write(byteModules); // mod値
		dos.writeInt(bytePublicExponent.length); // 指数値の長さ
		dos.write(bytePublicExponent);// 指数値
		System.out.println("送:mod値の長さ" + byteModules.length);
		System.out.println("送:mod値" + toHexString(byteModules));
		System.out.println("送:指数値の長さ" + bytePublicExponent.length);
		System.out.println("送:指数値" + toHexString(bytePublicExponent));

		// これらの値を受け取り側で読み取って利用する
		DataInputStream dis = new DataInputStream(new FileInputStream("public.key"));

		byte[] b1 = new byte[257];
		byte[] b2 = new byte[3];

		System.out.println("受:mod値の長さ" + dis.readInt());
		dis.read(b1);
		System.out.println("受:mod値" + toHexString(b1));
		System.out.println("受:指数値の長さ" + dis.readInt());
		dis.read(b2);
		System.out.println("受:指数値" + toHexString(b2));
		dis.close();
		// }

		// ※※※ ここでマシンBに公開キーを転送すると想定 ※※※

		// 公開キーを他方に転送した場合、まず公開キーのエンコード値をもとにKeySpecに復元する
		// openssl rsa -in ./key.pem -pubout -out ./key.x509
		// (opensslのbase64化したバイト列と互換)
		// X509EncodedKeySpec publicKeySpec = new
		// X509EncodedKeySpec(publicKey.getEncoded());

		// 公開キーを他方に転送した場合、まず公開キーのmodulesとexponents値をもとにPublicKeyを復元する.
		BigInteger modules = new BigInteger(b1);
		BigInteger publicExponent = new BigInteger(b2);
		RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modules, publicExponent);

		// KeySpecから、公開RSAキーを復元する.
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		RSAPublicKey publicKey2 = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);

		// 共通暗号化のための、パスワードとソルト
		char[] password = "hello, world!!".toCharArray();
		SecureRandom secRandom = SecureRandom.getInstance("SHA1PRNG");
		// ソルトは乱数で設定する
		byte salt[] = new byte[3];
		secRandom.nextBytes(salt);
		System.out.println("salt:" + toHexString(salt));

		// 共通暗号化キーをパスワードとソルトから生成する.
		// ※ JCE（Java Cryptography Extension）の無制限強度の管轄ポリシーファイルを
		// 設定しないと、標準ではAES256は使えないのでキーの長さは128にとどめる必要あり。
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec spec = new PBEKeySpec(password, salt, 65536, 128); // キーは128Bit
		SecretKey tmp = factory.generateSecret(spec);
		byte[] digest = tmp.getEncoded();

		// 公開キーで共通暗号キーを暗号化する.
		byte[] encryptedData;
		{
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey2);
			encryptedData = cipher.doFinal(digest);
			System.out.println("digestLen:" + digest.length + " encryptedDataLen:" + encryptedData.length);
		}

		// ※※※ ここでRSA暗号化された共通暗号化キーをマシンAに転送すると想定 ※※※

		// 秘密キーで共通暗号キーを復号化する.
		byte[] digest2;
		{
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			System.out.println("digest:" + toHexString(encryptedData));
			digest2 = cipher.doFinal(encryptedData);
		}

		// 元の共通暗号キーと、RSA暗号化経由で転送された共通暗号キーを表示
		System.out.println("digest(org)=" + toHexString(digest));
		System.out.println("digest(dec)=" + toHexString(digest2));

		// 共通暗号化キーを用いてAES/CBC/PKCS5Paddingで長いデータを暗号化する.
		byte[] encrypted2;
		byte[] iv;
		{
			// DotNETで受け取る場合は AES/CBC/PKCK7Padding となるが、
			// PKCS5とPKCS7は同じpaddingアルゴリズムであるため問題ない.
			// PKCS7 http://www.rfc-editor.org/rfc/rfc2315.txt
			// PKCS5 http://www.rfc-editor.org/rfc/rfc2898.txt

			SecretKey skey = new SecretKeySpec(digest2, "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, skey);

			iv = cipher.getIV();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();

			// ブロック単位で暗号化(CBC)するため、5バイトずつ分割して暗号化する
			for (int idx = 0; idx < 100; idx++) {
				bos.write(cipher.update(String.format("(%03d) ", idx).getBytes("UTF-8")));
				// bos.write(cipher.update(String.format("%d",
				// idx).getBytes("UTF-8")));
			}
			bos.write(cipher.doFinal());

			// byte[] bb = new byte[60000];
			// System.out.println("bb len:" + bb.length);
			// String bstr = "";
			// for (int idx = 0; idx < 10000; idx++) {
			// bstr += String.format("(%03d) ", idx);
			// }
			// bb = bstr.getBytes();
			// System.out.println("bb len:" + bb.length);
			// bos.write(cipher.update(bb));
			// bos.write(cipher.doFinal(bb));

			encrypted2 = bos.toByteArray();
		}

		// ※※※ ここでマシンBに、暗号化されたデータとIVを転送すると想定 ※※※
		// AESでの暗号鍵にはPBE(パスワードを元にした暗号化)で暗号化したキーを使用する
		// 共通暗号化キーと、IVを用いて、長いデータを復号化する
		String ret;
		{
			SecretKey skey = new SecretKeySpec(digest, "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, skey, new IvParameterSpec(iv));

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			// for (byte b : encrypted2) {
			// bos.write(cipher.update(new byte[] { b }));
			// }
			bos.write(cipher.update(encrypted2));
			bos.write(cipher.doFinal());
			byte[] bytes = bos.toByteArray();
			ret = new String(bytes, 0, bytes.length, "UTF-8");
		}

		// マシンAからマシンBに転送されたデータを表示する.
		System.out.println("ret=" + ret);
	}

	/**
	 * バイト列を16進数文字列に変換する.
	 *
	 * @param data
	 *            バイト列
	 * @return 16進数文字列
	 */
	private static String toHexString(byte[] data) {
		StringBuilder buf = new StringBuilder();
		for (byte d : data) {
			buf.append(String.format("%02X", d));
		}
		return buf.toString();
	}
}
