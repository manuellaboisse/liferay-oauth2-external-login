package com.minsait.auth.filter;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.servlet.filters.BasePortalFilter;
import com.minsait.auth.bean.OauthAuthLoginResponseWrapper;
import com.minsait.auth.configuration.OauthAuthConfig;
import com.minsait.auth.constants.OauthPortletKeys;
import com.minsait.auth.utilities.OauthGeneralMethods;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author malaboisse
 */
@Component(
        immediate = true,
        property = {
                "dispatcher=FORWARD",
                "dispatcher=REQUEST",
                "servlet-context-name=",
                "servlet-filter-name=OAUTH Auto Login Filter",
                "url-pattern="+OauthPortletKeys.PORTAL_LOGIN_URL,
                "url-pattern="+OauthPortletKeys.PORTAL_LOGOUT_URL
        },
        service = Filter.class,
        configurationPid = OauthPortletKeys.CONFIGURATION_ID
)
public class OauthAutoLoginFilter extends BasePortalFilter {

    private volatile OauthAuthConfig _configuration;

    private static final Log _log =
            LogFactoryUtil.getLog(OauthAutoLoginFilter.class);
    
	@Reference
	OauthGeneralMethods _oauthGeneralMethods;
	
    @Override
    protected void processFilter(
            HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain)
            throws Exception {

        String sessionId = request.getRequestedSessionId();
    	if(Validator.isNull(sessionId)) {
        	//If no session, access straight from /c/portal/login
            HttpSession session = request.getSession(true);
        	sessionId = session.getId();
        }
    	_log.info("******ENTRANDO AUTH SESSIONID: "+ sessionId);

	    String bypass = (String) request.getParameter("bypass");
	    if(bypass == null) {
	        String authServerUrl = _configuration.oauthServerUrl();
	        if(OauthPortletKeys.PORTAL_LOGIN_URL.equals(request.getAttribute(WebKeys.INVOKER_FILTER_URI))) {
	        	if(Validator.isNotNull(authServerUrl)) {
	        		
	        		String code = (String) request.getParameter(OauthPortletKeys.RESPONSE_TYPE_CODE_AS);
	        		String error = (String) request.getParameter(OauthPortletKeys.RESPONSE_TYPE_ERROR_AS);
	        		if(Validator.isNull(code) && Validator.isNull(error)) {
	        			//--------------- FIRST STEP: REDIRECT TO LOGIN AT AUTHENTICATION SERVER ---------------------
			        	_log.info("AUTH FILTER LOGIN sessionId: "+ sessionId);
			        	
			        	String redirectUrl = _configuration.redirectUrl();
			        	
			        	response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
			        	String authServerURI = authServerUrl + _configuration.oauthAuthPath();
			        	String clientId = _configuration.oauthAuthClientId();
			        	
			        	Map<String, String> params = new LinkedHashMap<String, String>();
			        	params.put(OauthPortletKeys.RESPONSE_TYPE, OauthPortletKeys.RESPONSE_TYPE_CODE_AS);
			        	params.put(OauthPortletKeys.CLIENT_ID, clientId);
			        	params.put(OauthPortletKeys.REDIRECT_URI, redirectUrl);
			        	params.put(OauthPortletKeys.STATE, sessionId);
			        	String parametros = getDataString(params);
			        	
			        	String newUrl = authServerURI + StringPool.QUESTION + parametros;
 	
			        	_log.info("AUTH FILTER LOGIN REDIRECT : "+ newUrl);
			        	response.sendRedirect(newUrl);
		        	
	        		} else {
	        			
	        			//------------------ SECOND STEP: Make a POST to obtain Access Token -------------------
	        			if(Validator.isNotNull(error)) {
	        				//Ha ocurrido un error
		        			_log.error("AUTH FILTER LOGIN ERROR : Ha ocurrido un error: " + error);
	        				sendRedirectError(request, response);
	        			} else {
	        				

	        				String state = (String) request.getParameter(OauthPortletKeys.STATE);
	        				_log.info("STATE recibido:::: " +state);
	        				
	                			
		        			Map<String, String> params = new LinkedHashMap<String, String>();
		        			params.put(OauthPortletKeys.GRANT_TYPE, OauthPortletKeys.GRANT_TYPE_AUTHORIZATION_CODE);
		        			params.put(OauthPortletKeys.RESPONSE_TYPE_CODE_AS, code);
		        			params.put(OauthPortletKeys.CLIENT_ID, _configuration.oauthAuthClientId());
		        			params.put(OauthPortletKeys.CLIENT_SECRET, _configuration.oauthAuthClientSecret());
							params.put("redirect_uri", _configuration.redirectUrl());
							String parametros = getDataString(params);
		        			_log.info("AUTH FILTER IDENTITY TOKEN PARAMS : "+ parametros);
		        			
		        			String tokenServerURI = authServerUrl + _configuration.oauthTokenPath();
	        				String identityJson = _oauthGeneralMethods.postIdentityLoginJson(tokenServerURI, parametros);
		        			_log.info("AUTH FILTER IDENTITY TOKEN : "+ identityJson);
		        			OauthAuthLoginResponseWrapper loginResponseWrapper = JSONFactoryUtil.looseDeserialize(identityJson, 
		        					OauthAuthLoginResponseWrapper.class);
		        			
		        			if(Validator.isNotNull(loginResponseWrapper.getError())){
		        				//An error ocurred
			        			_log.error("AUTH FILTER IDENTITY ERROR : Ha ocurrido un error: " + loginResponseWrapper.getError());
		        				sendRedirectError(request, response);
		        			} else {
			        			if(Validator.isNotNull(loginResponseWrapper.getAccess_token())){
				        			request.getSession().setAttribute(OauthPortletKeys.RESERVED_ACCESS_TOKEN_PARAM, 
				        					loginResponseWrapper.getAccess_token());
			        			}
		        			}
		        		}
	        		}
	        	}
	        } else if(OauthPortletKeys.PORTAL_LOGOUT_URL.equals(request.getAttribute(WebKeys.INVOKER_FILTER_URI))) {
	        	_log.info("AUTH FILTER LOGOUT sessionId: "+ sessionId);
	        	request.getSession().setAttribute(OauthPortletKeys.RESERVED_ACCESS_TOKEN_PARAM, StringPool.BLANK);
	        }
	    }
        processFilter(OauthAutoLoginFilter.class.getName(), request, response, filterChain);
    }
    
    
    private String getDataString(Map<String, String> params)  {
    	StringBuilder result = new StringBuilder();
    	boolean first = true;
    	for(Map.Entry<String, String> entry:params.entrySet()) {
    		if(first) {
    			first = false;
    		} else {
    			result.append(StringPool.AMPERSAND);
    		}
    		result.append(entry.getKey());
    		result.append(StringPool.EQUAL);
    		result.append(entry.getValue());
    	}
    	return result.toString();
    }
    
    private void sendRedirectError(HttpServletRequest request, HttpServletResponse response) 
    		throws Exception {
    	String errorUrl = _configuration.loginErrorUrl();
    	if(Validator.isNotNull(errorUrl)) {
    		response.sendRedirect(errorUrl);
    	}
    }
    
    @Activate
    @Modified
    protected void activate(Map<Object, Object> properties) {
        _configuration = ConfigurableUtil.createConfigurable(
                OauthAuthConfig.class, properties);
    }
}
