package t01;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

import com.sun.xml.internal.messaging.saaj.packaging.mime.MessagingException;
import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeUtility;

public class TestPulicKey {
  String data = "hogehogehogehoge";


  public void test() throws Exception {
      // ŒöŠJŒ®‚Ì“Ç‚Ýž‚Ý
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      InputStream is = null;

      try {
        is = new FileInputStream(new File("./mykey.x509"));
        int ch = 0;
        while ((ch = is.read()) >= 0) {
          baos.write(ch);
        }

      } finally {
        if(is != null) {
          is.close();
        }
      }
      // ˆÃ†‰»B
      byte[] encrypted = this.encrypt(data.getBytes(), baos.toByteArray());
      System.out.println("" + new String(encrypted));

      // ”é–§Œ®‚Ì“Ç‚Ýž‚Ý
      baos = new ByteArrayOutputStream();
      try {
        is = new FileInputStream(new File("./mykey.pkcs8"));
        int ch = 0;
        while ((ch = is.read()) >= 0) {
          baos.write(ch);
        }
      } finally {
        if(is != null) {
          is.close();
        }
      }
      // •œ†‰»B
      byte[] decrypted = this.decrypt(encrypted, baos.toByteArray());
      System.out.println("" + new String(decrypted));
    }

    public byte[] encrypt(final byte[] text, final byte[] publicKey)
            throws Exception {
      PublicKey key = KeyFactory.getInstance("RSA").generatePublic(
             new X509EncodedKeySpec(this.decodeBase64(publicKey)));

      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.ENCRYPT_MODE, key);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      CipherOutputStream cos = new CipherOutputStream(baos, cipher);
      cos.write(text);
      cos.close();

      return baos.toByteArray();
    }

    public byte[] decrypt(final byte[] text, final byte[] privateKey)
            throws Exception {
      PrivateKey key = KeyFactory.getInstance("RSA").generatePrivate(
             new PKCS8EncodedKeySpec(this.decodeBase64(privateKey)));

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.DECRYPT_MODE, key);
      CipherOutputStream cos = new CipherOutputStream(baos, cipher);
      cos.write(text);
      cos.close();

      return baos.toByteArray();
    }

    protected byte[] decodeBase64(final byte[] text) throws MessagingException,
            IOException {
      InputStream is = MimeUtility.decode(new ByteArrayInputStream(text), "base64");

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      int ch = 0;
      while ((ch = is.read()) >= 0) {
        baos.write(ch);
      }
      return baos.toByteArray();
    }
}
