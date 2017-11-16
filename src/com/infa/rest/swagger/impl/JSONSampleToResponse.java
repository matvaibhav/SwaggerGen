package com.infa.rest.swagger.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.informatica.sdk.helper.common.ApiException;
import com.informatica.sdk.helper.common.JsonUtil;

public class JSONSampleToResponse {
	private Map<String, Object> definitions = new LinkedHashMap<String, Object>();
	private Map<String, Object> jsonSchemaObjectPropertiesCopy = new LinkedHashMap<String, Object>();
	private String apiName = null;
	private ObjectMapper mapper;
	private JsonFactory factory;

	public JSONSampleToResponse() {

		mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

		factory = new JsonFactory();
		factory.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		factory.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

	}

	// If response contains multiple objects, its name will the API name for now
	// as we do not get the actual name of the array of objects in the response.

	@SuppressWarnings("unchecked")
	public void generateSchemaForJsonObject(Map<?, ?> jsonSample, List<Object> jsonSchemaObject, String nameOfResponseMap) {
		if (jsonSample.size() > 1) {
			Map<String, Object> jsonSchemaObjectProperties = new LinkedHashMap<String, Object>();
			generateDefinitions(jsonSample, jsonSchemaObjectProperties, nameOfResponseMap);

		} else {
			Iterator<?> iterSample = jsonSample.entrySet().iterator();
			if (iterSample.hasNext()) {
				Map.Entry<?, ?> entrySample = (Map.Entry<?, ?>) iterSample.next();
				Object jsonSampleInner = entrySample.getValue();
				EParameterDataType dataType = EParameterDataType.inferType(jsonSampleInner);
				EParameterDataType format = EParameterDataType.inferFormatType(jsonSampleInner);
				String name = entrySample.getKey().toString();
				Map<String, Object> mapForObjectTypes = new LinkedHashMap<String, Object>();
				if (dataType == EParameterDataType.OBJECT) {
					Map<String, Object> refMap = new LinkedHashMap<String, Object>();
					refMap.put(RestSamplingConsts.REF, RestSamplingConsts.DEFINITIONS_HASH + apiName + "##" + name);

					mapForObjectTypes.put(RestSamplingConsts.SCHEMA, refMap);
					mapForObjectTypes.put(RestSamplingConsts.DESCRIPTION, null);
					jsonSchemaObject.add(mapForObjectTypes);

					Map<String, Object> jsonSchemaObjectProperties = new LinkedHashMap<String, Object>();
					generateDefinitions(jsonSampleInner, jsonSchemaObjectProperties, name);
				} else if (dataType == EParameterDataType.ARRAY) {
					List<Object> arrayContents = (List<Object>) jsonSampleInner;
					EParameterDataType dataTypeInner = null;
					// If Inner contents of array are primitive data types no
					// Definition will be created, if
					// inner contents are objects or arrays definition is needed
					// to be created.

					if (arrayContents.get(0) != null) {
						dataTypeInner = EParameterDataType.inferType(arrayContents.get(0));
						format = EParameterDataType.inferFormatType(arrayContents.get(0));
						if (dataTypeInner.equals(EParameterDataType.STRING) || dataTypeInner.equals(EParameterDataType.NUMBER)
								|| dataTypeInner.equals(EParameterDataType.BOOLEAN)) {
							Map<String, Object> itemsMap = new LinkedHashMap<String, Object>();
							itemsMap.put(RestSamplingConsts.TYPE, dataTypeInner.toString().toLowerCase());
							if (format != EParameterDataType.NULL)
								itemsMap.put(RestSamplingConsts.FORMAT, format.toString());
							Map<String, Object> schemaMap = new LinkedHashMap<String, Object>();
							schemaMap.put(RestSamplingConsts.TYPE, dataType.toString().toLowerCase());
							schemaMap.put(RestSamplingConsts.ITEMS, itemsMap);
							mapForObjectTypes.put(RestSamplingConsts.DESCRIPTION, null);
							mapForObjectTypes.put(RestSamplingConsts.SCHEMA, schemaMap);
							jsonSchemaObject.add(mapForObjectTypes);
						} else if (dataTypeInner.equals(EParameterDataType.OBJECT) || dataTypeInner.equals(EParameterDataType.ARRAY)) {
							Map<String, Object> itemsMap = new LinkedHashMap<String, Object>();
							itemsMap.put(RestSamplingConsts.REF, RestSamplingConsts.DEFINITIONS_HASH + apiName + "##" + name);

							Map<String, Object> schemaMap = new LinkedHashMap<String, Object>();
							schemaMap.put(RestSamplingConsts.TYPE, dataType.toString().toLowerCase());
							schemaMap.put(RestSamplingConsts.ITEMS, itemsMap);

							mapForObjectTypes.put(RestSamplingConsts.DESCRIPTION, null);
							mapForObjectTypes.put(RestSamplingConsts.SCHEMA, schemaMap);
							jsonSchemaObject.add(mapForObjectTypes);

							Map<String, Object> jsonSchemaObjectProperties = new LinkedHashMap<String, Object>();
							generateDefinitions(jsonSampleInner, jsonSchemaObjectProperties, name);
						}
					}
				} else if (dataType.equals(EParameterDataType.STRING) || dataType.equals(EParameterDataType.NUMBER)
						|| dataType.equals(EParameterDataType.BOOLEAN)) {

					Map<String, Object> refMap = new LinkedHashMap<String, Object>();
					refMap.put(RestSamplingConsts.REF, RestSamplingConsts.DEFINITIONS_HASH + apiName + "##" + name);

					mapForObjectTypes.put(RestSamplingConsts.SCHEMA, refMap);
					mapForObjectTypes.put(RestSamplingConsts.DESCRIPTION, null);
					jsonSchemaObject.add(mapForObjectTypes);

					Map<String, Object> jsonSchemaObjectProperties = new LinkedHashMap<String, Object>();
					generateDefinitions(jsonSampleInner, jsonSchemaObjectProperties, name);

				}
			}
		}
	}

