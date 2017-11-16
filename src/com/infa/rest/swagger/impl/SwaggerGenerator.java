package com.infa.rest.swagger.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class SwaggerGenerator {

	public static Map<String, Object>getInfo(RestEndPoint restEndPoint){
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		LinkedHashMap<String, Object> infoMap = new LinkedHashMap<String, Object>();

		infoMap.put(RestSamplingConsts.DESCRIPTION, null);
		infoMap.put("version", "1.0.0");
		infoMap.put("title", null);
		infoMap.put("termsOfService", null);
		infoMap.put("contact", null);
		infoMap.put("license", null);


		map.put("swagger", "2.0");
		map.put("info", infoMap);

		String urlElements[] = restEndPoint.getBaseUrl().split("://", 2);
		String protocol = urlElements[0];
		urlElements = urlElements[1].split("/", 2);
		String baseUrl = urlElements[0];
		String basePath = "/";
		if (urlElements.length > 1){
			basePath += urlElements[1];
			if(basePath.endsWith("/"))
				basePath=basePath.substring(0, basePath.length()-1);
		}

		map.put("host", baseUrl);
		map.put("basePath", basePath);

		List<String> schemes = new ArrayList<>();
		schemes.add(protocol);
		map.put(RestSamplingConsts.PROTOCOLS, schemes);
		return map;

	}

	public String generateSwagger(Api api) throws Exception {
		if (api.getResponse() == null) {
			return null;

		}

		RestEndPoint restEndPoint = api.getEndPoint();
		JSONSampleToResponse jsonToResponse = new JSONSampleToResponse();

		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		LinkedHashMap<String, Object> definitionsAll = new LinkedHashMap<String, Object>();
		LinkedHashMap<String, Object> requestDefinitions = new LinkedHashMap<String, Object>();
		LinkedHashMap<String, Object> responseDefinitions = new LinkedHashMap<String, Object>();

		map.putAll(getInfo(restEndPoint));

		LinkedHashMap<String, Object> mapOfPaths = new LinkedHashMap<String, Object>();

		LinkedHashMap<String, Object> mapOfApi = new LinkedHashMap<String, Object>();

		// mapOfApi.put("get", api.getVerb());
		LinkedHashMap<String, Object> mapOfVerb = new LinkedHashMap<String, Object>();
		ArrayList<Object> tags = new ArrayList<>();
		tags.add(api.getName());
		mapOfVerb.put("tags", tags);
		mapOfVerb.put("summary", null);
		mapOfVerb.put(RestSamplingConsts.DESCRIPTION, null);
		mapOfVerb.put("operationId", api.getName());

		if (EContentType.NONE != api.getAccept()) {
			ArrayList<Object> producesList = new ArrayList<>();
			producesList.add(api.getAccept().toString());
			mapOfVerb.put("produces", producesList);
		}
		if (EContentType.NONE != api.getRequestContentType()) {
			ArrayList<Object> consumesList = new ArrayList<>();
			consumesList.add(api.getRequestContentType().toString());
			mapOfVerb.put("consumes", consumesList);
		}

		List<Object> listOfParameters = new ArrayList<>();
		List<Parameter> parameters = api.getParameters();
		Iterator<Parameter> parametersIterator = parameters.iterator();
		while (parametersIterator.hasNext()) {
			LinkedHashMap<String, Object> mapOfParameter = new LinkedHashMap<String, Object>();
			Parameter parameter = parametersIterator.next();

			if (parameter.getType() != null && parameter.getType() != EParameterType.Body) {
				mapOfParameter.put(RestSamplingConsts.NAME, parameter.getName());
				mapOfParameter.put(RestSamplingConsts.IN, parameter.getType().toString());
				mapOfParameter.put(RestSamplingConsts.DESCRIPTION, null);
				mapOfParameter.put(RestSamplingConsts.REQUIRED , parameter.isMandatory());
				mapOfParameter.put(RestSamplingConsts.TYPE, EParameterDataType.inferType(parameter.getValue()).toString());
				EParameterDataType format=EParameterDataType.inferFormatType(parameter.getValue());
				if (EParameterDataType.NULL != format) {
					mapOfParameter.put(RestSamplingConsts.FORMAT, format.toString());
				}
				listOfParameters.add(mapOfParameter);
			} else {//body parameter
				mapOfParameter.put(RestSamplingConsts.NAME, parameter.getName());
				mapOfParameter.put(RestSamplingConsts.IN, parameter.getType().toString());
				mapOfParameter.put(RestSamplingConsts.DESCRIPTION, null);
				mapOfParameter.put(RestSamplingConsts.REQUIRED , parameter.isMandatory());

//				JSONSampleToParameter jsonToParameter = new JSONSampleToParameter();
				jsonToResponse.generateSchemaFromParameter(api, parameter);

				if (jsonToResponse.getDefinitions()!= null && jsonToResponse.getDefinitions().size() >= 1) {
					LinkedHashMap<String, Object> mapOfBodyparam = new LinkedHashMap<String, Object>();
	
					if(parameter.getValue()!=null && parameter.getValue().toString().trim().startsWith("[")){		//Array Body Param
						LinkedHashMap<String, Object> arrayEle = new LinkedHashMap<String, Object>();
						arrayEle.put(RestSamplingConsts.REF, RestSamplingConsts.DEFINITIONS_HASH
								+ api.getName() + RestSamplingConsts.REQUEST + "##" + parameter.getName());
						mapOfBodyparam.put(RestSamplingConsts.TYPE, "array");
						mapOfBodyparam.put(RestSamplingConsts.ITEMS, arrayEle);
					}
					else{
						mapOfBodyparam.put(RestSamplingConsts.REF, RestSamplingConsts.DEFINITIONS_HASH
								+ api.getName() + RestSamplingConsts.REQUEST + "##" + parameter.getName());
					}
		
					mapOfParameter.put(RestSamplingConsts.SCHEMA, mapOfBodyparam);
					listOfParameters.add(mapOfParameter);
					requestDefinitions.putAll(jsonToResponse.getDefinitions());
				}
			}

		}

		jsonToResponse = new JSONSampleToResponse();
		List<Object> responseList = jsonToResponse.generateSchemaFromString(api.getResponse(), api.getName());
		responseDefinitions.putAll(jsonToResponse.getDefinitions());
		LinkedHashMap<String, Object> mapOfResponse = new LinkedHashMap<String, Object>();
		if (responseList != null) {
			LinkedHashMap<String, Object> mapOfResponseInner = new LinkedHashMap<String, Object>();
			mapOfResponseInner.put(RestSamplingConsts.DESCRIPTION, "successful operation");
			if (responseDefinitions.isEmpty() && responseList !=null && responseList.isEmpty()) {
//				mapOfResponse.put("200", mapOfResponseInner);
			} else {
				Iterator<Object> responseListIterator = responseList.iterator();
				if (responseListIterator.hasNext()) {
					mapOfResponse.put(api.getStatusCode().toString(), responseListIterator.next());
				} else {
					LinkedHashMap<String, Object> schemaMap = new LinkedHashMap<String, Object>();
					schemaMap.put(RestSamplingConsts.REF, RestSamplingConsts.DEFINITIONS_HASH+ api.getName());
					mapOfResponseInner.put(RestSamplingConsts.SCHEMA, schemaMap);
					mapOfResponse.put(api.getStatusCode().toString(), mapOfResponseInner);
				}
			}

		}

		responseDefinitions.putAll(requestDefinitions);
		definitionsAll.put(RestSamplingConsts.DEFINITIONS, responseDefinitions);
		mapOfVerb.put(RestSamplingConsts.PARAMETERS, listOfParameters);
		mapOfVerb.put(RestSamplingConsts.RESPONSES, mapOfResponse);

		if (api.getVerb() != null && api.getPath() != null) {
			mapOfApi.put(api.getVerb().toString().toLowerCase(), mapOfVerb);
			StringBuffer pathName=new StringBuffer(api.getPath());
			char firstChar=pathName.charAt(0);
			if('/' != firstChar)
				pathName.insert(0, '/');
			mapOfPaths.put(pathName.toString(), mapOfApi);
		}
		map.put(RestSamplingConsts.PATHS, mapOfPaths);
		ObjectMapper mapper;
		mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		map.putAll(definitionsAll);
		String swagger = mapper.writeValueAsString(map);
		return swagger;
	}
}

