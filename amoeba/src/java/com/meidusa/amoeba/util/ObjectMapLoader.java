/*
 * @(#)XMLUtils.java	1.6 05/11/17
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.meidusa.amoeba.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.meidusa.amoeba.config.DocumentUtil;

/**
 */
public class ObjectMapLoader {

    // XML loading and saving methods for Properties

    // The required DTD URI for exported properties
    private static final String PROPS_DTD_URI =
    "http://amoeba.meidusa.com/objectMap.dtd";

    private static final String PROPS_DTD =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    "<!-- DTD for properties -->"                +
    "<!ELEMENT bean ( property* ) >"                +
    "<!ATTLIST bean class NMTOKEN #REQUIRED >"                +

    "<!ELEMENT entry ( #PCDATA | bean )* >"                +
    "<!ATTLIST entry key NMTOKEN #REQUIRED >"                +

    "<!ELEMENT objectMap ( entry+ ) >"                +
    "<!ATTLIST objectMap version NMTOKEN #REQUIRED >"                +

    "<!ELEMENT property ( #PCDATA ) >"                +
    "<!ATTLIST property name NMTOKEN #REQUIRED >";            

    /**
     * Version number for the format of exported properties files.
     */

    public static void load(Map<String,Object> props, InputStream in)
        throws IOException, InvalidPropertiesFormatException
    {
        Document doc = null;
        try {
            doc = getLoadingDoc(in);
        } catch (SAXException saxe) {
            throw new InvalidPropertiesFormatException(saxe);
        }
        Element propertiesElement = (Element)doc.getChildNodes().item(1);
        importMap(props, propertiesElement);
    }

    static Document getLoadingDoc(InputStream in)
        throws SAXException, IOException
    {
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	dbf.setIgnoringElementContentWhitespace(true);
	dbf.setValidating(false);
        dbf.setCoalescing(true);
        dbf.setIgnoringComments(true);
	try {
	    DocumentBuilder db = dbf.newDocumentBuilder();
	    db.setEntityResolver(new Resolver());
	    db.setErrorHandler(new EH());
            InputSource is = new InputSource(in);
	    return db.parse(is);
	} catch (ParserConfigurationException x) {
	    throw new Error(x);
	}
    }

    private static void importMap(Map<String,Object> props, Element propertiesElement) {
        NodeList entries = propertiesElement.getChildNodes();
        int numEntries = entries.getLength();
        int start = numEntries > 0 && 
            entries.item(0).getNodeName().equals("comment") ? 1 : 0;
        for (int i=start; i<numEntries; i++) {
            Element entry = (Element)entries.item(i);
            if (entry.hasAttribute("key")) {
                Node n = entry.getFirstChild();
                Object val;
				try {
					val = loadBean(entry);
					 props.put(entry.getAttribute("key"), val);
				} catch (Exception e) {
					throw new Error(e.getMessage(),e);
				}
               
            }
        }
    }
    
    private static Object loadBean(Element keyElement) throws Exception{
    	NodeList entries = keyElement.getChildNodes();
    	for(int i=0;i<entries.getLength();i++){
    		Node node = entries.item(i);
       	 	if(node instanceof Element){
       	 		Element entry = (Element)node;
       	 		return  DocumentUtil.loadBeanConfig(entry).createBeanObject(true,System.getProperties());
       	 	}
    	}
  		String value = keyElement.getTextContent().trim();
  		return Class.forName(value).newInstance();
    	
    }

    private static class Resolver implements EntityResolver {
        public InputSource resolveEntity(String pid, String sid)
            throws SAXException
        {
            if (sid.equals(PROPS_DTD_URI)) {
                InputSource is;
                is = new InputSource(new StringReader(PROPS_DTD));
                is.setSystemId(PROPS_DTD_URI);
                return is;
            }
            throw new SAXException("Invalid system identifier: " + sid);
        }
    }

    private static class EH implements ErrorHandler {
        public void error(SAXParseException x) throws SAXException {
            throw x;
        }
        public void fatalError(SAXParseException x) throws SAXException {
            throw x;
        }
        public void warning(SAXParseException x) throws SAXException {
            throw x;
        }
    }
    
    public static void main(String[] args) throws Exception{
    	HashMap<String,Object> map = new HashMap<String,Object>();
    	load(map, new FileInputStream(new File("c:/1.xml")));
    	for(Map.Entry<String, Object> entry: map.entrySet()){
    		System.out.println("key="+entry.getKey()+",value="+entry.getValue());
    	}
    }

}
