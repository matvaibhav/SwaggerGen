package com.infa.rest.swagger.serv;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.infa.rest.swagger.impl.Api;
import com.infa.rest.swagger.impl.EConnectionAttributes;
import com.infa.rest.swagger.impl.EContentType;
import com.infa.rest.swagger.impl.EParameterType;
import com.infa.rest.swagger.impl.EVerb;
import com.infa.rest.swagger.impl.Parameter;
import com.infa.rest.swagger.impl.RestEndPoint;
import com.infa.rest.swagger.impl.SwaggerGenerator;
import com.infa.rest.swagger.reqres.model.API;
import com.infa.rest.swagger.reqres.model.Aparam;
import com.informatica.sdk.helper.auth.AuthenticationType;
import com.informatica.sdk.helper.common.ApiException;
import com.informatica.sdk.helper.common.IRequest;
import com.informatica.sdk.helper.common.IResponse;
import com.informatica.sdk.helper.common.JsonUtil;
import com.informatica.sdk.helper.common.XMLDataProvider;

public class RequestBuilder {
	private API api = null;
	private Boolean isDebug = GlobalConfig.getInstance().isDebug();
	private String responseFileName = null;

	private String getUserResponse() {
		/*String contents=null;
		if (hasUserResponse()) {
			try {
				contents= new String(Files.readAllBytes(Paths.get(responseFileName)),"UTF-8");
			} catch (IOException e) {
				//Should not occur
				e.printStackTrace();
			}
		}*/
		return api.getUserResponse();
	}

	private boolean hasUserResponse() {
		return (getUserResponse() != null && !getUserResponse().isEmpty()) ? true : false;
	}

	public RequestBuilder(String requestModel) throws ApiException {
		api = (API) JsonUtil.deserialize(requestModel, null, API.class);
	}

/*	public RequestBuilder setUserResponse(String responseFile) {
		responseFileName = responseFile;
		return this;
	}*/

	public RestEndPoint build() throws ApiException {
		RestEndPoint ep = new RestEndPoint();
		String procedureName = api.getOperationId();
//		String URL = getProtocol() + api.getApiHost();
		String URL = api.getEndPath();
		String baseURL = api.getBasePath();
		String epPathInfo = api.getApiPath();
		EVerb epMethod = EVerb.getVerb(api.getMethod());
		String accept = api.getAccept();
		String content = api.getContentType();
		String bodyParam = api.getBody();

		ep.setBaseUrl(URL + baseURL);

		Api api = new Api(ep);
		api.setName(procedureName);
		api.setPath(epPathInfo);
		api.setVerb(epMethod);
		api.setAccept(accept);

		// Add Query Params
		List<Parameter> epParameters = getQueryParams();

		// Add Body Params
		if (epMethod != EVerb.GET) {
			api.setRequestContentType(content);
			if (bodyParam != null) {
				// Validate Request JSON
				if ("application/json".equalsIgnoreCase(content)
						&& Util.isValidJson(bodyParam))
					;
				epParameters.add(new Parameter(EParameterType.Body, "body",
						bodyParam));
			}
		}
		epParameters.addAll(getHeaderParams());
		epParameters.addAll(getPathParams());
		api.getParameters().addAll(epParameters);
		api.setAuthType(getAuthType());
		api.setAuthAttributes(getAuthParams());
		api = fetchResponse(api);
		ep.mergeApi(api);
		return ep;
	}

	public API getApi() {
		return api;
	}

	private String getProtocol() {
		String protocol = "http://";
		int idx = api.getEndPath().indexOf("://");

		if (idx > 0) {
			protocol = api.getEndPath().substring(0, idx + 3);
		}
		return protocol;
	}

	private List<Parameter> getPathParams() {
		List<Parameter> parameters = new ArrayList<Parameter>();
		String s = api.getApiPath();
		Pattern p = Pattern.compile("\\{(.*?)\\}");
		Matcher m = p.matcher(s);

		while (m.find()) {
			String text = m.group(1);
			try {
				Parameter param = new Parameter(EParameterType.Path,
						URLDecoder.decode(text, "UTF-8"), text);
				parameters.add(param);
			} catch (UnsupportedEncodingException e) {
				// e.printStackTrace();
			}
		}

		return parameters;
	}