	public void generateSchemaForJsonObject(List<Object> jsonSample, List<Object> jsonSchemaObject, String nameOfResponseArray) {
		if (jsonSample.size() > 0) {
			Object jsonSampleInner = jsonSample.get(0);
			Map<String, Object> mapForObjectTypes = new LinkedHashMap<String, Object>();

			Map<String, Object> itemsMap = new LinkedHashMap<String, Object>();
			itemsMap.put(RestSamplingConsts.REF, RestSamplingConsts.DEFINITIONS_HASH + nameOfResponseArray + "_");
			Map<String, Object> schemaMap = new LinkedHashMap<String, Object>();
			schemaMap.put(RestSamplingConsts.TYPE, EParameterDataType.ARRAY.toString().toLowerCase());
			schemaMap.put(RestSamplingConsts.ITEMS, itemsMap);

			mapForObjectTypes.put(RestSamplingConsts.DESCRIPTION, "successful operation");
			mapForObjectTypes.put(RestSamplingConsts.SCHEMA, schemaMap);
			jsonSchemaObject.add(mapForObjectTypes);

			Map<String, Object> jsonSchemaObjectProperties = new LinkedHashMap<String, Object>();
			generateDefinitions(jsonSampleInner, jsonSchemaObjectProperties, nameOfResponseArray);

		}
	}

	public Map<String, Object> getDefinitions() {
		return definitions;
	}

	public void setDefinitions(Map<String, Object> definitions) {
		this.definitions = definitions;
	}

