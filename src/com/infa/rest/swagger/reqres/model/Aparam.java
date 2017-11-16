package com.infa.rest.swagger.reqres.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "user", "pass", "consumerKey", "cSec", "tk", "tkSec" })
public class Aparam implements Serializable{

	private static final long serialVersionUID = -8480020979866962546L;
	@JsonProperty("user")
	private String user;
	@JsonProperty("pass")
	private String pass;
	@JsonProperty("consumerKey")
	private String consumerKey;
	@JsonProperty("cSec")
	private String consumerSecret;
	@JsonProperty("tk")
	private String token;
	@JsonProperty("tkSec")
	private String tokenSecret;

	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	/**
	 *
	 * @return The user
	 */
	@JsonProperty("user")
	public String getUser() {
		return user;
	}

	/**
	 *
	 * @param user
	 *            The user
	 */
	@JsonProperty("user")
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 *
	 * @return The pass
	 */
	@JsonProperty("pass")
	public String getPass() {
		return pass;
	}

	/**
	 *
	 * @param pass
	 *            The pass
	 */
	@JsonProperty("pass")
	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getConsumerSecret() {
		return consumerSecret;
	}

	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getTokenSecret() {
		return tokenSecret;
	}

	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}
