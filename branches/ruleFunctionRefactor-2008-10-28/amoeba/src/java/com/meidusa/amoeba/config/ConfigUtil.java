package com.meidusa.amoeba.config;

import java.util.Properties;

/**
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public class ConfigUtil {
	public static String filter(String text) throws ConfigurationException {
		return filter(text,System.getProperties());
	}
	
	public static String filter(String text,Properties properties) throws ConfigurationException {
		String result = "";
		int cur = 0;
		int textLen = text.length();
		int propStart = -1;
		int propStop = -1;
		String propName = null;
		String propValue = null;
		for (; cur < textLen; cur = propStop + 1) {
			propStart = text.indexOf("${", cur);
			if (propStart < 0)
				break;
			result = result + text.substring(cur, propStart);
			propStop = text.indexOf("}", propStart);
			if (propStop < 0)
				throw new ConfigurationException("Unterminated property: "
						+ text.substring(propStart));
			propName = text.substring(propStart + 2, propStop);
			propValue = properties.getProperty(propName);
			if (propValue == null)
				throw new ConfigurationException("No such property: "
						+ propName);
			result = result + propValue;
		}

		result = result + text.substring(cur);
		return result;
	}
}
