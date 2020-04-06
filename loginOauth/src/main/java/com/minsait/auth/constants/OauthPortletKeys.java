package com.minsait.auth.constants;

/**
 * @author malaboisse
 */
public class OauthPortletKeys {

	public static final String LoginOauth = "loginoauth";

    public static final String CONFIGURATION_ID = "com.minsait.auth.configuration.OauthAuthConfig";
    
    public static final String RESPONSE_TYPE = "response_type";
    public static final String CLIENT_ID = "client_id";
    public static final String REDIRECT_URI = "redirect_uri";
    public static final String STATE = "state";
    public static final String GRANT_TYPE = "grant_type";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String BEARER = "Bearer ";
    
    public static final String RESPONSE_TYPE_CODE_AS = "code";
    public static final String RESPONSE_TYPE_ERROR_AS = "error";
    public static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
    
    public static final String PORTAL_LOGIN_URL = "/c/portal/login";
    public static final String PORTAL_LOGOUT_URL = "/c/portal/logout";
    
	public static final String PARAM_RESERVED_PREFFIX="_R__";
	public static final String RESERVED_ACCESS_TOKEN_PARAM = PARAM_RESERVED_PREFFIX+"accessToken";
	
	public static final String RESERVED_STATE_PARAM = PARAM_RESERVED_PREFFIX + STATE;
	
	

	/** HEADERS DE PETICIÓN COMÚN **/
	public static final String REQUEST_HEADER_ACCEPT = "Accept";

	public static final int StatusCode = 200;
	public static final int StatusCode206 = 206;
	public static final int StatusCode201 = 201;
	public static final int StatusCode404 = 404;
	public static final int StatusCode416 = 416;
	public static final int StatusCode408 = 408;
}