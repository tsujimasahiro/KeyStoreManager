/**
 *
 */
package t01;

/**
 * @author tsuji
 *
 */
public class Sqrt {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		double result1 = Math.pow(2, 1.0/3);
		double result2 = Math.sqrt(4);
		System.out.println("result1 = " + result1);
		System.out.println("result2 = " + result2);
		result1 = Math.log(10);
		result2 = Math.log10(10);
		System.out.println("result1 = " + result1);
		System.out.println("result2 = " + result2);
		result1 = Math.abs(123.456);
		result2 = Math.abs(-987.654);
		System.out.println("result1 = " + result1);
		System.out.println("result2 = " + result2);
		result1 = Math.sin( Math.PI * 1/2);
		result2 = Math.cos(Math.PI * 2);
		System.out.println("result1 = " + result1);
		System.out.println("result2 = " + result2);
	}

}
