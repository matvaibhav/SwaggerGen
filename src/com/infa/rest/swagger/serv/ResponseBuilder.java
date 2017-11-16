package com.infa.rest.swagger.serv;

import com.infa.rest.swagger.impl.Api;
import com.infa.rest.swagger.impl.RestEndPoint;
import com.infa.rest.swagger.impl.SwaggerGenerator;
import com.infa.rest.swagger.reqres.model.Response;
import com.infa.rest.swagger.reqres.model.Status;
import com.informatica.sdk.helper.common.ApiException;
import com.informatica.sdk.helper.common.JsonUtil;

public class ResponseBuilder {
	RestEndPoint ep = null;
	private Boolean isDebug=GlobalConfig.getInstance().isDebug();

	ResponseBuilder(RestEndPoint ep) {
		this.ep = ep;
	}

	public String build() throws ApiException {
		Api api = ep.getApis().get(0);
		Response res = new Response();
		res.setRequest(api.getRequest());
		res.setStatus(new Status(api.getStatusCode().toString(), "", ""));
		res.setResponse(api.getResponse());
		try {
			if (api.getStatusCode() == 200 || api.getStatusCode() == 204) {
				SwaggerGenerator gen = new SwaggerGenerator();
				api.setSwagger(gen.generateSwagger(api));
				res.setSwagger(ep.getSwagger());
			} else{
				res.setResponse(api.getErrorTrace());
				res.setSwagger("Swagger generation not attempted due to non successful or Invalid response.");
			}
		} catch (Exception e) {
			res.setStatus(new Status("600", "", ""));
			res.setSwagger("Swagger generation not attempted due to non successful or Invalid response.\n Reason:"+e.getMessage());
			if(isDebug)
				e.printStackTrace();
		}
		return JsonUtil.serialize(res);
	}

	public static void main(String a[]) throws ApiException {
		String msg = "{\"status\":{\"code\":\"status\",\"message\":\"aa\"},\"req\":\"status\",\"res\":\"status\"}";
		Response res = (Response) JsonUtil.deserialize(msg, null,
				Response.class);
		res.getResponse();

		Response res2 = new Response();
		res2.setRequest("Stt");
		res2.setStatus(new Status("a", "", "b"));
		res2.setResponse("RES");
		res2.setSwagger("{\"status\":{\"code\":\"status\",\"message\":\"aa\"},\"req\":\"status\",\"res\":\"status\"}");

		res2.getResponse();
		JsonUtil.serialize(res2);
	}
}
