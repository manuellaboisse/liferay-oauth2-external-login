package com.minsait.auth.configuration;

import com.minsait.auth.constants.OauthPortletKeys;

import aQute.bnd.annotation.metatype.Meta;


/**
 * @author malaboisse
 */
@Meta.OCD(id = OauthPortletKeys.CONFIGURATION_ID)
public interface OauthAuthConfig {
  
	  @Meta.AD(
			  type = Meta.Type.String,
	          required = false
	  )
	  public String loginErrorUrl();
	  
	  @Meta.AD(
			  type = Meta.Type.String,
	          required = false
	  )
	  public String redirectUrl();
	
	  @Meta.AD(
			  type = Meta.Type.String,
	          required = false
	  )
	  public String oauthServerUrl();
	  
	  @Meta.AD(
			  type = Meta.Type.String,
	          required = false
	  )
	  public String oauthCliUrl();
	
	  @Meta.AD(
			  type = Meta.Type.String,
	          required = false
	  )
	  public String oauthAuthPath();

	  @Meta.AD(
			  type = Meta.Type.String,
	          required = false
	  )
	  public String oauthTokenPath();
	  
	  @Meta.AD(
			  type = Meta.Type.String,
	          required = false
	  )
	  public String oauthAuthClientId();

	  @Meta.AD(
			  type = Meta.Type.String,
	          required = false
	  )
	  public String oauthAuthClientSecret();
}
