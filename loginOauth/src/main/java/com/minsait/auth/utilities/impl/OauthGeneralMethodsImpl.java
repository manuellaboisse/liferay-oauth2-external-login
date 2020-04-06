
package com.minsait.auth.utilities.impl;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;
import com.minsait.auth.bean.OauthAuthUserWrapper;
import com.minsait.auth.constants.OauthPortletKeys;
import com.minsait.auth.utilities.OauthGeneralMethods;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author malaboisse
 */
@Component
public class OauthGeneralMethodsImpl implements OauthGeneralMethods {


	@Reference
	JSONFactory jsonFactory;

    @Reference
    UserLocalService _userLocalService;

    @Reference
    Portal _portal;

    @Reference
    GroupLocalService _groupLocalservice;
    
	private static Log _log = LogFactoryUtil.getLog(OauthGeneralMethodsImpl.class);

	@Override
	public String getJson(String URLBase,
			Map<String, String> parametros,
			Map<String, String> headers) {

		String urlRest = URLBase;

		_log.debug("INICIO OauthGeneralMethodsImpl.getJson");
		_log.debug("Parametro: URLBase con valor: " + URLBase);
		_log.debug("Parametro: parametros con valor: "
				+ (Validator.isNotNull(parametros) ? parametros.toString() : StringPool.BLANK));

		// Construimos la URL con los parámetros indicados. Si no nos indican
		// ninguno, seguiremos con la URLBase
		if (Validator.isNotNull(parametros)) {
			urlRest = getParametersURL(URLBase, parametros);
		}
		
		try {

			if (Validator.isNotNull(urlRest)) {
				HttpGet httpget = new HttpGet(urlRest);
				return baseJson(null, httpget, headers);
			} else {
				_log.debug("FIN OauthGeneralMethodsImpl.getJson");
				return StringPool.BLANK;
			}
		} catch (Exception e) {
			e.printStackTrace();
			_log.debug("FIN OauthGeneralMethodsImpl.getJson TOKEN: " + e.getMessage());
			return StringPool.BLANK;
		}
	}
	
	private String getParametersURL(String URLBase, Map<String, String> parametros) {

		_log.debug("INICIO OauthGeneralMethodsImpl.getParametersURL");
		if (Validator.isNotNull(URLBase) && Validator.isNotNull(parametros)) {

			StringBuilder sb = new StringBuilder(URLBase);
			String resultado = StringPool.BLANK;
			for (Map.Entry<String, String> entry : parametros.entrySet()) {

				_log.debug("Parametro: " + entry.getKey() + " con valor: " + entry.getValue());

				// Ignore Reserved
				if (!entry.getKey().startsWith(OauthPortletKeys.PARAM_RESERVED_PREFFIX)) {
					sb.append(StringPool.FORWARD_SLASH);
					sb.append(entry.getKey());
					sb.append(StringPool.FORWARD_SLASH);
					sb.append(entry.getValue());
				}
			}

			resultado = sb.toString();

			if (Validator.isNotNull(resultado) && resultado.endsWith(StringPool.FORWARD_SLASH)) {

				resultado = resultado.substring(0, resultado.length() - 1);

			}

			// Agregamos barra al final para la integración final con plataforma Oracle
			// ORDS
			if (!resultado.endsWith(StringPool.FORWARD_SLASH)) {
				resultado = resultado.concat(StringPool.FORWARD_SLASH);
			}

			return resultado;
		} else {
			_log.debug("FIN OauthGeneralMethodsImpl.getParametersURL");
			return null;
		}
	}
	
