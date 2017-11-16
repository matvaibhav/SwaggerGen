package com.infa.rest.swagger.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.informatica.sdk.helper.common.ApiException;
import com.informatica.sdk.helper.common.IRequest;
import com.informatica.sdk.helper.common.IResponse;
import com.informatica.sdk.helper.common.JsonUtil;
import com.informatica.sdk.helper.common.ParameterContext;
import com.informatica.sdk.helper.common.RequestBuilder;
import com.informatica.sdk.helper.common.XMLDataProvider;
import com.informatica.sdk.helper.swagger.proxy.DefinitionProxy;
import com.informatica.sdk.helper.swagger.proxy.OperationProxy;
import com.informatica.sdk.helper.swagger.proxy.ParameterProxy;
import com.informatica.sdk.helper.swagger.proxy.PathProxy;
import com.informatica.sdk.helper.swagger.proxy.SwaggerException;
import com.informatica.sdk.helper.swagger.proxy.SwaggerInstance;

public class SwaggerParser implements ISwaggerParser {
	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.informatica.ict.common.rest.ISwaggerReader#parseSwagger(java.lang.
	 * String)
	 */
	@Override
	public RestEndPoint parseSwagger(Class<?> cl, String filePath) throws Throwable {
		RestEndPoint restendpoint = null;
		try {
			SwaggerInstance instance = SwaggerInstance.createSwaggerAsMetadata(this.getClass(), filePath);

			restendpoint = new RestEndPoint();
			restendpoint
					.setBaseUrl(instance.getSupportedProtocols().get(0).toLowerCase() + "://" + instance.getBaseURL());
			List<Api> apis = new ArrayList<>();
			for (PathProxy path : instance.getPaths()) {
				List<OperationProxy> operations = instance.getOperations(path);
				if (null != operations)
					for (OperationProxy operation : operations) {
						Api api = new Api(restendpoint);
						api.setName(operation.getNativeName());
						EVerb verb = getVerb(operation.getVerb().name());
						api.setVerb(verb);
						api.setPath(path.getNativeName());
						List<ParameterProxy> parameters = instance.getParameters(operation);
						Iterator<ParameterProxy> paramIterator = parameters.iterator();
						List<Parameter> parametersICT = new ArrayList<>();
						while (paramIterator.hasNext()) {
							ParameterProxy parameter = paramIterator.next();
							if (ParameterContext.Request == parameter.getContext()) {
								// ignore dummy parameter.
								if ("Request_Port".equalsIgnoreCase(parameter.getNativeName())) //$NON-NLS-1$
									continue;
								Parameter parameterICT = new Parameter();
								parameterICT.setName(parameter.getNativeName());
								EParameterType type = getType(parameter.getIn());
								parameterICT.setType(type);
								parameterICT.setMandatory(parameter.isRequired());
								parametersICT.add(parameterICT);
							}
						}
						api.setSwagger(getSwagger(api, instance, path, operation).replace("\\\"", "\""));
						api.setParameters(parametersICT);

						// FOR 449425, need to enable post EBF build enabled.
						// api.setAccept(operation.getProducerContentType());
						// api.setRequestContentType(operation.getConsumerContentType());
						api.setResponse("");
						apis.add(api);
						restendpoint.mergeApi(api);
					}
			}
		} catch (SwaggerException | IOException e) {
			e.printStackTrace();
			throw e;
		}

		return restendpoint;
	}

	@Override
	public String getSwagger(Api api) {
		SwaggerGenerator sg = new SwaggerGenerator();
		String swagger = null;
		try {
			swagger = sg.generateSwagger(api);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return swagger;
	}

	@Override
	public String getSwagger(RestEndPoint restEndPoint) {
		String swagger = null;
		try {

			LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
			map.putAll(SwaggerGenerator.getInfo(restEndPoint));

			Map<String, Object> paths = new LinkedHashMap<String, Object>();
			map.put(RestSamplingConsts.PATHS, paths);

			Map<String, Object> definitions = new LinkedHashMap<String, Object>();
			map.put(RestSamplingConsts.DEFINITIONS, definitions);

			ObjectMapper objectMapper = new ObjectMapper();

			for (Entry<String, List<Api>> swaggerPaths : restEndPoint.getApiPathMap().entrySet()) {
				String pathName = swaggerPaths.getKey();
				Map<String, Object> pathVerbs = new LinkedHashMap<String, Object>();
				for (Api api : swaggerPaths.getValue()) {
					JsonNode node = objectMapper.readValue(api.getSwagger(), JsonNode.class);
					JsonNode nodePath = node.get(RestSamplingConsts.PATHS);
					if (nodePath != null) {
						Iterator<Entry<String, JsonNode>> pathItr = nodePath.fields();
						while (pathItr != null && pathItr.hasNext()) {
							Entry<String, JsonNode> s = pathItr.next();
							Iterator<Entry<String, JsonNode>> pathVerbItr = s.getValue().fields();
							while (pathVerbItr.hasNext()) {
								pathVerbs.put(api.getVerb().toString(), pathVerbItr.next().getValue());
							}
						}
					}
					Iterator<Entry<String, JsonNode>> defItr = node.get(RestSamplingConsts.DEFINITIONS).fields();
					while (defItr.hasNext()) {
						Entry<String, JsonNode> s = defItr.next();
						definitions.put(s.getKey(), s.getValue());
					}

				}
				paths.put(pathName, pathVerbs);
			}

			objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			swagger = objectMapper.writeValueAsString(map);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return swagger;
	}

	private String getSwagger(Api api, SwaggerInstance instance, PathProxy path, OperationProxy operation)
			throws SwaggerException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();

		RestEndPoint restEndPoint = api.getEndPoint();

		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();

		map.putAll(SwaggerGenerator.getInfo(restEndPoint));

		List<String> schemes = new ArrayList<>();
		schemes.addAll(instance.getSupportedProtocols());
		map.put(RestSamplingConsts.PROTOCOLS, schemes);

		Map<String, Object> paths = new LinkedHashMap<String, Object>();
		JsonNode operationNode = objectMapper.readValue(operation.getObjectJSON().replaceAll("\r\n", ""),
				JsonNode.class);
		Map<String, Object> operationContainer = new LinkedHashMap<String, Object>();
		operationContainer.put(api.getVerb().name().toLowerCase(), operationNode);
		paths.put(path.getNativeName(), operationContainer);
		map.put(RestSamplingConsts.PATHS, paths);

		Map<String, Object> definitions = new LinkedHashMap<String, Object>();

		for (ParameterProxy param : instance.getParameters(operation)) {
			if (param.isComplexParameter()) {
				populateDefinitions(definitions, objectMapper, instance, param.getRefType());
			}
		}

		map.put(RestSamplingConsts.DEFINITIONS, definitions);

		objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		return objectMapper.writeValueAsString(map);

	}

	private void populateDefinitions(Map<String, Object> definitions, ObjectMapper objectMapper,
			SwaggerInstance instance, String refName)
			throws JsonParseException, JsonMappingException, SwaggerException, IOException {
		JsonNode node = null;
		DefinitionProxy defn = instance.getStructureByName(refName);
		if (null == defn)
			throw new SwaggerException("Error Swagger defination for "+ refName+" not found.");
		for (String refname : defn.getReferenceStructureNames()) {
			populateDefinitions(definitions, objectMapper, instance, refname);
		}
		node = objectMapper.readValue(defn.getObjectJSON().replaceAll("\r\n", ""), JsonNode.class);
		definitions.put(refName, node);
	}

	private static EParameterType getType(String in) {
		if (in != null) {
			return EParameterType.getParameterType(in);
		}
		return null;
	}

	private static EVerb getVerb(String operation) {
		return EVerb.getVerb(operation);
	}

	@Override
	public Api fetchResponse(Api api) {
		StringBuilder request = new StringBuilder();
		String resposeString = "", url = "";
		int status = 200;
		try {
			IRequest req = RequestBuilder.newInstance();

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
				req.setRequestContentType(api.getRequestContentType().toString());
				request.append("\nContent-Type:").append(api.getRequestContentType().toString());
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

			for (Parameter param : api.getParameters()) {
				req.addParam(param.getType().name(), param.getName(), param.getValue());
				switch (param.getType()) {
				case Body:
					bodyParam.append("\n").append(param.getName()).append(":").append(param.getValue());
					break;
				case Query:
					queryParam.append("\n\t").append(param.getName()).append(":").append(param.getValue());
					break;
				case Header:
					headerParam.append("\n\t").append(param.getName()).append(":").append(param.getValue());
					break;
				}
			}

			if (headerParam.length() > 0)
				request.append(headerParam.insert(0, "\nHeader Parameters:"));//$NON-NLS-1$
			if (queryParam.length() > 0)
				request.append(queryParam.insert(0, "\nQuery Parameters:"));//$NON-NLS-1$
			if (bodyParam.length() > 0)
				request.append(bodyParam.insert(0, "\nRequest Payload:"));//$NON-NLS-1$

			api.setRequest(request.toString());
			api.setSwagger("");
			IResponse tpResponse = req.execute();
			Object responseData = null;
			if (null != tpResponse)
				responseData = tpResponse.getRowData(0);
			resposeString = (responseData != null) ? responseData.toString() : "{}";
			resposeString = (!resposeString.isEmpty()) ? resposeString : "{}";

			api.setResponse(resposeString);
			if (tpResponse instanceof XMLDataProvider) {
				api.setResponse(JsonUtil.XML2JSON(resposeString));
			}

		} catch (ApiException e) {
			status = e.getCode();
			resposeString = e.toString();
		} catch (IOException e) {
			status = 500;// TODO: get the error codes from invoker.
			resposeString = e.getMessage();
		} catch (Throwable e) {
			status = 500;// TODO: get the error codes from invoker.
			// String errorString =
			// MessageFormat.format(Messages.getString(Messages.ERR_SWAGGER_GEN),
			// e.getMessage());
			String errorString = e.getMessage();
			if (!resposeString.isEmpty())
				errorString += "\n Response:" + resposeString;
			resposeString = errorString;
		}
		api.setUrl(url);
		api.setStatusCode(status);

		return api;
	}
}
