package sign;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

public class SignImpl {

	/**
	 * 署名の生成
	 *
	 * @param message
	 * @param alg
	 * @param key
	 * @return
	 */
	public byte[] getSign(String message, String alg, PrivateKey key) {
		try {
			// Signatureの初期化
			Signature signAlg = Signature.getInstance(alg);
			signAlg.initSign(key);
			signAlg.update(message.getBytes());
			// 署名の生成
			byte[] sign = signAlg.sign();
			return sign;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		byte[] bytes = null;
		return bytes;
	}

	/**
	 * メッセージと署名の検証
	 *
	 * @param message
	 * @param alg
	 * @param sign
	 * @param key
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	public boolean verifySign(String message, String alg, byte[] sign, PublicKey key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		// Signatureの初期化
		Signature signAlg = Signature.getInstance(alg);
		signAlg.initVerify(key);
		signAlg.update(message.getBytes());
		// メッセージの検証
		return signAlg.verify(sign);
	}
}
