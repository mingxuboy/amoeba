package com.meidusa.amoeba.route;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.meidusa.amoeba.config.BeanObjectEntityConfig;
import com.meidusa.amoeba.config.ConfigurationException;
import com.meidusa.amoeba.config.DocumentUtil;
import com.meidusa.amoeba.util.InitialisationException;

/**
 * 
 * @author struct
 *
 * @param <K>
 * @param <V>
 */
public abstract class FunctionLoader<K,V> {
	private static Logger logger = Logger.getLogger(FunctionLoader.class);
	private String dtdPath;
	private String dtdSystemID;
	public void setDTD(String dtdPath){
		this.dtdPath = dtdPath;
	}
	
	public void setDTDSystemID(String dtdSystemID){
		this.dtdSystemID = dtdSystemID;
	}
	
	public Map<K,V> loadFunctionMap(String configFileName){
		DocumentBuilder db;
		
    try {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(true);
        dbf.setNamespaceAware(false);

        db = dbf.newDocumentBuilder();
        db.setEntityResolver(new EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId) {
            	if (systemId.endsWith(dtdSystemID)) {
          	      InputStream in = AbstractQueryRouter.class.getResourceAsStream(dtdPath);
          	      if (in == null) {
          		LogLog.error("Could not find ["+dtdSystemID+"]. Used [" + AbstractQueryRouter.class.getClassLoader() 
          			     + "] class loader in the search.");
          		return null;
          	      } else {
          		return new InputSource(in);
          	      }
      	    } else {
      	      return null;
      	    }
            }
        });
        
        db.setErrorHandler(new ErrorHandler() {
            public void warning(SAXParseException exception) {
            }

            public void error(SAXParseException exception) throws SAXException {
                logger.error(exception.getMessage() + " at (" + exception.getLineNumber() + ":" + exception.getColumnNumber() + ")");
                throw exception;
            }

            public void fatalError(SAXParseException exception) throws SAXException {
                logger.fatal(exception.getMessage() + " at (" + exception.getLineNumber() + ":" + exception.getColumnNumber() + ")");
                throw exception;
            }
        });
        
        
       return loadFunctionFile(configFileName, db);
    } catch (Exception e) {
        logger.fatal("Could not load configuration file, failing", e);
        throw new ConfigurationException("Error loading configuration file " + configFileName, e);
    }
	
}

	private Map<K,V> loadFunctionFile(String fileName, DocumentBuilder db) throws InitialisationException {
	    Document doc = null;
	    InputStream is = null;
	    Map<K,V> funMap = new HashMap<K,V>();
	    try {
	        is = new FileInputStream(new File(fileName));
	
	        if (is == null) {
	            throw new Exception("Could not open file " + fileName);
	        }
	
	        doc = db.parse(is);
	    } catch (Exception e) {
	        final String s = "Caught exception while loading file " + fileName;
	        logger.error(s, e);
	        throw new ConfigurationException(s, e);
	    } finally {
	        if (is != null) {
	            try {
	                is.close();
	            } catch (IOException e) {
	                logger.error("Unable to close input stream", e);
	            }
	        }
	    }
	    Element rootElement = doc.getDocumentElement();
	    NodeList children = rootElement.getChildNodes();
	    int childSize = children.getLength();
	
	    for (int i = 0; i < childSize; i++) {
	        Node childNode = children.item(i);
	
	        if (childNode instanceof Element) {
	            Element child = (Element) childNode;
	
	            final String nodeName = child.getNodeName();
	            if (nodeName.equals("function")) {
	            	V function = loadFunction(child);
	            	putToMap(funMap,function);
	            }
	        }
	    }
	
	    if (logger.isInfoEnabled()) {
	        logger.info("Loaded function configuration from: " + fileName);
	    }
	    return funMap;
	}

	public abstract void putToMap(Map<K,V> map,V value);
	
	public abstract void initBeanObject(BeanObjectEntityConfig config,V bean);
	
	@SuppressWarnings("unchecked")
	protected V loadFunction(Element current){
		BeanObjectEntityConfig config = DocumentUtil.loadBeanConfig(current);
		V function = (V)config.createBeanObject(true);
		initBeanObject(config,function);
		return function;
	}
}
