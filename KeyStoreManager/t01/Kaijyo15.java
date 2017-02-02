/**
 *
 */
package t01;

/**
 * @author tsuji
 *
 */
public class Kaijyo15 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		double result1 = 0;
		double result2 = 1;
		result1 = 15d * 14 * 13 * 12 * 11 * 10 * 9 * 8 * 7 * 6 * 5 * 4 * 3 * 2 * 1;
		System.out.println("result1 = " + result1);
		for (int i = 15; i >= 1; --i) {
			result2 *= i;
		}
		System.out.println("result2 = " + result2);
	}

}
