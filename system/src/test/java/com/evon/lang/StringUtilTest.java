package com.evon.lang;

import org.junit.Assert;
import org.junit.Test;

import  com.evon.lang.StringUtil;

public class StringUtilTest {

	@Test
	public void getBytesTest() {
		String s = "éçã";

		// System.out.println(StringUtil.getBytes(s));
		Assert.assertEquals( 
				"{chars:["
						+ "{unicodeDecimal:233,unicodeHex:E9,rawInt:195,rawHex:C3,rawBinary:11000011,169,rawHex:A9,rawBinary:10101001},"
						+ "{unicodeDecimal:231,unicodeHex:E7,rawInt:195,rawHex:C3,rawBinary:11000011,167,rawHex:A7,rawBinary:10100111},"
						+ "{unicodeDecimal:227,unicodeHex:E3,rawInt:195,rawHex:C3,rawBinary:11000011,163,rawHex:A3,rawBinary:10100011}"
						+ "]}", StringUtil.getBytes(s));
	}
}
