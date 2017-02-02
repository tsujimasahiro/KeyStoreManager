package t01;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.junit.Test;

public class RegexAnd extends TestCase{

	protected static final Pattern PATTERN_ERRCD = Pattern.compile("^(?=.*apple)(?=.*orange)(?=.*peach)");


	/**
	 */
	@Test public void test002() throws Exception {

		Matcher mtch = PATTERN_ERRCD.matcher("lemon, apple, peach, orange, lemon");
		if (mtch.find()) {
//			for (int i = 0; i < mtch.groupCount(); i++) {
				System.out.println("match str:" + mtch.group(0));
//			}
		}
	}
}
