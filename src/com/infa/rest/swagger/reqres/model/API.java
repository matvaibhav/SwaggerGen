
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
@JsonPropertyOrder({
    "headers",
    "formParams",
    "body",
    "accept",
    "contentType",
    "auth",
    "method",
    "endPoint",
    "endPath",
    "aparam",
    "query",
    "apiHost",
    "basePath",
    "apiPath",
    "operationId",
    "userResponse"
})
public class API implements Serializable{

	private static final long serialVersionUID = 1L;
	@JsonProperty("headers")
    private Headers headers;
    @JsonProperty("formParams")
    private FormParams formParams;
    @JsonProperty("body")
    private String body;
    @JsonProperty("accept")
    private String accept="application/json";
    @JsonProperty("contentType")
    private String contentType="application/json";
    @JsonProperty("auth")
    private String auth="No Auth";
    @JsonProperty("method")
    private String method="GET";
    @JsonProperty("endPoint")
    private String endPoint;
    @JsonProperty("endPath")
    private String endPath;
    @JsonProperty("aparam")
    private Aparam aparam;
    @JsonProperty("query")
    private Query query;
    @JsonProperty("apiHost")
    private String apiHost;
    @JsonProperty("basePath")
    private String basePath;
    @JsonProperty("apiPath")
    private String apiPath;
    @JsonProperty("operationId")
    private String operationId;
    @JsonProperty("userResponse")
    private String userResponse;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     *     The headers
     */
    @JsonProperty("headers")
    public Headers getHeaders() {
        return headers;
    }

    /**
     *
     * @param headers
     *     The headers
     */
    @JsonProperty("headers")
    public void setHeaders(Headers headers) {
        this.headers = headers;
    }

    /**
     *
     * @return
     *     The formParams
     */
    @JsonProperty("formParams")
    public FormParams getFormParams() {
        return formParams;
    }

    /**
     *
     * @param formParams
     *     The formParams
     */
    @JsonProperty("formParams")
    public void setFormParams(FormParams formParams) {
        this.formParams = formParams;
    }

    /**
     *
     * @return
     *     The body
     */
    @JsonProperty("body")
    public String getBody() {
        return body;
    }

    /**
     *
     * @param body
     *     The body
     */
    @JsonProperty("body")
    public void setBody(String body) {
        this.body = body;
    }

    /**
     *
     * @return
     *     The accept
     */
    @JsonProperty("accept")
    public String getAccept() {
        return accept;
    }

    /**
     *
     * @param accept
     *     The accept
     */
    @JsonProperty("accept")
    public void setAccept(String accept) {
        this.accept = accept;
    }

    /**
     *
     * @return
     *     The contentType
     */
    @JsonProperty("contentType")
    public String getContentType() {
        return contentType;
    }

    /**
     *
     * @param contentType
     *     The contentType
     */
    @JsonProperty("contentType")
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     *
     * @return
     *     The auth
     */
    @JsonProperty("auth")
    public String getAuth() {
        return auth;
    }

    /**
     *
     * @param auth
     *     The auth
     */
    @JsonProperty("auth")
    public void setAuth(String auth) {
        this.auth = auth;
    }

    /**
     *
     * @return
     *     The method
     */
    @JsonProperty("method")
    public String getMethod() {
        return method;
    }

    /**
     *
     * @param method
     *     The method
     */
    @JsonProperty("method")
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     *
     * @return
     *     The endPoint
     */
    @JsonProperty("endPoint")
    public String getEndPoint() {
        return endPoint;
    }

    /**
     *
     * @param endPoint
     *     The endPoint
     */
    @JsonProperty("endPoint")
    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    /**
     *
     * @return
     *     The endPath
     */
    @JsonProperty("endPath")
    public String getEndPath() {
        return endPath;
    }

    /**
     *
     * @param endPath
     *     The endPath
     */
    @JsonProperty("endPath")
    public void setEndPath(String endPath) {
        this.endPath = endPath;
    }

    /**
     *
     * @return
     *     The aparam
     */
    @JsonProperty("aparam")
    public Aparam getAparam() {
        return aparam;
    }

    /**
     *
     * @param aparam
     *     The aparam
     */
    @JsonProperty("aparam")
    public void setAparam(Aparam aparam) {
        this.aparam = aparam;
    }

    /**
     *
     * @return
     *     The query
     */
    @JsonProperty("query")
    public Query getQuery() {
        return query;
    }

    /**
     *
     * @param query
     *     The query
     */
    @JsonProperty("query")
    public void setQuery(Query query) {
        this.query = query;
    }

    /**
     *
     * @return
     *     The apiHost
     */
    @JsonProperty("apiHost")
    public String getApiHost() {
        return apiHost;
    }

    /**
     *
     * @param apiHost
     *     The apiHost
     */
    @JsonProperty("apiHost")
    public void setApiHost(String apiHost) {
        this.apiHost = apiHost;
    }

    /**
     *
     * @return
     *     The basePath
     */
    @JsonProperty("basePath")
    public String getBasePath() {
        return basePath;
    }

    /**
     *
     * @param basePath
     *     The basePath
     */
    @JsonProperty("basePath")
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    /**
     *
     * @return
     *     The apiPath
     */
    @JsonProperty("apiPath")
    public String getApiPath() {
        return apiPath;
    }

    /**
     *
     * @param apiPath
     *     The apiPath
     */
    @JsonProperty("apiPath")
    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    /**
     *
     * @return
     *     The operationId
     */
    @JsonProperty("operationId")
    public String getOperationId() {
        return operationId;
    }

    /**
     *
     * @param operationId
     *     The operationId
     */
    @JsonProperty("operationId")
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }
    
    /**
    *
    * @return
    *     The userResponse
    */
   @JsonProperty("userResponse")
   public String getUserResponse() {
       return userResponse;
   }

   /**
    *
    * @param userResponse
    *     The userResponse
    */
   @JsonProperty("userResponse")
   public void setUserResponse(String userResponse) {
       this.userResponse = userResponse;
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
