package com.icontrol.openhome.data.bind;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.icontrol.openhome.data.*;


public class LocalDataBinder {
	
	private Class<?> currentClass;
	
	private static String openhomeDataPackage = ResponseStatus.class.getName();
	
	static {
		String responseClass = ResponseStatus.class.getName();
		int index = responseClass.indexOf(ResponseStatus.class.getSimpleName());
		openhomeDataPackage = responseClass.substring(0, index-1);
	}
	
	private static List<Class<?>> capList = new ArrayList<Class<?>>();
	
	static {
	
		capList.add(0, AddressingFormatCap.class); 
		capList.add(1, AudioCodecCap.class); 
		capList.add(2, BooleanCap.class); 
		capList.add(3, ConfigFileDataCap.class); 
		capList.add(4, DateTimeCap.class);	 
		capList.add(5, EventTypeCap.class);  
		capList.add(6, FloatCap.class); 
		capList.add(7, HexBinaryCap.class);  
		capList.add(8, IdCap.class);  
		capList.add(9, ImageTypeCap.class);  
		capList.add(10, IntegerCap.class);  
		capList.add(11, MACCap.class);  
		capList.add(12, OnOffCap.class);  
		capList.add(13, PercentageCap.class);  
		capList.add(14, PermissionCap.class);   
		capList.add(15, ProtocolCap.class); 
		capList.add(16, SeverityCap.class); 
		capList.add(17, StringCap.class);  
		capList.add(18, TimeCap.class); 
		capList.add(19, VectorCap.class); 
		
	}
	
	private static List<Class<?>> primitiveList = new ArrayList<Class<?>>();
	
	static {
		
		primitiveList.add(0, boolean.class);
		primitiveList.add(1, Boolean.class);
		primitiveList.add(2, char.class);
		primitiveList.add(3, Character.class);
		primitiveList.add(4, byte.class);
		primitiveList.add(5, Byte.class);
		primitiveList.add(6, short.class);
		primitiveList.add(7, Short.class);
		primitiveList.add(8, int.class);
		primitiveList.add(9, Integer.class);
		primitiveList.add(10, long.class);
		primitiveList.add(11, Long.class);
		primitiveList.add(12, float.class);
		primitiveList.add(13, Float.class);
		primitiveList.add(14, double.class);
		primitiveList.add(15, Double.class);
		primitiveList.add(16, String.class);
		primitiveList.add(17, BigInteger.class);
		
	}

