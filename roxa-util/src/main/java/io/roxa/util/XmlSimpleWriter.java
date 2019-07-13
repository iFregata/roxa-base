/**
 * Copyright (c) 2011-2014 SC Abacus, Inc
 * The MIT License (MIT)
 */
package io.roxa.util;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Steven Chen
 *
 */
public class XmlSimpleWriter {

	private XMLStreamWriter writer;
	private ByteArrayOutputStream bos;

	/**
	 *
	 */
	public XmlSimpleWriter() {
		try {
			bos = new ByteArrayOutputStream();
			XMLOutputFactory factory = XMLOutputFactory.newFactory();
			writer = factory.createXMLStreamWriter(bos, "UTF-8");
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		} catch (FactoryConfigurationError e) {
			throw new RuntimeException(e);
		}
	}

	public String toString() {
		return bos.toString();
	}

	public XmlSimpleWriter start(Enum<?> enumElement) {
		return start(enumElement.name());
	}

	public XmlSimpleWriter start(String startElement) {
		try {
			writer.writeStartElement(startElement);
			return this;
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
	}

	public XmlSimpleWriter end() {
		try {
			writer.writeEndElement();
			writer.flush();
			return this;
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
	}

	public XmlSimpleWriter close() {
		try {
			writer.close();
			return this;
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
	}

	public XmlSimpleWriter put(Enum<?> key, Integer value) {
		return put(key.name(), value);
	}

	public XmlSimpleWriter put(String key, Integer value) {
		try {
			writer.writeStartElement(key);
			writer.writeCharacters(String.valueOf(value));
			writer.writeEndElement();
			return this;
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
	}

	public XmlSimpleWriter put(Enum<?> key, String value) {
		return put(key.name(), value);
	}

	public XmlSimpleWriter put(String key, String value) {
		try {
			writer.writeStartElement(key);
			writer.writeCData(value == null ? "" : value);
			writer.writeEndElement();
			return this;
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
	}

	public XmlSimpleWriter put(String key, Object value) {
		if (value == null)
			return this;
		if (value instanceof String) {
			put(key, (String) value);
		} else if (value instanceof Integer) {
			put(key, (Integer) value);
		} else if (value instanceof Long) {
			put(key, (Long) value);
		} else if (value instanceof Double) {
			put(key, (Double) value);
		} else if (value instanceof BigDecimal) {
			put(key, ((BigDecimal) value).doubleValue());
		} else {
			throw new RuntimeException("Unsupported type: " + value.getClass());
		}
		return this;
	}

	public XmlSimpleWriter put(Enum<?> key, Double value) {
		return put(key.name(), value);
	}

	public XmlSimpleWriter put(String key, Double value) {
		try {
			writer.writeStartElement(key);
			writer.writeCharacters(String.valueOf(value));
			writer.writeEndElement();
			return this;
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
	}
}
