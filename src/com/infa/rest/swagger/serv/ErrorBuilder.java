package com.infa.rest.swagger.serv;

import com.infa.rest.swagger.reqres.model.Status;
import com.informatica.sdk.helper.common.ApiException;
import com.informatica.sdk.helper.common.JsonUtil;

public class ErrorBuilder {
	String errMsg = null;
	String title = "";

	ErrorBuilder(Exception e) {
		this.errMsg = e.getMessage();
	}

	ErrorBuilder(String e) {
		this.errMsg = e;
	}

	ErrorBuilder(String title, String detailsMsg) {
		this.errMsg = detailsMsg;
	}

	public String build() {
		Status sts = new Status("GEN-100", title, errMsg);
		try {
			return JsonUtil.serialize(sts);
		} catch (ApiException e) {// ignore
		}
		return errMsg;
	}
}
