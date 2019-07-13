/**
 * Copyright (c) 2011-2014 SC Abacus, Inc
 * The MIT License (MIT)
 */
package io.roxa.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

/**
 * @author Steven Chen
 *
 */
public class XmlSimpleReader {

	private XMLStreamReader reader;
	private Map<String, String> internalMap;
	private String elName;
	private StringBuilder elValue;

	/**
	 *
	 */
	public XmlSimpleReader(Reader sourceReader) {
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			reader = factory.createXMLStreamReader(sourceReader);
			internalMap = new HashMap<String, String>();
		} catch (FactoryConfigurationError e) {
			throw new RuntimeException(e);
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 *
	 */
	public XmlSimpleReader(InputStream in) {
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			reader = factory.createXMLStreamReader(in);
			internalMap = new HashMap<String, String>();
		} catch (FactoryConfigurationError e) {
			throw new RuntimeException(e);
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 *
	 */
	public XmlSimpleReader(String xmlContent) {
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			reader = factory.createXMLStreamReader(new ByteArrayInputStream(xmlContent.getBytes()), "UTF-8");
			internalMap = new HashMap<String, String>();
		} catch (FactoryConfigurationError e) {
			throw new RuntimeException(e);
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
	}

	public Map<String, String> asMap() {
		Map<String, String> rs = new HashMap<>();
		rs.putAll(internalMap);
		return rs;
	}

	public String getString(Enum<?> e) {
		return getString(e.name());
	}

	public String getString(String key) {
		return internalMap.get(key);
	}

	public Double getDouble(Enum<?> e) {
		return getDouble(e.name());
	}

	public Double getDouble(String key) {
		try {
			String val = internalMap.get(key);
			if (val == null)
				return null;
			return new Double(val);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public Integer getInteger(Enum<?> e) {
		return getInteger(e.name());
	}

	public Integer getInteger(String key) {
		try {
			String val = internalMap.get(key);
			if (val == null)
				return null;
			return new Integer(val);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public Long getLong(Enum<?> e) {
		return getLong(e.name());
	}

	public Long getLong(String key) {
		try {
			String val = internalMap.get(key);
			if (val == null)
				return null;
			return new Long(val);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public Date getDate(Enum<?> e) {
		return getDate(e.name());
	}

	public Date getDate(String key) {
		try {
			String val = internalMap.get(key);
			if (val == null)
				return null;
			return new Date(Long.parseLong(val));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public XmlSimpleReader unmarshal() {
		try {
			while (reader.hasNext()) {
				int eventType = reader.next();
				processEventType(eventType);
			}
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		} finally {
			if (null != reader) {
				try {
					reader.close();
				} catch (XMLStreamException e) {

				}
			}
		}
		return this;
	}

	private void processEventType(int eventType) {
		switch (eventType) {
		case XMLEvent.START_ELEMENT:
			elName = reader.getLocalName();
			elValue = new StringBuilder();
			break;
		case XMLEvent.END_ELEMENT:
			populateInternalMap();
			break;
		case XMLEvent.CHARACTERS:
			if (elValue != null)
				elValue.append(reader.getText());
			break;
		case XMLEvent.CDATA:
			if (elValue != null)
				elValue.append(reader.getText());
			break;
		case XMLEvent.END_DOCUMENT:
			break;
		default:
			throw new RuntimeException("Uexpeted event type:" + eventType);
		}
	}

	private void populateInternalMap() {
		String name = trim(elName);
		String value = trim(elValue == null ? null : elValue.toString());
		if (name != null && value != null) {
			String _value = internalMap.get(name);
			if (_value == null) {
				internalMap.put(name, value);
			} else {
				StringBuilder sb = new StringBuilder(_value);
				sb.append(",").append(value);
				internalMap.put(name, sb.toString());
			}

		}
		elName = null;
		elValue = null;
	}

	private static String trim(String original) {
		if (original == null)
			return null;
		String value = original.trim();
		if ("".equals(value))
			return null;
		return value;
	}

}
