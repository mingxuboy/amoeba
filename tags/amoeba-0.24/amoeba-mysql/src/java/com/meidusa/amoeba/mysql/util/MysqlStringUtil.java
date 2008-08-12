package com.meidusa.amoeba.mysql.util;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import com.meidusa.amoeba.util.StringUtil;

public class MysqlStringUtil extends StringUtil{
	/**
	 * Returns the byte[] representation of the given string (re)using the given
	 * charset converter, and the given encoding.
	 * 
	 * @param s
	 *            the string to convert
	 * @param converter
	 *            the converter to reuse
	 * @param encoding
	 *            the character encoding to use
	 * @param serverEncoding
	 *            DOCUMENT ME!
	 * @param parserKnowsUnicode
	 *            DOCUMENT ME!
	 * 
	 * @return byte[] representation of the string
	 * 
	 * @throws SQLException
	 *             if an encoding unsupported by the JVM is supplied.
	 * @throws UnsupportedEncodingException 
	 */
	public static final byte[] getBytes(String s,
			SingleByteCharsetConverter converter, String encoding,
			String serverEncoding, boolean parserKnowsUnicode)
			throws UnsupportedEncodingException {
			byte[] b = null;

			if (converter != null) {
				b = converter.toBytes(s);
			} else if (encoding == null) {
				b = s.getBytes();
			} else {
				b = s.getBytes(encoding);

				if (!parserKnowsUnicode && (encoding.equalsIgnoreCase("SJIS") //$NON-NLS-1$
						|| encoding.equalsIgnoreCase("BIG5") //$NON-NLS-1$
				|| encoding.equalsIgnoreCase("GBK"))) { //$NON-NLS-1$

					if (!encoding.equalsIgnoreCase(serverEncoding)) {
						b = escapeEasternUnicodeByteStream(b, s, 0, s.length());
					}
				}
			}
			return b;	
	}
}
