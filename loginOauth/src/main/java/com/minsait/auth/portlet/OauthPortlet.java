package com.minsait.auth.portlet;

import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.minsait.auth.constants.OauthPortletKeys;

import javax.portlet.Portlet;

import org.osgi.service.component.annotations.Component;

/**
 * @author malaboisse
 */
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.instanceable=false",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.name=" + OauthPortletKeys.LoginOauth,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user"
	},
	service = Portlet.class
)
public class OauthPortlet extends MVCPortlet {
}