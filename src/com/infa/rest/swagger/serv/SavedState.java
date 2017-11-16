package com.infa.rest.swagger.serv;

import java.io.Serializable;

import com.infa.rest.swagger.reqres.model.API;
import com.infa.rest.swagger.reqres.model.Response;
import com.informatica.sdk.helper.common.ApiException;
import com.informatica.sdk.helper.common.JsonUtil;

public class SavedState implements Serializable{
	private static final long serialVersionUID = 1L;

	String pad = "";
	String sessionId = "";
	API apiRequest = new API();
	Response apiResponse = null;
	String userResponseFileName=null;

	public SavedState(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getPad() {
		return pad;
	}

	public void setPad(String pad) {
		this.pad = pad;
	}

	public void setApiResponse(String apiResponse) throws ApiException {
		this.apiResponse = (Response) JsonUtil.deserialize(apiResponse, null,
				Response.class);
	}

	public Response getApiResponse() {
		return apiResponse;
	}

	public API getApiRequest() {
		return apiRequest;
	}

	public void setApiRequest(API apiRequest) {
		this.apiRequest = apiRequest;
	}

	public void setApiRequest(String apiRequest) throws ApiException {
		this.apiRequest = (API) JsonUtil.deserialize(apiRequest, null, API.class);
	}

	public boolean hasUserResponse() {
		return (userResponseFileName!=null)?true:false;
	}

	public String getUserResponseFileName() {
		return userResponseFileName;
	}

	public void setUserResponseFileName(String userResponseFileName) {
		this.userResponseFileName = userResponseFileName;
	}

}
