package com.infa.rest.swagger.impl;

public enum EContentType {
	APPLICATION_JSON("application/json"),
	APPLICATION_XML("application/xml"),
	FORM_URL_ENCODED("application/x-www-form-urlencoded"), //only for Request/content_Type
	NONE("");
	
	private String displayName;
	EContentType(String displayName){
		this.displayName=displayName;
	}

	@Override
	public String toString(){
		return displayName;
	}
}
