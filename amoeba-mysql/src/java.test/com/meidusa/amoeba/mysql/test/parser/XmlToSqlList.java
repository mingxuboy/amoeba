package com.meidusa.amoeba.mysql.test.parser;

import java.io.InputStream;

import java.io.IOException;

import java.util.ArrayList;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

public class XmlToSqlList {

	public static List<String> executeXml2List(InputStream stream) {
		Document doc;// ÉùÃ÷xmlÎÄ¼þ

		List<String> tree = new ArrayList<String>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setIgnoringComments(true);

		dbf.setIgnoringElementContentWhitespace(true);

		try {

			DocumentBuilder db = dbf.newDocumentBuilder();

			doc = db.parse(stream);

			NodeList tagNodes = doc.getElementsByTagName("sql");

			for (int i = 0; i < tagNodes.getLength(); i++) {
				tree.add(tagNodes.item(i).getTextContent().trim());
			}

		} catch (ParserConfigurationException e) {
			e.printStackTrace();

		} catch (SAXException e) {

			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return tree;
	}

}