	private List<Parameter> getQueryParams() {
		List<Parameter> parameters = new ArrayList<Parameter>();
		for (Entry<String, Object> entry : api.getQuery()
				.getQueryPropertiesMap().entrySet()) {
			Parameter param = new Parameter(EParameterType.Query,
					entry.getKey(), entry.getValue());
			if (param.getValue() == null
					|| param.getValue().toString().isEmpty())
				param.setMandatory(false);
			parameters.add(param);

		}
		return parameters;
	}

	private List<Parameter> getHeaderParams() {
		List<Parameter> parameters = new ArrayList<Parameter>();
		for (Entry<String, Object> entry : api.getHeaders().getHeaderMap()
				.entrySet()) {
			Parameter param = new Parameter(EParameterType.Header,
					entry.getKey(), entry.getValue());

			if (param.getValue() == null
					|| param.getValue().toString().isEmpty())
				param.setMandatory(false);
			parameters.add(param);

		}
		return parameters;
	}

	private AuthenticationType getAuthType() {

		switch (api.getAuth().toLowerCase()) {
		case "basic":
			return AuthenticationType.BASIC;
		case "digest":
			return AuthenticationType.DIGEST;
		case "oauth":
			return AuthenticationType.OAUTH;
		}

		return AuthenticationType.NONE;
	}

	private Map<EConnectionAttributes, String> getAuthParams() {
		Map<EConnectionAttributes, String> map = new HashMap<EConnectionAttributes, String>();
		Aparam authParams = api.getAparam();
		switch (getAuthType()) {
		case BASIC:
		case DIGEST:
			map.put(EConnectionAttributes.userName, authParams.getUser());
			map.put(EConnectionAttributes.password, authParams.getPass());
			break;
		case OAUTH:
			map.put(EConnectionAttributes.consumerKey,
					authParams.getConsumerKey());
			map.put(EConnectionAttributes.consumerSecret,
					authParams.getConsumerSecret());
			map.put(EConnectionAttributes.token, authParams.getToken());
			map.put(EConnectionAttributes.tokenSecret,
					authParams.getTokenSecret());
		default:
			break;
		}
		return map;
	}

