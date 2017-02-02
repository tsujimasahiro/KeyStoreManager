import javax.security.cert.X509Certificate;
import java.security.SignatureException;
import java.security.PublicKey;
import java.io.FileInputStream;
public class X509Test {
 public static void main(String[] args) throws Exception {
  FileInputStream in = new FileInputStream(args[0]);
  X509Certificate cert0 = X509Certificate.getInstance(in);
  System.out.println(cert0);
  in = new FileInputStream(args[1]);
  X509Certificate cert1 = X509Certificate.getInstance(in);
  System.out.println(cert1);
 	PublicKey pk = cert1.getPublicKey();
 	try {
 	 System.out.println("verifying:"+args[0]+" by "+ args[1]);
 	 cert0.verify(pk);
 	 System.out.println("  OK");
 	} catch(SignatureException e){
 	 System.out.println("  BAD");
 	} catch(Exception e){
 		e.printStackTrace();
 	}
 }
}