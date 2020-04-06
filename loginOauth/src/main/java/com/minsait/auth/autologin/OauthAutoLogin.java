package com.minsait.auth.autologin;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auto.login.AutoLogin;
import com.liferay.portal.kernel.security.auto.login.BaseAutoLogin;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.minsait.auth.bean.OauthAuthUserWrapper;
import com.minsait.auth.configuration.OauthAuthConfig;
import com.minsait.auth.constants.OauthPortletKeys;
import com.minsait.auth.utilities.OauthGeneralMethods;

import java.util.Map;

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
        service = AutoLogin.class,
        configurationPid = OauthPortletKeys.CONFIGURATION_ID
)
public class OauthAutoLogin extends BaseAutoLogin {

	private volatile OauthAuthConfig _configuration;

    private static final Log _log =
            LogFactoryUtil.getLog(OauthAutoLogin.class);

	@Reference
	OauthGeneralMethods _oauthGeneralMethods;

    @Reference
    UserLocalService _userLocalService;
    
    @Override
    protected String[] doLogin(
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        final long companyId = PortalUtil.getCompanyId(request);

        String sessionId = request.getRequestedSessionId();
       
        HttpSession session = request.getSession(true);
        if(Validator.isNull(sessionId)) {
        	//If no session, access straight from /c/portal/login
        	sessionId = session.getId();
        }

        _log.info("----AUTO LOGIN : SESSIONID: " + sessionId);
        
        User _user = null;
        String accessToken = (String)session.getAttribute(OauthPortletKeys.RESERVED_ACCESS_TOKEN_PARAM);
        if(Validator.isNotNull(accessToken)) {
        	
        	//Get the user from server by /api/jsonws/user/get-current-user
            String _userJson = _oauthGeneralMethods.getUserFromServer(_configuration.oauthServerUrl(), accessToken);
            _log.info("********************** USER **********: " + _userJson);
            
            //Map the user to our wrapper
            OauthAuthUserWrapper userWrapper = JSONFactoryUtil.looseDeserialize(_userJson, 
            		OauthAuthUserWrapper.class);
            
            //Search the user in our client
            _user = _userLocalService.fetchUserByScreenName(companyId, userWrapper.getScreenName());

            if (Validator.isNull(_user)){
            	_user = _oauthGeneralMethods.createUser(companyId, userWrapper);
            } else {
            	_user = _oauthGeneralMethods.updateUser(_user, userWrapper);
            }

        }

        final String[] credentials;
        if (_user != null) {
            credentials = new String[3];
            credentials[0] = String.valueOf(_user.getUserId());
            credentials[1] = _user.getPassword();
            credentials[2] = Boolean.toString(Boolean.FALSE);
        } else {
            credentials = null;
        }
      
        return credentials;
    }

    @Activate
    @Modified
    protected void activate(Map<Object, Object> properties) {
        _configuration = ConfigurableUtil.createConfigurable(
                OauthAuthConfig.class, properties);
    }
}
