package t01;

import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;

public class SecureRandomTest {
	public static void main(String args[]) {

		SecureRandom secRandom = null;
		byte bytes[] = new byte[16];
		try {
			Provider[] provs = Security.getProviders();
			for (Provider prov : provs) {
				System.out.println(prov.getName()+":"+prov.getInfo());
			}

			secRandom = SecureRandom.getInstance("SHA1PRNG");
			secRandom.nextBytes(bytes);
			print("byteArray:", bytes);
			secRandom.nextBytes(bytes);
			print("byteArray:", bytes);
		} catch (NoSuchAlgorithmException e) {
			System.out.println("‚»‚ñ‚ÈƒAƒ‹ƒSƒŠƒYƒ€‚Í‚È‚¢‚æ");
		}
		System.out.println(secRandom.nextDouble());
		System.out.println(secRandom.nextDouble());
		System.out.println(secRandom.nextDouble());
		System.out.println(bytes.length);
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
