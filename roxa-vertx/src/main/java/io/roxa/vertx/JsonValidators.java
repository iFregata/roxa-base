/**
 * The MIT License
 * 
 * Copyright (c) 2018-2020 Shell Technologies PTY LTD
 *
 * You may obtain a copy of the License at
 * 
 *       http://mit-license.org/
 *       
 */
package io.roxa.vertx;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

import io.roxa.util.Strings;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author Steven Chen
 *
 */
public class JsonValidators {

	private JsonObject validators;

	/**
	 * Create a validators instance
	 * 
	 * @return
	 */
	public static JsonValidators create() {
		JsonValidators inst = new JsonValidators();
		inst.validators = new JsonObject();
		return inst;
	}

	private JsonValidators() {

	}

	/**
	 * Required validation
	 * 
	 * @param fieldName
	 * @return
	 */
	public JsonValidators required(String fieldName) {
		return required(fieldName, "string");
	}

	public JsonValidators millis(String fieldName) {
		return millis(fieldName, -1);
	}

	public JsonValidators millis(String fieldName, long offset) {
		range(fieldName, 0, 4102452248150L).required(fieldName, "millis").validator(fieldName).put("scheme",
				new JsonObject().put("offset", offset));
		return this;
	}

	public JsonValidators datetimePattern(String fieldName, String pattern) {
		return datetimePattern(fieldName, pattern, -1);
	}

	public JsonValidators datetimePattern(String fieldName, String pattern, int offset) {
		len(fieldName, pattern.length()).required(fieldName, "datetime_chars").validator(fieldName).put("scheme",
				new JsonObject().put("pattern", pattern).put("offset", offset));
		return this;
	}

	public JsonValidators datePattern(String fieldName, String pattern) {
		return datePattern(fieldName, pattern, -1);
	}

	public JsonValidators datePattern(String fieldName, String pattern, int offset) {
		len(fieldName, pattern.length()).required(fieldName, "date_chars").validator(fieldName).put("scheme",
				new JsonObject().put("pattern", pattern).put("offset", offset));
		return this;
	}

	/**
	 * 
	 * @param fieldName
	 * @param min
	 * @param max
	 * @return
	 */
	public JsonValidators range(String fieldName, long min, long max) {
		required(fieldName, "number").validator(fieldName).put("range",
				new JsonObject().put("min", min).put("max", max));
		return this;
	}

	/**
	 * 
	 * @param fieldName
	 * @param min
	 * @param max
	 * @return
	 */
	public JsonValidators range(String fieldName, String min, String max) {
		required(fieldName, "number").validator(fieldName).put("range", new JsonObject()
				.put("min", new BigDecimal(min).doubleValue()).put("max", new BigDecimal(max).doubleValue()));
		return this;
	}

	/**
	 * 
	 * @param fieldName
	 * @param checkValues
	 * @return
	 */
	public JsonValidators check(String fieldName, String... checkValues) {
		required(fieldName).validator(fieldName).put("check", new JsonArray(Arrays.asList(checkValues)));
		return this;
	}

	/**
	 * 
	 * @param fieldName
	 * @param min
	 * @param max
	 * @return
	 */
	public JsonValidators len(String fieldName, int min, int max) {
		required(fieldName).validator(fieldName).put("len", new JsonObject().put("min", min).put("max", max));
		return this;
	}

	/**
	 * 
	 * @param fieldName
	 * @param len
	 * @return
	 */
	public JsonValidators len(String fieldName, int len) {
		required(fieldName).validator(fieldName).put("len", new JsonObject().put("min", len).put("max", len));
		return this;
	}

	/**
	 * 
	 * @param fieldName
	 * @param regExp
	 * @return
	 */
	public JsonValidators pattern(String fieldName, String regExp) {
		required(fieldName).validator(fieldName).put("pattern", regExp);
		return this;
	}