	private Api fetchResponse(Api api) {
		StringBuilder request = new StringBuilder();
		String resposeString = "", url = "";
		int status = 200;
		try {
			IRequest req = com.informatica.sdk.helper.common.RequestBuilder
					.newInstance();

			url = api.getEndPoint().getBaseUrl();
			String[] urlElements = url.split("://", 2);
			req.setProtocol(urlElements[0]);
			req.setHost(urlElements[1]);
			req.setPath(api.getPath());
			req.setVerb(api.getVerb().name());

			request.append("host:").append(url);
			request.append("\nmethod:").append(api.getVerb());
			request.append("\npath:").append(api.getPath());

			// Add Header parameters content-type and accept.
			if (EContentType.NONE != api.getRequestContentType()) {
				req.setRequestContentType(api.getRequestContentType()
						.toString());
				request.append("\nContent-Type:").append(
						api.getRequestContentType().toString());
			}
			if (EContentType.NONE != api.getAccept()) {
				req.setResponseContentType(api.getAccept().toString());
				request.append("\nAccept:").append(api.getAccept().toString());
			}
			request.append("\naccept-encoding:gzip, deflate");//$NON-NLS-1$
			// Add Path, query & other params.

			StringBuilder bodyParam = new StringBuilder();
			StringBuilder queryParam = new StringBuilder();
			StringBuilder headerParam = new StringBuilder();

			if (api.getAuthType() != AuthenticationType.NONE)
				req.addAuthentication(api.getAuthType(), api.getAuthObject());

			request.append("\nAuthentication: " + api.getAuthType().toString());

			for (Parameter param : api.getParameters()) {
				req.addParam(param.getType().name(), param.getName(),
						param.getValue());
				switch (param.getType()) {
				case Body:
					bodyParam.append("\n").append(param.getName()).append(":")
							.append(param.getValue());
					break;
				case Query:
					queryParam.append("\n\t").append(param.getName())
							.append(":").append(param.getValue());
					break;
				case Header:
					headerParam.append("\n\t").append(param.getName())
							.append(":").append(param.getValue());
					break;
				default:
					break;
				}
			}

			if (headerParam.length() > 0)
				request.append(headerParam.insert(0, "\nHeader Parameters:"));//$NON-NLS-1$
			if (queryParam.length() > 0)
				request.append(queryParam.insert(0, "\nQuery Parameters:"));//$NON-NLS-1$
			if (bodyParam.length() > 0)
				request.append(bodyParam.insert(0, "\nRequest Payload:"));//$NON-NLS-1$
			if (hasUserResponse())
				request.append("\n\n\t").append("REST Endpoint API is not executed. Will use user given response.");//$NON-NLS-1$

			api.setRequest(request.toString());
			api.setSwagger("");

			IResponse tpResponse = null;
			Object responseData = null;
			if (hasUserResponse()) {
				responseData = getUserResponse();
			} else {
				tpResponse = req.execute();
				if (null != tpResponse)
					responseData = tpResponse.getRowData(0);
			}
			resposeString = (responseData != null) ? responseData.toString()
					: "{}";
			resposeString = (!resposeString.isEmpty()) ? resposeString : "{}";

			api.setResponse(resposeString);

			if ((api.getAccept() == EContentType.APPLICATION_XML)
					|| (tpResponse != null && tpResponse instanceof XMLDataProvider)) {
				api.setResponse(JsonUtil.XML2JSON(resposeString));
			}

		} catch (ApiException e) {
			status = e.getCode();
			resposeString = e.toString();
			if (isDebug)
				e.printStackTrace();
		} catch (IOException e) {
			status = 600;// TODO: get the error codes from invoker.
			resposeString = e.getMessage();
			if (isDebug)
				e.printStackTrace();
		} catch (Throwable e) {
			status = 600;// TODO: get the error codes from invoker.
			String errorString = e.getMessage();
			if (!resposeString.isEmpty())
				errorString += "\n Response:" + resposeString;
			resposeString = errorString;

			if (isDebug)
				e.printStackTrace();
		}

		if (status != 200)
			api.setErrorTrace(resposeString);
		api.setStatusCode(status);

		return api;
	}

	public static void main(String a[]) throws ApiException {
		try {
			RequestBuilder builder = new RequestBuilder(
					"{\"headers\":{\"h\":\"h\",\"h2\":\"h2\"},\"formParams\":{},\"body\":\"Raw Content\",\"accept\":\"application/json\",\"contentType\":\"application/json\",\"auth\":\"Basic\",\"method\":\"POST\",\"endPoint\":\"\",\"endPath\":\"https://www.googleapis.com/fusiontables/v2/query\",\"aparam\":{\"user\":\"user\",\"pass\":\"pass\"},\"query\":{\"sql\":\"SELECT * FROM 1KxVV0wQXhxhMScSDuqr-0Ebf0YEt4m4xzVplKd4\",\"format\":\"json\"},\"apiHost\":\"www.googleapis.com\",\"basePath\":\"fusiontables\",\"apiPath\":\"v2/query\",\"operationId\":\"GoogAPI\"}");

			RestEndPoint ep = builder.build();

			try {
				String generatedSwagger = "";
				SwaggerGenerator swaggerGenerator = new SwaggerGenerator();
				for (Api api : ep.getApis()) {
					try {
						generatedSwagger = swaggerGenerator
								.generateSwagger(api);
						api.setSwagger(generatedSwagger);
					} catch (Exception e) {
						api.setSwagger(e.getMessage());
					}
				}
			} catch (Exception e) {
				// responseStr =
				// "Not able to authenticate session, Please re-try.";
			}

		} catch (ApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
