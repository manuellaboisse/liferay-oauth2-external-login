
package com.minsait.auth.utilities;

import com.liferay.portal.kernel.model.User;
import com.minsait.auth.bean.OauthAuthUserWrapper;

import java.util.Map;

public interface OauthGeneralMethods {

	public String getJson(
		String URLBase, 
		Map<String, String> parametros,
		Map<String, String> headers);

	public String postIdentityLoginJson(
			String authServerTokensURI,
			String parametros);
    public User createUser(long companyId,
    		OauthAuthUserWrapper selaeUserWrapper);
    
    public User updateUser(User userParam,
    		OauthAuthUserWrapper selaeUserWrapper);

    public String getUserFromServer(String oauthServerUrl,
    		String accessToken);
}
