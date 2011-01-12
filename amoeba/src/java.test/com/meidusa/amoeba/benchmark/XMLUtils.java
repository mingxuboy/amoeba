/*
 * @(#)XMLUtils.java	1.6 05/11/17
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.meidusa.amoeba.benchmark;

import java.io.*;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.*;

import com.meidusa.amoeba.config.DocumentUtil;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

/**
 * A class used to aid in Properties load and save in XML. Keeping this
 * code outside of Properties helps reduce the number of classes loaded
 * when Properties is loaded.
 *
 * @version 1.9, 01/23/03
 * @author  Michael McCloskey
 * @since   1.3
 */
class XMLUtils {

    // XML loading and saving methods for Properties

    // The required DTD URI for exported properties
    private static final String PROPS_DTD_URI =
    "http://java.sun.com/dtd/properties.dtd";

    private static final String PROPS_DTD =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    "<!-- DTD for properties -->"                +
    "<!ELEMENT properties ( comment?, entry* ) >"+
    "<!ATTLIST properties"                       +
        " version CDATA #FIXED \"1.0\">"         +
    "<!ELEMENT comment (#PCDATA) >"              +
    "<!ELEMENT entry (#PCDATA) >"                +
    "<!ATTLIST entry "                           +
        " key CDATA #REQUIRED>";

    /**
     * Version number for the format of exported properties files.
     */
    private static final String EXTERNAL_XML_VERSION = "1.0";

    static void load(Map<String,Object> props, InputStream in)
        throws IOException, InvalidPropertiesFormatException
    {
        Document doc = null;
        try {
            doc = getLoadingDoc(in);
        } catch (SAXException saxe) {
            throw new InvalidPropertiesFormatException(saxe);
        }
        Element propertiesElement = (Element)doc.getChildNodes().item(1);
        String xmlVersion = propertiesElement.getAttribute("version");
        if (xmlVersion.compareTo(EXTERNAL_XML_VERSION) > 0)
            throw new InvalidPropertiesFormatException(
                "Exported Properties file format version " + xmlVersion +
                " is not supported. This java installation can read" +
                " versions " + EXTERNAL_XML_VERSION + " or older. You" +
                " may need to install a newer version of JDK.");
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
	    //db.setEntityResolver(new Resolver());
	    db.setErrorHandler(new EH());
            InputSource is = new InputSource(in);
	    return db.parse(is);
	} catch (ParserConfigurationException x) {
	    throw new Error(x);
	}
    }

    static void importMap(Map<String,Object> props, Element propertiesElement) {
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
    	 Node node = entries.item(0);
    	 if(node instanceof Element){
    		 Element entry = (Element)node;
    		 return  DocumentUtil.loadBeanConfig(entry).createBeanObject(true,System.getProperties());
    	 }else{
    		 String value = node.getTextContent().trim();
    		return Class.forName(value).newInstance();
    	 }
    	
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
    }

}