	/**
	 * 
	 * @return
	 */
	public String encodePrettily() {
		return validators.encodePrettily();
	}

	/**
	 * 
	 * @param json
	 * @return
	 */
	public JsonObject validate(JsonObject json) {
		JsonObject validationErrors = new JsonObject();
		if (validators.isEmpty())
			return validationErrors;
		if (json == null || json.isEmpty())
			return validationErrors.put("global", "The json is null or empty!");
		validators.fieldNames().stream().forEach(key -> {
			Object value = json.getValue(key);
			if (value == null) {
				validationErrors.put(key, String.format("Missing field [%s]", key));
			} else {
				JsonObject validator = validators.getJsonObject(key);
				if (validator != null && !validator.isEmpty()) {
					switch (validator.getString("required")) {
					case "string":
						validateStringValue(validationErrors, validator, key, value);
						break;
					case "number":
						validateNumberValue(validationErrors, validator, key, value);
						break;
					case "date_chars":
						validateStringValue(validationErrors, validator, key, value);
						validateDateCharsValue(validationErrors, validator, key, value);
						break;
					case "datetime_chars":
						validateStringValue(validationErrors, validator, key, value);
						validateDatetimeCharsValue(validationErrors, validator, key, value);
						break;
					case "millis":
						validateNumberValue(validationErrors, validator, key, value);
						validateMillisValue(validationErrors, validator, key, value);
						break;
					default:
						break;
					}
				}
			}
		});
		return validationErrors;
	}

	private JsonValidators required(String fieldName, String type) {
		validator(fieldName).put("required", type);
		return this;
	}

	private static JsonObject validateMillisValue(JsonObject validationErrors, JsonObject validator, String key,
			Object value) {
		if (validationErrors.containsKey(key))
			return validationErrors;
		if (!(value instanceof Long))
			return validationErrors.put(key, String.format("The field [%s] must be time millisecond", key));
		Long timeMillis = (Long) value;
		JsonObject rule = validator.getJsonObject("scheme");
		long offset = rule.getLong("offset");
		try {
			Instant instant = Instant.ofEpochMilli(timeMillis);
			if (offset != -1) {
				Instant now = Instant.now();
				if (instant.isBefore(now.minusMillis(offset)) || instant.isAfter(now.plusMillis(offset))) {
					validationErrors.put(key,
							String.format("The [%s] of field [%s] is out of datetime offset [%s] in millisecond",
									timeMillis, key, offset));
				}
			}
		} catch (Throwable e) {
			validationErrors.put(key, String.format("The field [%s] must be a millisecond", key));
		}

		return validationErrors;
	}

	private static JsonObject validateDateCharsValue(JsonObject validationErrors, JsonObject validator, String key,
			Object value) {
		if (validationErrors.containsKey(key))
			return validationErrors;
		String dateChars = (String) value;
		JsonObject rule = validator.getJsonObject("scheme");
		String pattern = rule.getString("pattern");
		int offset = rule.getInteger("offset");
		try {
			LocalDate ld = LocalDate.parse(dateChars, DateTimeFormatter.ofPattern(pattern));
			if (offset != -1) {
				LocalDate today = LocalDate.now();
				if (ld.isBefore(today.minusDays(offset)) || ld.isAfter(today))
					validationErrors.put(key, String.format("The [%s] of field [%s] is out of date offset [%s] in day",
							dateChars, key, offset, dateChars));
			}
		} catch (Throwable e) {
			validationErrors.put(key, String.format("The field [%s] must match pattern [%s]", key, pattern));
		}

		return validationErrors;
	}

