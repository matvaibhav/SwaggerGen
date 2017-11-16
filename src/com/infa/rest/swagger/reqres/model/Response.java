package com.infa.rest.swagger.reqres.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.infa.rest.swagger.reqres.model.Status;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "status", "req", "res", "swag" })
public class Response implements Serializable{
	private static final long serialVersionUID = 1760864816360482911L;
	@JsonProperty("status")
	private Status status;
	@JsonProperty("req")
	private String request;
	@JsonProperty("res")
	private String response;
	@JsonProperty("swag")
	private String swagger;
	@JsonProperty("code")
	private String code;
	@JsonProperty("message")
	private String message;

	public Status getStatus() {
		if(message!=null && code!=null){
			response=message;
			return new Status(code,"",message);
		}
		else
			return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public String getRequest() {
		return request;
	}
	public void setRequest(String request) {
		this.request = request;
	}
	public String getResponse() {
		if(response!=null)
			return response;
		else if(message!=null)
			return message;
		else
			return status.getMessage();
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public String getSwagger() {
		if(swagger != null)
			return swagger;
		else if( message != null)
			return message;
		else
			return status.getMessage();
	}
	public void setSwagger(String swagger) {
		this.swagger = swagger;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}


}