    public static String capitalize(String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static String decapitalize(String str) {
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    public void serialize(StringBuilder sb, Object obj, String elementName) throws Exception {
        Class<?> objClass = obj.getClass();

        List<Method> subobjects = new ArrayList<Method>();
        sb.append("<").append(elementName).append(">");
        for (Method method : objClass.getMethods()) {
            if (method.getDeclaringClass().equals(Object.class))
                continue;
            if (method.getName().startsWith("get")) {
                String attr = "";
                if (method.getName().length() > 3) {  //added to avoid IndexOutOfBoundException
                    attr = decapitalize(method.getName().substring(3));
                    Object value = method.invoke(obj);
                    if (value != null && (!value.equals(method.getDefaultValue()))) {
                        String objectValue = null;
                        if (value instanceof Collection && (!method.getName().equals("getAny"))) {
                            for (Object subn : ((Collection) value)) {
                                serialize(sb, subn, attr);
                            }
                        }
                        if (Arrays.asList(AudioCodecCap.class, ConfigFileDataCap.class, EventTypeCap.class, ImageTypeCap.class,
                                AddressingFormatCap.class, PermissionCap.class, ProtocolCap.class, SeverityCap.class
                        ).contains(value.getClass())) {
                            Object object = value.getClass().getMethod("getValue").invoke(value);
                            objectValue = object.getClass().getMethod("value").invoke(object).toString();

                        } else if (Arrays.asList(IntegerCap.class, HexBinaryCap.class, DateTimeCap.class,TimeCap.class,
                                IdCap.class,StringCap.class,MACCap.class,UUIDCap.class,PercentageCap.class,
                                VectorCap.class).contains(value.getClass())
                                ||Arrays.asList(StringCap.class, IntegerCap.class,
                                VectorCap.class).contains(value.getClass().getSuperclass())) {//handle object that extends CapObject
                        	if (value.getClass().getMethod("getValue") != null) {
                        		Object getValue = value.getClass().getMethod("getValue").invoke(value);
                        		objectValue = getValue == null ? "" : getValue.toString();
                        	} else {
                            	objectValue = "";
                        	}
                        }
                        else if (value.getClass().isPrimitive() || Arrays.asList(
                                Boolean.class, BigInteger.class, BigDecimal.class, Character.class, Byte.class, Short.class, Integer.class,
                                Long.class, Float.class, Double.class, String.class, HashMap.class).contains(value.getClass())
                                ) {
                            if (!value.toString().equals("{}")) { //remove the case when hashmap is created as empty
                                objectValue = value.toString();
                            }
                        }
                        else if (value.getClass().isEnum())
                            objectValue = value.getClass().getMethod("value").invoke(value).toString();

                        else if (value.getClass().equals(FloatCap.class))
                            objectValue = Float.toString(((FloatCap) value).getValue());

                        else if (value.getClass().equals(BooleanCap.class))
                            objectValue = new Boolean(((BooleanCap) value).isValue()).toString();

                        else if (!(value instanceof Collection) || !((Collection) value).isEmpty()) {
                            subobjects.add(method);
                        }
                        if (objectValue != null && objectValue.length() > 0 && !attr.equalsIgnoreCase("version")) {
                            sb.append(" <").append(attr).append(">")
                                    .append(
                                            objectValue.replaceAll("&", "&amp;").replaceAll("\"", "&quot;").replaceAll("<", "&lt;").replaceAll(">", "&gt;")
                                    ).append("</" + attr + "").append(">");
                        }
                    }
                }
            }
        }
        if (subobjects.isEmpty()) {
            sb.append(" </" + elementName + ">");
        } else {
            for (Method method : subobjects) {
            	String methodName = method.getName();
            	if (methodName.length() > 3 && methodName.indexOf("get") == 0) {
            		methodName = methodName.substring(3);
            	}
                Object sub = method.invoke(obj);
                if (sub instanceof Collection) {
                    for (Object subn : ((Collection) sub)) {
                        //serialize(sb, subn, false);   //todo: temporary removed
                    }
                } else {
                    serialize(sb, sub, methodName);
                }
            }
            sb.append("</").append(elementName).append(">");
        }

    }

    public static String toString(Object obj) throws Exception {
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        new LocalDataBinder().serialize(sb, obj, obj.getClass().getSimpleName());
        String example=sb.toString();
        return example;
    }


    public Object valueOf(Node node) throws Exception {
        String objectClassName = capitalize(node.getNodeName());

        Class<?> objectClass;
        
        /*
         * Get the class of the property that corresponds to 
         * the name of this node. We will recurs until we 
         * get a primitive or a Cap.
         */
        objectClass = getClass(node);
        
        if (objectClass == null) return null;
        
        if (objectClass.isEnum()){
            return getEnumValue(objectClass, node.getTextContent());

        }
        
        /*
         * If this is an objectCap return the cap object with the 
         * value set from the node.
         */
        if (capList.contains(objectClass)) {
        	return getCapValue(objectClass, node.getTextContent());
        }
        
        if (StringCap.class.isAssignableFrom(objectClass)) {
        	try {
			Object capObject = objectClass.newInstance();
			((StringCap)capObject).setValue(node.getTextContent());
			return capObject;
        	} catch (Exception e) {
        		e.printStackTrace();
        		return null;
        	}
        }
        
        /*
         * If this is a primitive return the cap object with the 
         * value set from the node.
         */
        if (primitiveList.contains(objectClass)) {
        	return getPrimitiveValue(objectClass, node.getTextContent());
        }
        
        setCurrentClass(objectClass);

        Object obj = objectClass.newInstance();

        /*
         * For this node, go through the list of 
         * attributes and set their values in the 
         * object.
         */
        NamedNodeMap attributes = node.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            
            Class<?> attrClass = getClass(attr);
                        
            Method setter = setterForName(capitalize(attr.getNodeName()), attrClass);
            
            Class<?> nodeClass = getClass(attr);
            if (!primitiveList.contains(nodeClass)) {
            	continue;
            }
            
            Object attrValue = getPrimitiveValue(attrClass, attr.getNodeValue());
            
            setter.invoke(obj, attrValue);
            
        }

        /*
         * We still have an object that needs further breakdown.
         */
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            
            /*
             * Recurs through this method until we get a value back.
             */
        	Class<?> parentClass = currentClass;
            Object value = valueOf(childNode);
            currentClass = parentClass;
            
            String valueClassName = "";
            if (value != null) valueClassName = value.getClass().getName();
            
            /*
             * Find a setter for the property that corresponds to the
             * node name and set the value
             */
        	Method setter = setterForName(childNode.getNodeName(), getClass(childNode));
        	
        	if (setter != null) {
        		setter.invoke(obj, value);
        	} else {
        		Method getter = getterForName(childNode.getNodeName());
        		if (getter !=null 
    				&& (List.class.isAssignableFrom(getter.getReturnType()) 
        			|| value instanceof Node && getter.getName().equals("getAny"))) {
    	            ((List) getter.invoke(obj)).add(value);
        		}
        		
        	}
	
        }
        
        return obj;
    }

