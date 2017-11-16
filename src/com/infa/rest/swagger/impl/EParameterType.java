package com.infa.rest.swagger.impl;

public enum EParameterType {
	Header("header"), Path("path"), Query("query"), Body("body"), FormData("formData");

	private String parameterType;

	private EParameterType(String parameterType) {
		this.parameterType = parameterType;
	}

	public static EParameterType getParameterType(String parameter) {
		EParameterType paramType = Query;
		for (EParameterType param : EParameterType.values()) {
			if (param.toString().equalsIgnoreCase(parameter)) {
				paramType = param;
				break;
			}
		}
		return paramType;
	}

	public String toString() {
		return this.parameterType;
	}
}