	private static JsonObject validateDatetimeCharsValue(JsonObject validationErrors, JsonObject validator, String key,
			Object value) {
		if (validationErrors.containsKey(key))
			return validationErrors;
		String datetimeChars = (String) value;
		JsonObject rule = validator.getJsonObject("scheme");
		String pattern = rule.getString("pattern");
		int offset = rule.getInteger("offset");
		try {
			LocalDateTime ldt = LocalDateTime.parse(datetimeChars, DateTimeFormatter.ofPattern(pattern));
			if (offset != -1) {
				LocalDateTime now = LocalDateTime.now();
				if (ldt.isBefore(now.minusSeconds(offset)) || ldt.isAfter(now.plusSeconds(offset))) {
					validationErrors.put(key,
							String.format("The [%s] of field [%s] is out of datetime offset [%s] in second",
									datetimeChars, key, offset));
				}
			}
		} catch (Throwable e) {
			validationErrors.put(key, String.format("The field [%s] must match pattern [%s]", key, pattern));
		}

		return validationErrors;
	}

	private static JsonObject validateNumberValue(JsonObject validationErrors, JsonObject validator, String key,
			Object value) {
		if (validationErrors.containsKey(key))
			return validationErrors;
		if (!(value instanceof Number))
			return validationErrors.put(key, String.format("The field [%s] value must be a number", key));
		// validate check value
		JsonArray checkRule = validator.getJsonArray("check");
		if (checkRule != null && !checkRule.isEmpty() && !checkRule.contains(value))
			return validationErrors.put(key,
					String.format("The field [%s] value must be one of %s", key, checkRule.encode()));
		// validate length
		JsonObject rangeRule = validator.getJsonObject("range");
		if (rangeRule != null && !rangeRule.isEmpty()) {
			Object _max = rangeRule.getValue("max");
			Object _min = rangeRule.getValue("min");
			BigDecimal max = _max.getClass() == BigDecimal.class ? (BigDecimal) _max
					: new BigDecimal(String.valueOf(_max));
			BigDecimal min = _min.getClass() == BigDecimal.class ? (BigDecimal) _min
					: new BigDecimal(String.valueOf(_min));
			BigDecimal input = new BigDecimal(String.valueOf(value));
			if (input.min(min) == input || input.max(max) == input)
				return validationErrors.put(key,
						String.format("The value of field [%s] must be gt [%s] and lt [%s]", key, min, max));
		}
		return validationErrors;
	}

	private static JsonObject validateStringValue(JsonObject validationErrors, JsonObject validator, String key,
			Object value) {
		if (validationErrors.containsKey(key))
			return validationErrors;
		if (!(value instanceof String))
			return validationErrors.put(key, String.format("The field [%s] value must be a string", key));
		String s = (String) value;
		if (Strings.emptyAsNull(s) == null)
			return validationErrors.put(key, String.format("The field [%s] value must not be empty", key));
		// validate check value
		JsonArray checkRule = validator.getJsonArray("check");
		if (checkRule != null && !checkRule.isEmpty() && !checkRule.contains(value))
			return validationErrors.put(key,
					String.format("The field [%s] value must be one of %s", key, checkRule.encode()));
		// validate length
		JsonObject lenRule = validator.getJsonObject("len");

		if (lenRule != null && !lenRule.isEmpty()) {
			int len = s.length();
			int max = lenRule.getInteger("max");
			int min = lenRule.getInteger("min");
			if (max == min && len != max)
				return validationErrors.put(key, String.format("The length of field [%s] must be [%s]", key, min));
			if (len < min || len > max)
				return validationErrors.put(key,
						String.format("The length of field [%s] must be between [%s] and [%s]", key, min, max));
		}
		// RegExp pattern
		String regExpRule = validator.getString("pattern");
		if (regExpRule != null && !regExpRule.isEmpty()) {
			if (!s.matches(regExpRule))
				return validationErrors.put(key,
						String.format("The length of field [%s] must match pattern [%s]", key, regExpRule));
		}
		return validationErrors;
	}

	private JsonObject validator(String fieldName) {
		return Optional.ofNullable(validators.getJsonObject(fieldName)).orElseGet(() -> {
			JsonObject validator = new JsonObject();
			validators.put(fieldName, validator);
			return validator;
		});
	}
}