	private String baseJson(String json, HttpRequestBase httpBase, Map<String, String> headers) {

		_log.info("INICIO OauthGeneralMethodsImpl.baseJson");
		_log.info("Servicio: " + (Validator.isNotNull(httpBase) ? httpBase.toString() : StringPool.BLANK));
		_log.info("Parametro: json con valor: " + json);

		String respuesta = StringPool.BLANK;

		try {
			CloseableHttpClient httpClient = HttpClients.createDefault();

			if (httpBase instanceof HttpEntityEnclosingRequest) {
				HttpEntityEnclosingRequestBase httpEnclosing = (HttpEntityEnclosingRequestBase) httpBase;

				StringEntity jsonEntity = new StringEntity(json, StandardCharsets.UTF_8);
				httpEnclosing.setHeader(HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON);
				httpEnclosing.setHeader(HttpHeaders.ACCEPT, ContentTypes.TEXT_PLAIN);
				httpEnclosing.setEntity(jsonEntity);

			}

			// Configurando Headers
			headers.put(OauthPortletKeys.REQUEST_HEADER_ACCEPT, ContentTypes.APPLICATION_JSON);

			if (Validator.isNotNull(headers)) {
				for (String headerName : headers.keySet()) {
					httpBase.setHeader(headerName, headers.get(headerName));
				}
			}

			long startTime = System.currentTimeMillis();

			// Ejecución de la petición HTTP
			CloseableHttpResponse httpResponse = httpClient.execute(httpBase);

			long endTime = System.currentTimeMillis();

			// LLamada al servicio
			_log.info("LLamada al servicio: " + (Validator.isNotNull(httpBase) ? httpBase.toString() : StringPool.BLANK)
					+ " -- Tiempo de respuesta: " + String.valueOf(endTime - startTime) + "ms");

			// Código de respuesta de la petición
			final int statusCode = httpResponse.getStatusLine().getStatusCode();
			final String statusMsg = httpResponse.getStatusLine().getReasonPhrase();

			_log.debug("Parametro: statusCode con valor: " + statusCode);

			// Resultado NOK
			if (statusCode != OauthPortletKeys.StatusCode && statusCode != OauthPortletKeys.StatusCode206
					&& statusCode != OauthPortletKeys.StatusCode201 && statusCode != OauthPortletKeys.StatusCode416) {
				JSONObject jsonRespuesta = JSONFactoryUtil.createJSONObject();
				String entityContent = getEntityContent(httpResponse);
				if (!Validator.isBlank(entityContent)) {
					jsonRespuesta = JSONFactoryUtil.createJSONObject(entityContent);
				}

				respuesta = jsonRespuesta.toString();

				httpClient.close();

				_log.debug("Failed : HTTP error code : " + statusCode + " (" + statusMsg + "). Llamada al servicio: "
						+ (Validator.isNotNull(httpBase) ? httpBase.toString() : StringPool.BLANK));
				_log.error("Failed : HTTP error code : " + statusCode + " (" + statusMsg + "). Llamada al servicio: "
						+ (Validator.isNotNull(httpBase) ? httpBase.toString() : StringPool.BLANK));
			}

			// Resultado OK
			else {

				String entityContent = getEntityContent(httpResponse);

				_log.debug("Parametro: resp con valor: " + entityContent);

				JSONObject jsonRespuesta = JSONFactoryUtil.createJSONObject(entityContent);

				respuesta = jsonRespuesta.toString();

				httpClient.close();
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return respuesta;
	}


	/**
	 * Método auxiliar para obtener el contenido de la entidad proporcionada en un
	 * httpResponse
	 *
	 * @param httpResponse
	 *            CloseableHttpResponse
	 * @return String contenido de la entidad
	 * @throws IllegalStateException
	 *             error al recuperar el contenido
	 * @throws IOException
	 *             error al recuperar el contenido
	 */
	private String getEntityContent(CloseableHttpResponse httpResponse) throws IllegalStateException, IOException {

		BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));

		StringBuffer resp = new StringBuffer();
		String inputLine;

		while ((inputLine = reader.readLine()) != null) {
			resp.append(inputLine);
		}

		reader.close();

		return resp.toString();
	}
	
	@Override
	public String postIdentityLoginJson(
			String authServerTokensURI,
			String parametros) {
		return postIdentityLogin(
				authServerTokensURI, parametros);
	}
	
