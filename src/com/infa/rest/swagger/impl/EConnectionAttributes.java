package com.infa.rest.swagger.impl;

public enum EConnectionAttributes {
	userName("User Name", true), password("Password", false), consumerKey(
			"Consumer Key", true), consumerSecret("Consumer Secret", true), token(
			"Token", true), tokenSecret("Token Secret", true);

	String displayName;
	boolean required;

	EConnectionAttributes(String displayName, boolean mandatory) {
		this.displayName = displayName;
		this.required = mandatory;
	}
}
