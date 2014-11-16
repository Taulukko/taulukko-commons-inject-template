package com.evon.lang;

public class StringUtil {

	public static int convertToInt(byte t) {
		return (t < 0) ? 256 + t : t;
	}

	public static String getBytes(String value) {
		byte bytes[] = value.getBytes();

		String s = "{chars:[";

		for (int index = 0; index < bytes.length; index++) {
			if (index > 0) {
				s += ",";
			}
			if (index % 2 == 0) {
				s += "{unicodeDecimal:";
				char c = value.charAt(index / 2);
				s += (int) (c);
				s += ",unicodeHex:"
						+ Integer.toHexString((int) (c)).toUpperCase();
				s += ",rawInt:";
			}
			byte charByte = bytes[index];
			int charInt = convertToInt(charByte);
			String hexValue = Integer.toHexString(charInt);
			s += charInt + ",rawHex:" + hexValue.toUpperCase() + ",rawBinary:"
					+ Integer.toBinaryString(charInt);
			if (index % 2 == 1) {
				s += "}";
			}
		}
		s += "]}";
		return s;
	}
}