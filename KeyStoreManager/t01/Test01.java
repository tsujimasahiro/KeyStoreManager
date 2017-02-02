package t01;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import junit.framework.TestCase;

import org.junit.Test;

public class Test01 extends TestCase{

	/** 画面初期表示時のコマンドの正規表現オブジェクト1 */
	protected static final Pattern PATTERN_CMD1 = Pattern.compile(
			"(TM_.*?)\\s|(TM_.*?)$");

	/** 画面初期表示時のコマンドの正規表現オブジェクト2 */
	protected static final Pattern PATTERN_CMD2 = Pattern.compile(
			"cmdOpenC\\w\\w\\wG\\d\\d\\d\\w\\w");

	/** 画面初期表示時のコマンドの正規表現オブジェクト1 */
	protected static final Pattern ptnErrCd = Pattern.compile("printExclusiveErrLog ERRCD=(\\d+)");

	/** SAのテーブル名の正規表現オブジェクト */
	protected static final Pattern ptnSaTbl = Pattern.compile("TM_BLL|TM_BLM|TM_BPZ|TM_HLCLD");

	/** 不正なリクエスト */
	protected static final Pattern NormalGetlRequest = Pattern.compile(".*/subwin\\?|.*/jsp\\?");

	/** データBean名を取得する正規表現オブジェクト1 */
	protected static final Pattern PTN_BEANNAME = Pattern.compile("uji.bean=([^&]+)|beanName=([^&]+)");

	/** コマンド名を取得する正規表現オブジェクト1 */
	protected static final Pattern PTN_VERBNAME = Pattern.compile("uji.verb=([^&]+)|verb=([^&]+)");

	/** initParam */
	protected static final Pattern PTN_INITPARAM = Pattern.compile("initParam=([^&]*)");

//	protected static final Pattern PTN_BEAN_AND_VERB = Pattern.compile("^(?=(?:^|.*&)beanName=)(?=(?:^|.*&)verb=)|^(?=(?:^|.*&)uji.bean=)(?=(?:^|.*&)uji.verb=)");
	protected static final Pattern PTN_BEAN_AND_VERB = Pattern.compile("(?<=beanName=)(?<=verb=)");

	/**
	 * test関数
	 */
	@Test public void test002() throws Exception {

		byte[] bytes = DatatypeConverter.parseHexBinary("0A0A0A");

		System.out.println(Integer.toHexString(127));
		byte salt[] = new byte[1];
		salt[0] = 1;
//		salt[1] = -1;
//		salt[2] = 1;
		printEncryptData(salt);
		printEncryptData2(salt);


		Matcher mtch = PTN_BEAN_AND_VERB.matcher("verb=cmdOpenCSCZG020SR&beanName=jp.co.smfc.cscz.bean.CSCZG820SUBean&initParam=01&initParam=&initParam=&initParam=2");
		if (mtch.find()) {
			System.out.println("match:" + mtch.groupCount());

//			System.out.println("match str:" + mtch.group(0));
			for (int i = 1; i <= mtch.groupCount(); i++) {
				System.out.println("match str:" + mtch.group(i));
			}
		}

		System.out.println(Boolean.valueOf(null));

		if(1==1)return;

		if ("aaa".indexOf(null) > -1) {
			System.out.println("aaa");
		}

	}
	// encryptData の表示用メソッド
	public static void printEncryptData(byte[] b) {
		for (int i = 0; i < b.length; i++) {
			String h = Integer.toHexString(b[i]);
			System.out.print(h + " ");
		}
		System.out.println();
	}
	// encryptData の表示用メソッド
	public static void printEncryptData2(byte[] b) {
		for (int i = 0; i < b.length; i++) {
			// int型で16進数になおすため、000000ff とANDをして1バイトにしぼる
			String h = Integer.toHexString(b[i] & 0xff);
			System.out.print(h + " ");
		}
		System.out.println();
	}


}