	private String postIdentityLogin(String URLBase, String parametros) {
		HttpPost httpBase = new HttpPost(URLBase);
		_log.info("INICIO OauthGeneralMethodsImpl.postIdentityLogin");
		_log.info("Parametro: parametros con valor: "
				+ (Validator.isNotNull(httpBase) ? httpBase.toString() : StringPool.BLANK));
		String respuesta = StringPool.BLANK;

		try {
			CloseableHttpClient httpClient = HttpClients.createDefault();

			StringEntity jsonEntity = new StringEntity(parametros);
			if (httpBase instanceof HttpEntityEnclosingRequest) {
				HttpEntityEnclosingRequestBase httpEnclosing = (HttpEntityEnclosingRequestBase) httpBase;

				httpEnclosing.setHeader(HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_X_WWW_FORM_URLENCODED);
				httpEnclosing.setEntity(jsonEntity);
			}

			Map<String, String> headers = new LinkedHashMap<String, String>();

			// Configurando Headers
			headers.put(HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_X_WWW_FORM_URLENCODED);
			if (Validator.isNotNull(headers)) {
				for (String headerName : headers.keySet()) {
					httpBase.setHeader(headerName, headers.get(headerName));
				}
			}

			_log.info("Parametro2: parametros: " + parametros);

			// Ejecución de la petición HTTP
			CloseableHttpResponse httpResponse = httpClient.execute(httpBase);

			// Código de respuesta de la petición
			int statusCode = httpResponse.getStatusLine().getStatusCode();

			_log.info("Parametro: statusCode con valor: " + statusCode);

			// Resultado NOK
			if (statusCode != OauthPortletKeys.StatusCode) {

				String entityContent = getEntityContent(httpResponse);

				JSONObject jsonRespuesta = JSONFactoryUtil.createJSONObject(entityContent);

				respuesta = jsonRespuesta.toString();
				httpClient.close();

				_log.debug("Failed : HTTP error code : " + statusCode);
				_log.error("Failed : HTTP error code : " + statusCode);
			}

			// Resultado OK
			else {
				String entityContent = getEntityContent(httpResponse);

				_log.info("Parametro: resp con valor: " + entityContent);

				JSONObject jsonRespuesta = JSONFactoryUtil.createJSONObject(entityContent);

				respuesta = jsonRespuesta.toString();

				httpClient.close();
			}
		} catch (RuntimeException e) {
			_log.error(e.getMessage());
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			_log.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			_log.error(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			_log.error(e.getMessage());
			e.printStackTrace();
		}

		return respuesta;
	}

	@Override
	public User createUser(long companyId,
    		OauthAuthUserWrapper userWrapper) {
		
        User user = null;
        try {

            final Group defaultGroup = _groupLocalservice.getCompanyGroup(companyId);

            final long creatorUserId = defaultGroup.getCreatorUserId();
            final boolean autoPassword = Boolean.TRUE;
            final String password1 = StringPool.BLANK;
            final String password2 = StringPool.BLANK;
            final boolean autoScreenName = Boolean.FALSE;
            final String screenName = userWrapper.getScreenName();
            final String emailAddress = userWrapper.getEmailAddress();
            final long facebookId = 0l;
            final String openId = StringPool.BLANK;
            final Locale locale = _portal.getSiteDefaultLocale(defaultGroup.getGroupId());
            final String firstName = userWrapper.getFirstName();
            final String middleName = StringPool.BLANK;
            final String lastName = userWrapper.getLastName();
            final long prefixId = 0;
            final long suffixId = 1;
            final boolean male = Boolean.TRUE;
            final int birthdayMonth = 0;
            int birthdayDay = 1;
            int birthdayYear = 1970;
            final String jobTitle = userWrapper.getJobTitle();
            final long[] groupIds = new long[]{defaultGroup.getGroupId()};
            final long[] organizationIds = null;
            final long[] roleIds = null;
            final long[] userGroupIds = null;
            final boolean sendEmail = Boolean.FALSE;
            final ServiceContext serviceContext = new ServiceContext();

            user = _userLocalService.addUser(creatorUserId, companyId,
                    autoPassword, password1, password2, autoScreenName, screenName,
                    emailAddress, facebookId, openId, locale,
                    firstName, middleName,
                    lastName, prefixId, suffixId, male,
                    birthdayMonth, birthdayDay, birthdayYear,
                    jobTitle, groupIds, organizationIds,
                    roleIds, userGroupIds, sendEmail,
                    serviceContext);
            
            
        } catch (Exception e) {
            _log.error(e);
            user = null;
        }

        return user;
	}

	@Override
	public User updateUser(User userParam, 
			OauthAuthUserWrapper userWrapper) {

        User user;
        try {
            userParam.setFirstName(userWrapper.getFirstName());
            userParam.setLastName(userWrapper.getLastName());
            userParam.setJobTitle(userWrapper.getJobTitle());

            user = _userLocalService.updateUser(userParam);
        } catch (Exception e) {
            _log.error(e);
            user = null;
        }
        return user;
	}

	
	@Override
    public String getUserFromServer(String oauthServerUrl,
    		String accessToken) {


    	String urlBase = oauthServerUrl + "/api/jsonws/user/get-current-user";
    	
		
		Map<String, String> headers = new LinkedHashMap<String, String>();
		headers.put(HttpHeaders.AUTHORIZATION, OauthPortletKeys.BEARER + accessToken);

		return getJson(
			urlBase, null, headers);

	}
}
