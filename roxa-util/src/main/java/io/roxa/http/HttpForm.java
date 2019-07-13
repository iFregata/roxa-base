/**
 * The MIT License
 * 
 * Copyright (c) 2016-2018 Shell Technologies PTY LTD
 *
 * You may obtain a copy of the License at
 * 
 *       http://mit-license.org/
 *       
 */
package io.roxa.http;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Steven Chen
 *
 */
public class HttpForm {

	private Map<String, String> fields = new HashMap<>();
	private String[] filePartName;

	public static HttpForm filePartNameForm(String fieldName, File file) {
		HttpForm form = new HttpForm();
		form.filePartName(fieldName, file.getName());
		return form;
	}

	public static HttpForm filePartNameForm(String fieldName, String fileName) {
		HttpForm form = new HttpForm();
		form.filePartName(fieldName, fileName);
		return form;
	}

	/**
	 * 
	 */
	public HttpForm() {
	}

	public HttpForm field(String name, String value) {
		fields.put(name, value);
		return this;
	}

	public HttpForm literal(String literal) {
		Map<String, String> rs = Stream.of(literal).flatMap(groups -> {
			return Arrays.stream(groups.split(";"));
		}).map(item -> {
			return item.split("=");
		}).collect(Collectors.toMap(r -> r[0], r -> r[1]));
		fields.putAll(rs);
		return this;
	}

	public Stream<Entry<String, String>> stream() {
		return fields.entrySet().stream();
	}

	public HttpForm filePartName(String name, String fileName) {
		filePartName = new String[] { name, fileName };
		return this;
	}

	public String[] filePartName() {
		return filePartName;
	}

}
