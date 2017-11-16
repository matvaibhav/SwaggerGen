package com.infa.rest.swagger.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public enum EParameterDataType {

	STRING("string"), NUMBER("number"), BOOLEAN("boolean"), ARRAY("array"), OBJECT("object"), BINARY("binary"), FLOAT(
			"float"), DOUBLE("double"), INT("int32"), LONG("int64"), DATE("date-time"), NULL("null");

	String typeStr;

	private EParameterDataType(String dataTypeStr) {
		typeStr = dataTypeStr;
	}

	public static EParameterDataType inferType(Object value) {
		EParameterDataType type = null;

		if (value == null || value.toString().isEmpty())
			type = EParameterDataType.STRING; // If the sampling data for an
												// object is null, it's data
												// type will be treated as
												// string.
		else if (value instanceof String)
			type = EParameterDataType.STRING;
		else if (value instanceof Number)
			type = EParameterDataType.NUMBER;
		else if (value instanceof Boolean)
			type = EParameterDataType.BOOLEAN;
		else if (value instanceof List) {
			if (((List<?>) value).size() == 0) {
				type = EParameterDataType.STRING;
			} else
				type = EParameterDataType.ARRAY;
		} else if (value instanceof Map) {
			if (((Map<?, ?>) value).values() != null && ((Map<?, ?>) value).values().size() == 0)
				type = EParameterDataType.STRING;
			else
				type = EParameterDataType.OBJECT;
		}
		return type;
	}

	public static EParameterDataType inferFormatType(Object value) {
		EParameterDataType type = EParameterDataType.NULL;

		if (value instanceof Byte)
			type = EParameterDataType.BINARY;
		else if (value instanceof Integer || value instanceof Short)
			type = EParameterDataType.INT;
		else if (value instanceof Long || value instanceof BigInteger )
			type = EParameterDataType.LONG;
		else if (value instanceof Double || value instanceof BigDecimal)
			type = EParameterDataType.DOUBLE;
		else if (value instanceof Float)
			type = EParameterDataType.FLOAT;
		else if (value instanceof Time || value instanceof Date || value instanceof java.util.Date
				|| value instanceof Timestamp)
			type = EParameterDataType.DATE;
		return type;
	}

	public String toString() {
		return typeStr;
	}

}