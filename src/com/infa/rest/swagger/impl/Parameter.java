package com.infa.rest.swagger.impl;

public class Parameter {
	private EParameterType type;
	private String name;
	private Object value;
	private boolean mandatory = false;
	private boolean encrypted = false;

	public Parameter() {
	}

	public Parameter(EParameterType type, String name, Object value) {
		super();
		this.type = type;
		this.name = name;
		this.value = value;
	}

	public EParameterType getType() {
		return type;
	}

	public void setType(EParameterType type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public boolean isMandatory() {
		return this.mandatory;
	}

	public boolean isEncrypted() {
		return this.encrypted;
	}

	public void setMandatory(boolean flag) {
		this.mandatory = flag;
	}

	public void setEncrypted(boolean flag) {
		this.encrypted = flag;
	}

	public boolean isParameterSame(Parameter p1) {
		if (this.name.equals(p1.getName()) && this.type.equals(p1.getType())) {
			return true;
		} else
			return false;
	}
}
