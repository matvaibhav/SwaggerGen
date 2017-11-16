package com.infa.rest.swagger.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RestEndPoint {
	private String baseUrl = "";
	private List<Api> apis = new ArrayList<>();
	private Map<String, Api> apiNameMap = new HashMap<String, Api>();
	private Map<String, List<Api>> apiMapByPath = new HashMap<String, List<Api>>();

	public RestEndPoint() {

	}

	public RestEndPoint(String swaggerString) {
	}

	public List<Api> getApis() {
		return apis;
	}

	public void removeApi(Api api) {
		apis.remove(api);
		apiNameMap.remove(api.getName());
		List<Api> pathApis = apiMapByPath.get(api.getPath());

		if (null != pathApis) {
			pathApis.remove(api);
			if (pathApis.size() == 0)
				apiMapByPath.remove(api.getPath());// if no path remove key from
													// map
		}
	}

	// Use Merge API to add multiple APIs as used from UI
	// public void setApis(List<Api> apis) {
	// this.apis = apis;
	// }
	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getSwagger() {
		return new SwaggerParser().getSwagger(this);
	}

	public void mergeApi(Api api) {
		for (Api a : apis) {
			if (a.getName().equalsIgnoreCase(api.getName())) {
				removeApi(a);
				break;
			}
		}

		apis.add(api);
		apiNameMap.put(api.getName(), api);

		List<Api> pathApis = apiMapByPath.get(api.getPath());

		if (null == pathApis) {
			pathApis = new ArrayList<Api>();
		}
		pathApis.add(api);
		apiMapByPath.put(api.getPath(), pathApis);
	}

	public Api getNewApi() {
		Api newAPI = new Api(this);
		newAPI.setVerb(EVerb.GET);
		return newAPI;
	}

	public Api getApiByName(String name) {
		// for(Api a: apis) {
		// if(a.getName().equalsIgnoreCase(name)) {
		// return a;
		// }
		// }
		// return null;
		return apiNameMap.get(name);
	}

	public boolean isUniqueName(Api api) {
		Api apiInMap = apiNameMap.get(api.getName());
		if (apiInMap != null && apiInMap.getIdentity() != api.getIdentity()) {// name
																				// check
																				// against
																				// Self
																				// is
																				// ignored
			return false;
		}
		return true;
	}

	public boolean isUniquePathVerb(Api api) {
		boolean isUnique = true;
		List<Api> apisInPath = apiMapByPath.get(api.getPath());
		if (null != apisInPath) {
			for (Api pathApi : apisInPath) {
				if (pathApi.getIdentity() != api.getIdentity()
						&& pathApi.getVerb() == api.getVerb()) {// check against
																// Self is
																// ignored
					isUnique = false;
					break;
				}
			}
		}
		return isUnique;
	}

	public Map<String, List<Api>> getApiPathMap() {
		return apiMapByPath;
	}

	/*
	 *
	 * public static void main(String args[]){ RestEndPoint endPoint = new
	 * RestEndPoint(); IConnectInfo connectInfo = new ASConnectInfo();
	 * endPoint.setBaseUrl("http://petstore.swagger.io/v2");
	 * endPoint.setConnectInfo(connectInfo);
	 *
	 * Api api = new Api(endPoint); api.setName("pet/findByStatus");
	 * api.setPath("pet/findByStatus");
	 *
	 * Parameter statusParameter = new Parameter();
	 * statusParameter.setName("status"); statusParameter.setValue("available");
	 * statusParameter.setType(EParameterType.Query);
	 * api.getParameters().add(statusParameter);
	 *
	 *
	 * Parameter acceptParameter = new Parameter();
	 * acceptParameter.setName("Accept");
	 * acceptParameter.setValue("application/json");
	 * acceptParameter.setType(EParameterType.Header);
	 * api.getParameters().add(acceptParameter);
	 *
	 * try { System.out.println(api.getResponse()); } catch (IOException e) { //
	 * TODO Auto-generated catch block e.printStackTrace(); }
	 *
	 * api.getSwagger();
	 *
	 * }
	 */

}