	@SuppressWarnings("unchecked")
	private void generateDefinitions(Object jsonSample, Map<String, Object> jsonSchemaObjectProperties, String name) {
		EParameterDataType dataType = EParameterDataType.inferType(jsonSample);
		jsonSchemaObjectPropertiesCopy.putAll(jsonSchemaObjectProperties);
		jsonSchemaObjectProperties = new LinkedHashMap<String, Object>();

		if (dataType.equals(EParameterDataType.OBJECT)) {
			Iterator<?> iterSample = ((Map<?, ?>) jsonSample).entrySet().iterator();

			while (iterSample.hasNext()) {
				Map.Entry<?, ?> entrySample = (Map.Entry<?, ?>) iterSample.next();
				Object jsonSampleInner = entrySample.getValue();
				dataType = EParameterDataType.inferType(jsonSampleInner);
				EParameterDataType.inferFormatType(jsonSampleInner);
				String InnerName = entrySample.getKey().toString();
				Map<String, Object> jsonSchemaObjectPropertiesInner = new LinkedHashMap<String, Object>();
				if (dataType.equals(EParameterDataType.STRING) || dataType.equals(EParameterDataType.NUMBER)
						|| dataType.equals(EParameterDataType.BOOLEAN)) {

					jsonSchemaObjectPropertiesInner.put(RestSamplingConsts.TYPE, dataType.toString().toLowerCase());
					EParameterDataType format = EParameterDataType.inferFormatType(jsonSampleInner);
					if (format != EParameterDataType.NULL)
						jsonSchemaObjectPropertiesInner.put(RestSamplingConsts.FORMAT, format.toString());
					jsonSchemaObjectProperties.put(InnerName, jsonSchemaObjectPropertiesInner);
				}

				else if (dataType == EParameterDataType.OBJECT) {
					if (apiName.equals(name))
						generateDefinitions(jsonSampleInner, jsonSchemaObjectProperties, InnerName);
					else
						generateDefinitions(jsonSampleInner, jsonSchemaObjectProperties, name + "##" + InnerName);

					if (!apiName.equals(name))
						jsonSchemaObjectPropertiesInner.put(RestSamplingConsts.REF, RestSamplingConsts.DEFINITIONS_HASH + apiName + "##" + name
								+ "##" + InnerName);
					else
						jsonSchemaObjectPropertiesInner.put(RestSamplingConsts.REF, RestSamplingConsts.DEFINITIONS_HASH + apiName + "##" + InnerName);

					jsonSchemaObjectProperties.put(InnerName, jsonSchemaObjectPropertiesInner);

				} else if (dataType == EParameterDataType.ARRAY) {
					List<Object> arrayInner = (List<Object>) jsonSampleInner;
					Iterator<?> arrayInnerIterator = arrayInner.iterator();
					Object arraySample = null;
					EParameterDataType dataTypeInner = EParameterDataType.STRING;
					while (arrayInnerIterator.hasNext()) {
						arraySample = arrayInnerIterator.next();
						dataTypeInner = EParameterDataType.inferType(arraySample);

						Map<String, Object> itemsMap = new LinkedHashMap<String, Object>();
						if (dataTypeInner.equals(EParameterDataType.STRING) || dataTypeInner.equals(EParameterDataType.NUMBER)
								|| dataTypeInner.equals(EParameterDataType.BOOLEAN)) {
							itemsMap.put(RestSamplingConsts.TYPE, dataTypeInner.toString().toLowerCase());
							EParameterDataType format = EParameterDataType.inferFormatType(arraySample);
							if (format != EParameterDataType.NULL)
								itemsMap.put(RestSamplingConsts.FORMAT, format.toString());
						} else {
							if (!apiName.equals(name))
								itemsMap.put(RestSamplingConsts.REF, RestSamplingConsts.DEFINITIONS_HASH + apiName + "##" + name + "##" + InnerName);
							else
								itemsMap.put(RestSamplingConsts.REF, RestSamplingConsts.DEFINITIONS_HASH + apiName + "##" + InnerName);

						}

						jsonSchemaObjectPropertiesInner.put(RestSamplingConsts.TYPE, EParameterDataType.ARRAY.toString().toLowerCase());
						jsonSchemaObjectPropertiesInner.put(RestSamplingConsts.ITEMS, itemsMap);
						jsonSchemaObjectProperties.put(InnerName, jsonSchemaObjectPropertiesInner);
						if (dataTypeInner.equals(EParameterDataType.ARRAY) || dataTypeInner.equals(EParameterDataType.OBJECT))
							if (apiName.equals(name))
								generateDefinitions(jsonSampleInner, jsonSchemaObjectProperties, InnerName);
							else
								generateDefinitions(jsonSampleInner, jsonSchemaObjectProperties, name + "##" + InnerName);
					}
				}
			}
		} else if (dataType.equals(EParameterDataType.ARRAY)) {
			// Every array must have an object called items
			// LinkedHashMap<String, Object> jsonSchemaObjectItems = new
			// LinkedHashMap<String, Object>(5);
			List<Object> array = (List<Object>) jsonSample;
			// Iterator<?> iterSample = ((Map<?, ?>) jsonSample).entrySet()
			// .iterator();
			Iterator<?> arrayIterator = array.iterator();
			EParameterDataType format = null;
			if (arrayIterator.hasNext()) {
				Object arraySample = arrayIterator.next();
				dataType = EParameterDataType.inferType(arraySample);
				format = EParameterDataType.inferFormatType(arraySample);
			}
			Map<String, Object> jsonSchemaObjectPropertiesInner = new LinkedHashMap<String, Object>();
			Map<String, Object> itemsMap = new LinkedHashMap<String, Object>();
			if (dataType.equals(EParameterDataType.STRING) || dataType.equals(EParameterDataType.NUMBER)
					|| dataType.equals(EParameterDataType.BOOLEAN)) {
				itemsMap.put(RestSamplingConsts.TYPE, dataType.toString().toLowerCase());
				if (format != EParameterDataType.NULL)
					itemsMap.put(RestSamplingConsts.FORMAT, format.toString());
				jsonSchemaObjectPropertiesInner.put(RestSamplingConsts.ITEMS, itemsMap);
				jsonSchemaObjectPropertiesInner.put(RestSamplingConsts.TYPE, EParameterDataType.ARRAY.toString().toLowerCase());
				jsonSchemaObjectProperties.put(name + "_" + "Anonymous array", jsonSchemaObjectPropertiesInner);

			} else {
				arrayIterator = array.iterator();
				while (arrayIterator.hasNext()) {
					LinkedHashMap<?, ?> arraySample = (LinkedHashMap<?, ?>) arrayIterator.next();
					Iterator<?> iterArrarySample = ((Map<?, ?>) arraySample).entrySet().iterator();

					while (iterArrarySample.hasNext()) {

						Map.Entry<?, ?> entrySample = (Map.Entry<?, ?>) iterArrarySample.next();
						Object jsonSampleInner = entrySample.getValue();
						dataType = EParameterDataType.inferType(jsonSampleInner);
						String InnerName = entrySample.getKey().toString();
						jsonSchemaObjectPropertiesInner = new LinkedHashMap<String, Object>();

						if (dataType.equals(EParameterDataType.STRING) || dataType.equals(EParameterDataType.NUMBER)
								|| dataType.equals(EParameterDataType.BOOLEAN)) {

							jsonSchemaObjectPropertiesInner.put(RestSamplingConsts.TYPE, dataType.toString().toLowerCase());
							format = EParameterDataType.inferFormatType(arraySample);
							if (format != EParameterDataType.NULL)
								itemsMap.put(RestSamplingConsts.FORMAT, format.toString());
							jsonSchemaObjectProperties.put(InnerName, jsonSchemaObjectPropertiesInner);
						}

						else if (dataType == EParameterDataType.OBJECT) {
							if (apiName.equals(name))
								generateDefinitions(jsonSampleInner, jsonSchemaObjectProperties, InnerName);
							else
								generateDefinitions(jsonSampleInner, jsonSchemaObjectProperties, name + "##" + InnerName);

							if (!apiName.equals(name))
								jsonSchemaObjectPropertiesInner.put(RestSamplingConsts.REF, RestSamplingConsts.DEFINITIONS_HASH + apiName + "##"
										+ name + "##" + InnerName);
							else
								jsonSchemaObjectPropertiesInner.put(RestSamplingConsts.REF, RestSamplingConsts.DEFINITIONS_HASH + apiName + "##"
										+ InnerName);
							jsonSchemaObjectProperties.put(InnerName, jsonSchemaObjectPropertiesInner);
						} else if (dataType == EParameterDataType.ARRAY) {
							// TODO ADD CHECK FOR DUPLICATE DEFINITIONS
							// TODO UNTESTED CODE
							itemsMap = new LinkedHashMap<String, Object>();
							if (!apiName.equals(name))
								itemsMap.put(RestSamplingConsts.REF, RestSamplingConsts.DEFINITIONS_HASH + apiName + "##" + name + "##" + InnerName);
							else
								itemsMap.put(RestSamplingConsts.REF, RestSamplingConsts.DEFINITIONS_HASH + apiName + "##" + InnerName);

							Map<String, Object> schemaMap = new LinkedHashMap<String, Object>();
							schemaMap.put(RestSamplingConsts.TYPE, EParameterDataType.ARRAY.toString().toLowerCase());
							schemaMap.put(RestSamplingConsts.ITEMS, itemsMap);

							jsonSchemaObjectPropertiesInner.put(RestSamplingConsts.SCHEMA, schemaMap);
							jsonSchemaObjectProperties.put(InnerName, jsonSchemaObjectPropertiesInner);

							if (apiName.equals(name))
								generateDefinitions(jsonSampleInner, jsonSchemaObjectProperties, InnerName);
							else
								generateDefinitions(jsonSampleInner, jsonSchemaObjectProperties, name + "##" + InnerName);
						}
					}
				}
			}
		} else if (dataType.equals(EParameterDataType.STRING) || dataType.equals(EParameterDataType.NUMBER)
				|| dataType.equals(EParameterDataType.BOOLEAN)) {
			Map<String, Object> jsonSchemaObjectPropertiesInner = new LinkedHashMap<String, Object>();
			jsonSchemaObjectPropertiesInner.put(RestSamplingConsts.TYPE, dataType.toString().toLowerCase());
			EParameterDataType format = EParameterDataType.inferFormatType(jsonSample);
			if (format != EParameterDataType.NULL)
				jsonSchemaObjectPropertiesInner.put(RestSamplingConsts.FORMAT, format.toString());
			jsonSchemaObjectProperties.put(name, jsonSchemaObjectPropertiesInner);

		}
		jsonSchemaObjectPropertiesCopy.putAll(jsonSchemaObjectProperties);
		Map<String, Object> definitionProperties = new LinkedHashMap<String, Object>();
		definitionProperties.put(RestSamplingConsts.PROPERTIES, jsonSchemaObjectProperties);
		Map<String, Object> definitionPropertiesGetPrevious = new LinkedHashMap<String, Object>();
		if (!apiName.equals(name)) {
			if (null != (Map<String, Object>) definitions.get(apiName + "##" + name)) {
				definitionPropertiesGetPrevious = (Map<String, Object>) definitions.get(apiName + "##" + name);
				jsonSchemaObjectProperties.putAll((Map<String, Object>) definitionPropertiesGetPrevious.get(RestSamplingConsts.PROPERTIES));
				definitionProperties.put(RestSamplingConsts.PROPERTIES, jsonSchemaObjectProperties);
			}
			definitions.put(apiName + "##" + name, definitionProperties);
		} else {
			if (null != (Map<String, Object>) definitions.get(apiName + "##" + name)) {
				definitionPropertiesGetPrevious = (Map<String, Object>) definitions.get(apiName + "##" + name);
				jsonSchemaObjectProperties.putAll((Map<String, Object>) definitionPropertiesGetPrevious.get(RestSamplingConsts.PROPERTIES));
				definitionProperties.put(RestSamplingConsts.PROPERTIES, jsonSchemaObjectProperties);
			}
			definitions.put(name, definitionProperties);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Object> generateSchemaFromString(String jsonSampleStr, String name) throws Exception {
		setApiName(name);
		// Make sure we got a valid string as input
		if (jsonSampleStr == null)
			throw new IllegalArgumentException("Sample string is empty");

		// Check if root of json sample is an array or an object?
		// And read it accordingly
		jsonSampleStr = jsonSampleStr.trim();
		Class<? extends Object> classType = (jsonSampleStr.charAt(0) == '[') ? List.class : Map.class;
		Object jsonSampleObj = mapper.readValue(jsonSampleStr, classType);

		// Generate json schema corresponding to the sample as object graph
		// LinkedHashMap<String, Object> jsonSchemaObject = new
		// LinkedHashMap<String, Object>(5);
		// mapDefinitions = new LinkedHashMap<String, Object>();
		List<Object> arrayOfObjects = null;
		List<Object> jsonSchemaObject = new ArrayList<Object>();

		// Response is either an array of maps or one map.
		// If an array then considering only the first entry of the array.
		if (jsonSampleObj instanceof List) {
			arrayOfObjects = (List<Object>) jsonSampleObj;
			if (arrayOfObjects.get(0) != null && arrayOfObjects.get(0) instanceof Map) {
				generateSchemaForJsonObject((Map<?, ?>) arrayOfObjects.get(0), jsonSchemaObject, name);
			} else if (arrayOfObjects.get(0) != null && arrayOfObjects.get(0) instanceof List) {
				generateSchemaForJsonObject(arrayOfObjects, jsonSchemaObject, name);
			} else {
				if (arrayOfObjects.get(0) != null) {
					EParameterDataType dataType = EParameterDataType.inferType(arrayOfObjects.get(0));
					Map<String, Object> itemsMap = new LinkedHashMap<String, Object>();
					itemsMap.put(RestSamplingConsts.TYPE, dataType.toString().toLowerCase());
					Map<String, Object> mapForObjectTypes = new LinkedHashMap<String, Object>();
					Map<String, Object> schemaMap = new LinkedHashMap<String, Object>();
					schemaMap.put(RestSamplingConsts.TYPE, EParameterDataType.ARRAY.toString().toLowerCase());
					schemaMap.put(RestSamplingConsts.ITEMS, itemsMap);
					mapForObjectTypes.put(RestSamplingConsts.SCHEMA, schemaMap);
					mapForObjectTypes.put(RestSamplingConsts.DESCRIPTION, "successful operation");

					jsonSchemaObject.add(mapForObjectTypes);
				}
			}
		} else if (jsonSampleObj instanceof Map) {
			Map<?, ?> sampleMap = (Map<?, ?>) jsonSampleObj;
			if (sampleMap.size() > 0)
				generateSchemaForJsonObject(sampleMap, jsonSchemaObject, name);
		}
		if (definitions.size() > 0)
			setDefinitions(definitions);
		return jsonSchemaObject;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public void generateSchemaFromParameter(Api api, Parameter parameter) throws Exception {
		try {
			String paramValue = (String) parameter.getValue();
			if (EContentType.APPLICATION_XML == api.getRequestContentType())
				paramValue = JsonUtil.XML2JSON(paramValue);

			// Make sure we got a valid string as input
			if (paramValue == null) {
				if (parameter.isMandatory()) {
					throw new IllegalArgumentException("Mandatory field " + parameter.getName() + " string is null");
				}
			}

			paramValue = paramValue.trim();
			String jsonFinalStr = "{\"" + parameter.getName() + "\":" + paramValue + "}";

			generateSchemaFromString(jsonFinalStr, api.getName() + RestSamplingConsts.REQUEST);
		} catch (ApiException e) {
			throw new IllegalArgumentException("Invalid XML for parameter [" + parameter.getName() + "].\nError: " + e.getMessage());
		}
	}
}