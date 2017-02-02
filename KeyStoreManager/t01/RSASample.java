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
 * RSA�ɂ��L�[������AES�Í����̃T���v��
 *
 * @author seraphy
 */
public class RSASample {

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) throws Exception {
		// ������ �}�V��A, B������A�܂���A����͂��܂�Ƒz�肷�� ������

		// �Í����L�[�����S�ɓ�_�ԂŌ������邽�߂�RSA�Í����L�[�𐶐�����.
		KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
		// 1024bit - 88bit = 117byte (�ő啽���T�C�Y)
		// PKCS#1�̃p�f�B���O��11byte�g��
		keygen.initialize(2048);
		KeyPair keyPair = keygen.generateKeyPair();

		// �閧�L�[
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		// ���J�L�[
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

		// �閧�L�[�ƌ��J�L�[��\��
		Key[] keys = new Key[] { privateKey, publicKey };
		for (Key key : keys) {
			String algo = key.getAlgorithm();
			String format = key.getFormat();
			byte[] bin = key.getEncoded();
			String encoded = toHexString(bin);
			System.out.println("algo=" + algo + "/format=" + format + "/key=" + encoded);
		}

		// RSA PublicKey���t�@�C���ɕۑ�����.
		byte[] byteModules = publicKey.getModulus().toByteArray();
		byte[] bytePublicExponent = publicKey.getPublicExponent().toByteArray();

		// public-key���o�C�i���œ]������ꍇ�ABigEndian�ł��邱�Ƃɒ���.
		FileOutputStream fos = new FileOutputStream("public.key");
		DataOutputStream dos = new DataOutputStream(fos);
		// java �̓f�t�H���g�� BigEndian �ŏ�������� ���̌���Ƃ��Ƃ肷��ꍇ�͗v����
		dos.writeInt(byteModules.length); // mod�l�̒���
		dos.write(byteModules); // mod�l
		dos.writeInt(bytePublicExponent.length); // �w���l�̒���
		dos.write(bytePublicExponent);// �w���l
		System.out.println("��:mod�l�̒���" + byteModules.length);
		System.out.println("��:mod�l" + toHexString(byteModules));
		System.out.println("��:�w���l�̒���" + bytePublicExponent.length);
		System.out.println("��:�w���l" + toHexString(bytePublicExponent));

		// �����̒l���󂯎�葤�œǂݎ���ė��p����
		DataInputStream dis = new DataInputStream(new FileInputStream("public.key"));

		byte[] b1 = new byte[257];
		byte[] b2 = new byte[3];

		System.out.println("��:mod�l�̒���" + dis.readInt());
		dis.read(b1);
		System.out.println("��:mod�l" + toHexString(b1));
		System.out.println("��:�w���l�̒���" + dis.readInt());
		dis.read(b2);
		System.out.println("��:�w���l" + toHexString(b2));
		dis.close();
		// }

		// ������ �����Ń}�V��B�Ɍ��J�L�[��]������Ƒz�� ������

		// ���J�L�[�𑼕��ɓ]�������ꍇ�A�܂����J�L�[�̃G���R�[�h�l�����Ƃ�KeySpec�ɕ�������
		// openssl rsa -in ./key.pem -pubout -out ./key.x509
		// (openssl��base64�������o�C�g��ƌ݊�)
		// X509EncodedKeySpec publicKeySpec = new
		// X509EncodedKeySpec(publicKey.getEncoded());

		// ���J�L�[�𑼕��ɓ]�������ꍇ�A�܂����J�L�[��modules��exponents�l�����Ƃ�PublicKey�𕜌�����.
		BigInteger modules = new BigInteger(b1);
		BigInteger publicExponent = new BigInteger(b2);
		RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modules, publicExponent);

		// KeySpec����A���JRSA�L�[�𕜌�����.
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		RSAPublicKey publicKey2 = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);

		// ���ʈÍ����̂��߂́A�p�X���[�h�ƃ\���g
		char[] password = "hello, world!!".toCharArray();
		SecureRandom secRandom = SecureRandom.getInstance("SHA1PRNG");
		// �\���g�͗����Őݒ肷��
		byte salt[] = new byte[3];
		secRandom.nextBytes(salt);
		System.out.println("salt:" + toHexString(salt));

		// ���ʈÍ����L�[���p�X���[�h�ƃ\���g���琶������.
		// �� JCE�iJava Cryptography Extension�j�̖��������x�̊Ǌ��|���V�[�t�@�C����
		// �ݒ肵�Ȃ��ƁA�W���ł�AES256�͎g���Ȃ��̂ŃL�[�̒�����128�ɂƂǂ߂�K�v����B
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec spec = new PBEKeySpec(password, salt, 65536, 128); // �L�[��128Bit
		SecretKey tmp = factory.generateSecret(spec);
		byte[] digest = tmp.getEncoded();

		// ���J�L�[�ŋ��ʈÍ��L�[���Í�������.
		byte[] encryptedData;
		{
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey2);
			encryptedData = cipher.doFinal(digest);
			System.out.println("digestLen:" + digest.length + " encryptedDataLen:" + encryptedData.length);
		}

		// ������ ������RSA�Í������ꂽ���ʈÍ����L�[���}�V��A�ɓ]������Ƒz�� ������

		// �閧�L�[�ŋ��ʈÍ��L�[�𕜍�������.
		byte[] digest2;
		{
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			System.out.println("digest:" + toHexString(encryptedData));
			digest2 = cipher.doFinal(encryptedData);
		}

		// ���̋��ʈÍ��L�[�ƁARSA�Í����o�R�œ]�����ꂽ���ʈÍ��L�[��\��
		System.out.println("digest(org)=" + toHexString(digest));
		System.out.println("digest(dec)=" + toHexString(digest2));

		// ���ʈÍ����L�[��p����AES/CBC/PKCS5Padding�Œ����f�[�^���Í�������.
		byte[] encrypted2;
		byte[] iv;
		{
			// DotNET�Ŏ󂯎��ꍇ�� AES/CBC/PKCK7Padding �ƂȂ邪�A
			// PKCS5��PKCS7�͓���padding�A���S���Y���ł��邽�ߖ��Ȃ�.
			// PKCS7 http://www.rfc-editor.org/rfc/rfc2315.txt
			// PKCS5 http://www.rfc-editor.org/rfc/rfc2898.txt

			SecretKey skey = new SecretKeySpec(digest2, "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, skey);

			iv = cipher.getIV();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();

			// �u���b�N�P�ʂňÍ���(CBC)���邽�߁A5�o�C�g���������ĈÍ�������
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

		// ������ �����Ń}�V��B�ɁA�Í������ꂽ�f�[�^��IV��]������Ƒz�� ������
		// AES�ł̈Í����ɂ�PBE(�p�X���[�h�����ɂ����Í���)�ňÍ��������L�[���g�p����
		// ���ʈÍ����L�[�ƁAIV��p���āA�����f�[�^�𕜍�������
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

		// �}�V��A����}�V��B�ɓ]�����ꂽ�f�[�^��\������.
		System.out.println("ret=" + ret);
	}

	/**
	 * �o�C�g���16�i��������ɕϊ�����.
	 *
	 * @param data
	 *            �o�C�g��
	 * @return 16�i��������
	 */
	private static String toHexString(byte[] data) {
		StringBuilder buf = new StringBuilder();
		for (byte d : data) {
			buf.append(String.format("%02X", d));
		}
		return buf.toString();
	}
}