    public static Object valueOf(Document src) throws Exception {
        return new LocalDataBinder().valueOf(src.getDocumentElement());
    }

    @SuppressWarnings("unchecked")
	public static <T> T valueOf(InputStream is, Class<T> paramType) throws Exception {
    	
        String stringValue = convertStreamToString(is, "UTF-8");
        System.out.println("Get the string value of stream!"+stringValue);
        stringValue = stringValue.replaceAll("\r", "").replaceAll("\n", "");
         return (T) valueOf(stringValue);
    }
    
    public static Object valueOf(String stringValue) throws Exception {
    	
        stringValue=removeSpacesXml(stringValue);
        System.out.println(stringValue);
        Document document = getDocument(new StringReader(stringValue));
        return valueOf(document);
        
    }

    public static String convertStreamToString(InputStream is, String encoding) throws IOException {
        StringBuilder sb = new StringBuilder(Math.max(16, is.available()));
        char[] tmp = new char[4096];

        try {
            InputStreamReader reader = new InputStreamReader(is, encoding);
            for (int cnt; (cnt = reader.read(tmp)) > 0; )
                sb.append(tmp, 0, cnt);
        } finally {
            is.close();
        }
        return sb.toString();
    }

    static public Document getDocument(Reader source) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setExpandEntityReferences(false);
        documentBuilderFactory.setValidating(false);
        documentBuilderFactory.setNamespaceAware(true);

        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
        builder.setEntityResolver(new EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException {
                // if (systemId.equals("ALLOWED_DTD.dtd")) {
                // }
                return null;
            }
        });
        InputSource inputSource=new InputSource(source);
        Document document = builder.parse(inputSource);
        return document;
    }

    public static String removeSpacesXml(String xml){
        String retXml="";
        while (xml.indexOf("<")>=0){
            int startIndex=xml.indexOf("<");
            int endIndex=xml.indexOf(">");
            retXml=retXml+xml.substring(startIndex,endIndex+1);
            xml=xml.substring(endIndex+1);
            if (xml.indexOf("<")>=0){
                retXml=retXml+xml.substring(0,xml.indexOf("<")).trim();
            }
        }
        return retXml;
    }
    
    /**
     * Return a Cap object with the value set from the passed node.
     * 
     * @param objectClass - Class of the Cap object (e.g. StringCap)
     * @param node - Document node containing the value associated with 
     * 				the property who's name is the node name.
     * @return - Cap object with value.
     */
	private Object getCapValue(Class<?> objectClass, String value) {
		
		Object capObject = null;
		
		switch(capList.indexOf(objectClass)) {
		
		case	2:	capObject = new BooleanCap();
					((BooleanCap)capObject).setValue(new Boolean(value));
					break;
		
		case	3:	capObject = new ConfigFileDataCap();
					((ConfigFileDataCap)capObject).setValue(value);
					break;
				
		case	6:	capObject = new FloatCap();
					((FloatCap)capObject).setValue(new Float(value));
					break;
					
		case	7:	capObject = new HexBinaryCap();
					((HexBinaryCap)capObject).setValue(value.getBytes());
					break;
					
		case	8:	capObject = new IdCap();
					((IdCap)capObject).setValue(value);
					break;
					
		case	10:	capObject = new IntegerCap();
					((IntegerCap)capObject).setValue(new BigInteger(value));
					break;
					
		case	11:	capObject = new MACCap();
					((MACCap)capObject).setValue(value);
					break;
					
		case	13:	capObject = new PercentageCap();
					((PercentageCap)capObject).setValue(new Integer(value));
					break;
					
		case	17:	capObject = new StringCap();
					((StringCap)capObject).setValue(value);
					break;
					
		case	19:	capObject = new VectorCap();
					((VectorCap)capObject).setValue(new Integer(value));
					break;
		
		}
		
		if (capObject != null) {
			return capObject;
		}
		
		if (objectClass.isAssignableFrom(StringCap.class)) {
			capObject = new StringCap();
			((StringCap)capObject).setValue(value);
			return capObject;
		}
		
		return capObject;
		
	}
	
    /**
     * Return a primitive wrapper object with the value set from the passed node.
     * 
     * @param objectClass - Class of the primitive or object (e.g. )
     * @param node - Document node containing the value associated with 
     * 				the property who's name is the node name.
     * @return - Cap object with value.
     */
	private Object getPrimitiveValue(Class<?> objectClass, String value) {
		
		Object primitiveObject = null;
		
		switch(primitiveList.indexOf(objectClass)) {
		
		case	0:
		case	1:	primitiveObject = new Boolean(value);
					break;
					
		case	2:
		case	3:	primitiveObject = new Character(value.charAt(0));
					break;
					
		case	4:
		case	5:	primitiveObject = new Byte(value.getBytes()[0]);
					break;
					
		case	6:
		case	7:	primitiveObject = new Short(value);
					break;
					
		case	8:
		case	9:	primitiveObject = new Integer(value);
					break;
					
		case	10:
		case	11:	primitiveObject = new Long(value);
					break;
					
		case	12:
		case	13:	primitiveObject = new Float(value);
					break;
					
		case	14:
		case	15:	primitiveObject = new Double(value);
					break;
					
		case	16:	primitiveObject = new String(value);
					break;
					
		case   17: primitiveObject = new BigInteger(value);
		            break;

		}
		
		return primitiveObject;
		
	}
	
	private Enum getEnumValue(Class<?> objectClass, String value) {
				
		if (objectClass.equals(AudioCodec.class)) {
			return AudioCodec.fromValue(value);
		}
		if (objectClass.equals(ImageType.class)) {
			return ImageType.fromValue(value);
		}
		if (objectClass.equals(AccessRightsType.class)) {
			return AccessRightsType.fromValue(value);
		}
		if (objectClass.equals(AddressingFormat.class)) {
			return AddressingFormat.fromValue(value);
		}
		if (objectClass.equals(EventType.class)) {
			return EventType.fromValue(value);
		}
		if (objectClass.equals(OnOffCap.class)) {
			return OnOffCap.fromValue(value);
		}
		if (objectClass.equals(Permission.class)) {
			return Permission.fromValue(value);
		}
		if (objectClass.equals(Protocol.class)) {
			return Protocol.fromValue(value);
		}
		if (objectClass.equals(Severity.class)) {
			return Severity.fromValue(value);
		}
		if (objectClass.equals(UploadType.class)) {
			return UploadType.fromValue(value);
		}
		if (objectClass.equals(UploadType.class)) {
			return UploadType.fromValue(value);
		}
		
		return null;
	}
    
	/**
	 * If the Class is not in the same package as ResponseStatus
     * then it isn't ours.
     * 
	 * @param objectClass - Class
	 * @return - True, the class is an open home data object.
	 */
    private boolean isOurs(Class<?> objectClass) {
    	
    	if (objectClass == null) return false;
    	
    	try {
    		if (objectClass.getName().indexOf(openhomeDataPackage) == 0) return true;
    	} catch (Exception e) {
    		e.printStackTrace();
    		return false;
    	}
    	
    	return false;
    	
    }
    
    /**
     * Use the node name to locate a getter method in the
     * current class. The return type of the getter will
     * be used as the class of this node. If there is no
     * current class (this is the top node) it must be
     * the name of one of the open home data objects
     * 
     * @param node - Document Node
     * @return Class<?>
     */
    private Class<?> getClass(Node node) {
    	
        String objectClassName = capitalize(node.getNodeName());
        
        if (currentClass != null) {
        	return getterClass(objectClassName);
        }

        Class<?> objectClass = null;
		String responseClass = ResponseStatus.class.getName();
		int index = responseClass.indexOf(ResponseStatus.class.getSimpleName());
		String responsePackageName = responseClass.substring(0, index);
	    	
		try {
			objectClass = Class.forName(responsePackageName + objectClassName);
		} catch (ClassNotFoundException e) {
			try {
				objectClass = Class.forName(responsePackageName
						+ capitalize(node.getParentNode().getNodeName()) + "$"
						+ objectClassName);
			} catch (ClassNotFoundException ex) {
				return null;
			}
		}
    	setCurrentClass(objectClass);
    	return objectClass;
    }
    
    /**
     * If there is a getter for this property in the current class,
     * return the return Type for that getter.
     * 
     * @param node - Document Node
     * @return Class<?>
     */
    private Class<?> getterClass(String className) {
    	    	
    	if (currentClass == null) return null;
    	
    	Class<?> objectClass = null;
		Method getter = getterForName(className);
		
		if (getter == null) return null;
		
		objectClass = getter.getReturnType();
		
		if (isOurs(objectClass)) {
			return objectClass;
		}

		/*
		 * If this is a List, pass back its generic type.
		 */
		if (List.class.isAssignableFrom(objectClass)) {
			Type type = getter.getGenericReturnType();
			int typeStartIndex = type.toString().indexOf("<");
			int typeEndIndex = type.toString().indexOf(">");
			String genericClassName = type.toString().substring(typeStartIndex+1, typeEndIndex);
			try {
				objectClass = Class.forName(genericClassName);
			} catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		return objectClass;
		
    }
    
    /**
     * Return the getter method for the passed property name in the
     * current class.
     * 
     */
    private Method getterForName(String propertyName) {
    	    	
		try {
			return currentClass.getDeclaredMethod("get" + capitalize(propertyName));
		} catch (NoSuchMethodException e) {
			return null;
		}
    }
    
    /**
     * Return the setter method for the passed property name in the
     * current class.
     * 
     */
    private Method setterForName(String propertyName, Class<?> parameterClass) {
    	    	
		try {
			return currentClass.getDeclaredMethod("set" + capitalize(propertyName), parameterClass);
		} catch (NoSuchMethodException e) {
			return null;
		}
    }
    
    /**
     * Set the currentClass from the passed parameter if it 
     * is one of the openhome objects.
     * 
     */
    private void setCurrentClass(Class<?> objectClass) {
    	if (!isOurs(objectClass)) return;
    	currentClass = objectClass;
    }
    
}
