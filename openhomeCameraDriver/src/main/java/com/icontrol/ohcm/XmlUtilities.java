package com.icontrol.ohcm;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.icontrol.openhome.data.ResponseStatus;
import com.icontrol.openhome.data.bind.DataBinder;

public class XmlUtilities {
	
	private static Logger logger = LoggerFactory.getLogger(XmlUtilities.class);
	
	/**
	 * Marshall an object to xml.
	 * 
	 * @param xmlObject
	 * @return xml encode string
	 */
	public static String marshalObject(Object xmlObject) {

		try {
			return DataBinder.toXML(xmlObject);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("marshalObject " + e.getMessage() , e.getCause());
		}
		
		return "";
		
	}

	/**
	 * Unmarshall an xml string to an object.
	 * 
	 * @param xmlString
	 * @return unmarshalled object
	 * @throws CameraException 
	 * @throws Exception
	 */
	public static Object unmarshalObject(String xmlString) throws CameraException  {
		
		/*
		 * If this is not an xml encoded string we'll get an exception right here
		 * and just let the caller figure it out. 
		 */
		Document xmlDocument = null;
		try {
			xmlDocument = getDocumentfromXml(xmlString);
		} catch (Exception e) {
			logger.error("unmarshalObject " + e.getMessage() , e.getCause());
			return null;
		}

		
    	//We'll assume that all the data object classes are in the same package as ResponseStatus
    	String className = new ResponseStatus().getClass().getName();
    	String rootNodeName = xmlDocument.getDocumentElement().getNodeName();
    	className = className.replace(new ResponseStatus().getClass().getSimpleName(), rootNodeName);

		

		try {
			return DataBinder.valueOfXML(new ByteArrayInputStream(xmlString.getBytes()));
		} catch (Exception e) {
			logger.error("unmarshalObject " + e.getMessage() , e.getCause());
			e.printStackTrace();
		}
		
		return xmlString;
		
	}
	
 
	/**
	 * Get a Document from the passed xml encoded string.
	 * 
	 * @param xmlString - xml encoded string.
	 * @return Document
	 * @throws Exception
	 */
	public static Document getDocumentfromXml(String xmlString) throws Exception {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setExpandEntityReferences(false);
        documentBuilderFactory.setValidating(false);
        documentBuilderFactory.setNamespaceAware(true);

        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
        builder.setEntityResolver(new EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException {
                return null;
            }
        });
        InputSource inputSource=new InputSource(new StringReader(xmlString));
        Document document = builder.parse(inputSource);
        return document;
		
	}

}
