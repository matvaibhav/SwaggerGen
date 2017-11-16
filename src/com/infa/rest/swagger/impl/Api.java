package com.infa.rest.swagger.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.informatica.sdk.helper.auth.Authentication;
import com.informatica.sdk.helper.auth.AuthenticationType;
import com.informatica.sdk.helper.auth.HttpBasicAuth;
import com.informatica.sdk.helper.auth.HttpDigestAuth;
import com.informatica.sdk.helper.auth.OAuth;

public class Api {
	private String name;
	private String path;
	private EVerb verb;
	private String response = "";
	private String errorTrace = "";
	private String url = "";
	private String request = "";
	private Integer statusCode = 200;
	private String swagger;
	private RestEndPoint endPoint;
	private List<Parameter> parameters = new ArrayList<Parameter>();

	private EContentType contentType = EContentType.APPLICATION_JSON;
	private EContentType accept = EContentType.APPLICATION_JSON;

	private int identity;// to validate same object after clone

	private AuthenticationType authType = AuthenticationType.NONE;
	private Map<EConnectionAttributes, String> authAttributes = new HashMap<EConnectionAttributes, String>();

	private EContentType getContentType(String contentType) {
		EContentType cType = EContentType.NONE;
		for (EContentType type : EContentType.values()) {
			if (type.toString().equalsIgnoreCase(contentType)) {
				cType = type;
				break;
			}
		}
		return cType;
	}

	public Api replicate(Api api) {
		Api newAPI = new Api(api.getEndPoint());
		newAPI.identity = api.identity;
		newAPI.name = api.name;
		newAPI.path = api.path;
		newAPI.verb = api.verb;
		newAPI.response = api.response;
		newAPI.url = api.url;
		newAPI.request = api.request;
		newAPI.statusCode = api.statusCode;
		newAPI.swagger = api.swagger;
		newAPI.parameters = api.parameters;

		newAPI.contentType = api.contentType;
		newAPI.accept = api.accept;
		return newAPI;
	}

	public Api(RestEndPoint endPoint) {
		this.endPoint = endPoint;
		identity = IdGenerator.getNext();
	}

	public int getIdentity() {
		return identity;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public EVerb getVerb() {
		return verb;
	}

	public void setVerb(EVerb verb) {
		this.verb = verb;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getErrorTrace() {
		return errorTrace;
	}

	public void setErrorTrace(String errorTrace) {
		this.errorTrace = errorTrace;
	}

	public String getSwagger() {
		return swagger;
	}

	public void setSwagger(String swaggerJson) {
		this.swagger = swaggerJson;
	}

	public String fetchResponse() {
		Api newAPi = new SwaggerParser().fetchResponse(this);
		this.setRequest(newAPi.getRequest());
		this.setUrl(newAPi.getUrl());
		this.setStatusCode(newAPi.getStatusCode());
		this.setSwagger(newAPi.getSwagger());
		return newAPi.getResponse();
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public Integer getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}

	public RestEndPoint getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(RestEndPoint endPoint) {
		this.endPoint = endPoint;
	}

	public EContentType getRequestContentType() {
		return contentType;
	}

	public void setRequestContentType(EContentType contentType) {
		this.contentType = contentType;
	}

	public EContentType getAccept() {
		return accept;
	}

	public void setAccept(EContentType accept) {
		this.accept = accept;
	}

	public void setAccept(String accept) {
		this.accept = getContentType(accept);
	}

	public void setRequestContentType(String contentType) {
		this.contentType = getContentType(contentType);
	}

	public boolean isParameterListContains(Parameter param) {
		for (Parameter p : this.getParameters()) {
			if (p.isParameterSame(param)) {
				return true;
			}
		}
		return false;
	}

	public AuthenticationType getAuthType() {
		return authType;
	}

	public void setAuthType(AuthenticationType authType) {
		this.authType = authType;
	}

	public Map<EConnectionAttributes, String> getAuthAttributes() {
		return authAttributes;
	}

	public void setAuthAttributes(
			Map<EConnectionAttributes, String> authAttributes) {
		this.authAttributes = authAttributes;
	}

	public Authentication getAuthObject() {
		Authentication auth = null;
		Map<EConnectionAttributes, String> attributes = getAuthAttributes();
		switch (authType) {
		case BASIC:
			HttpBasicAuth basicAuth = new HttpBasicAuth();
			basicAuth.setUsername(attributes
					.get(EConnectionAttributes.userName));
			basicAuth.setPassword(attributes
					.get(EConnectionAttributes.password));
			auth = basicAuth;
			break;
		case DIGEST:
			HttpDigestAuth digestAuth = new HttpDigestAuth();
			digestAuth.setUsername(attributes
					.get(EConnectionAttributes.userName));
			digestAuth.setPassword(attributes
					.get(EConnectionAttributes.password));

			auth = digestAuth;
			break;
		case OAUTH:
			OAuth oAuth = new OAuth();
			oAuth.setConsumerKey(attributes
					.get(EConnectionAttributes.consumerKey));
			oAuth.setConsumerSecret(attributes
					.get(EConnectionAttributes.consumerSecret));
			oAuth.setToken(attributes.get(EConnectionAttributes.token));
			oAuth.setTokenSecret(attributes
					.get(EConnectionAttributes.tokenSecret));
			auth = oAuth;
			break;
		default:
			break;
		}

		return auth;
	}
}

enum IdGenerator {
	;
	private static int id = 0;

	public static int getNext() {
		return id++;
	}
}