package com.infa.rest.swagger.impl;

public interface ISwaggerParser {

	public RestEndPoint parseSwagger(Class<?> cl, String filePath) throws Throwable;

	public String getSwagger(Api api);

	public String getSwagger(RestEndPoint restEndPoint);
	
	public Api fetchResponse(Api api);

}