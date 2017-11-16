package com.infa.rest.swagger.impl;

import java.util.ArrayList;
import java.util.List;

public enum EVerb {
	GET("get"), POST("post"), PUT("put"), DELETE("delete");

	private String verb;

	private EVerb(String verb) {
		this.verb = verb;
	}

	public static EVerb getVerb(String verb) {
		EVerb verbType = GET;
		for (EVerb eVerb : EVerb.values()) {
			if (eVerb.toString().equalsIgnoreCase(verb)) {
				verbType = eVerb;
				break;
			}
		}
		return verbType;
	}

	public String toString() {
		return verb;
	}

	public List<EParameterType> getPayloadParamTypes() {
		List<EParameterType> types = new ArrayList<EParameterType>();
		types.add(EParameterType.Header);
		if (GET != this)
			types.add(EParameterType.Body);
		return types;
	}
}
