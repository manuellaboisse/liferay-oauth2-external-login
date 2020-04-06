package com.minsait.auth.autologin;

import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.LifecycleAction;
import com.liferay.portal.kernel.events.LifecycleEvent;
import com.liferay.portal.kernel.struts.LastPath;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PropsKeys;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author mlaboisse
 */
@Component(
        immediate = true,
        property = { "key="+PropsKeys.LOGIN_EVENTS_POST },
        service = LifecycleAction.class
)
public class OauthLifecycleAction implements LifecycleAction {
	
    @Reference
	private Portal _portal;
    
	@Override
	public void processLifecycleEvent(LifecycleEvent lifecycleEvent) throws ActionException {
		HttpSession session = lifecycleEvent.getRequest().getSession();

		//Redireccionamos a path configurado tras login /group/guest
		String lastPath = ((LastPath)session.getAttribute("LAST_PATH")).getPath();
		try {
			lifecycleEvent.getResponse().sendRedirect(lastPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
