package t01;

import java.io.*;
import javax.crypto.Cipher;
import javax.crypto.spec.*;

public class AesJavaEnc {

	/**
	 * メインメソッド
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		// 暗号化メソッド呼出
		new AesJavaEnc().encode();

		// 復号化メソッド呼出
		new AesJavaEnc().decode();
	}

	public void encode() {
		try {
			// 鍵
			//            byte[] kagi = { 0x00, 0x01, 0x02, 0x03,
			//                            0x04, 0x05, 0x06, 0x07,
			//                            0x08, 0x09, 0x0a, 0x0b,
			//                            0x0c, 0x0d, 0x0e, 0x0f };

            byte[] kagi = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 
    				10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 
    				20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 
    				30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 
    				40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 
    				50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 
    				60, 61, 62, 63, 64, 65, 66, 67, 68, 69,
    				70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
    				80, 81, 82, 83, 84, 85, 86, 87, 88, 89,
    				90, 91, 92, 93, 94, 95, 96, 97, 98, 99,
    				100, 101, 102, 103, 104, 105, 106, 107, 108, 109,
    				110, 111, 112, 113, 114, 115, 116, 117, 118, 119,
    				120, 121, 122, 123, 124, 125, 126, 127, 1, 2 };

			SecretKeySpec sks = new SecretKeySpec(kagi, 0, 16, "AES");

			// 暗号化
			Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
			c.init(Cipher.ENCRYPT_MODE, sks);
			byte input[] = "0123456789012345678901234567890".getBytes();
			byte encrypted[] = c.doFinal(input);

			for (int i = 0; i < encrypted.length; i++) {
				System.out.print(Integer.toHexString(encrypted[i] & 0xff) + " ");
			}
			System.out.println("\n" + encrypted.length);

			// 暗号化したデータを保存
			FileOutputStream fe = new FileOutputStream("a.enc");
			fe.write(encrypted);
			fe.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void decode() {
		try {
			// 鍵
            byte[] kagi = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 
    				10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 
    				20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 
    				30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 
    				40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 
    				50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 
    				60, 61, 62, 63, 64, 65, 66, 67, 68, 69,
    				70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
    				80, 81, 82, 83, 84, 85, 86, 87, 88, 89,
    				90, 91, 92, 93, 94, 95, 96, 97, 98, 99,
    				100, 101, 102, 103, 104, 105, 106, 107, 108, 109,
    				110, 111, 112, 113, 114, 115, 116, 117, 118, 119,
    				120, 121, 122, 123, 124, 125, 126, 127, 1, 2 };

            SecretKeySpec sks = new SecretKeySpec(kagi, 0, 16, "AES");

			// 暗号化データ読み込み
			byte[] input = new byte[32];
			FileInputStream fe = new FileInputStream("a.enc");
			fe.read(input);
			fe.close();

			// 復号
			Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
			c.init(Cipher.DECRYPT_MODE, sks);
			byte output[] = c.doFinal(input);

			System.out.println(new String(output));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
