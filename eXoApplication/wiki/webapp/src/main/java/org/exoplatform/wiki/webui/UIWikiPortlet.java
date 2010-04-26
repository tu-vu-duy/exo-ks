/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wiki.webui;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.rendering.MarkupRenderingService;
import org.exoplatform.wiki.rendering.xwiki.XWikiRenderer;
import org.exoplatform.wiki.resolver.PageResolver;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.service.WikiPageParams;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Nov
 * 5, 2009
 */

@ComponentConfig(lifecycle = UIApplicationLifecycle.class, template = "app:/templates/wiki/webui/UIWikiPortlet.gtmpl")
public class UIWikiPortlet extends UIPortletApplication {
  public UIWikiPortlet() throws Exception {
    super();
    try {
      addChild(UIWikiUpperArea.class, null, null).setRendered(true);
      addChild(UIWikiPageArea.class, null, null).setRendered(true);
      addChild(UIWikiBottomArea.class, null, null).setRendered(true);
    } catch (Exception e) {
      log.error("An exception happens when init WikiPortlet", e);
    }
  }

  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    UIPortal uiPortal = Util.getUIPortal();
    String portalURI = portalRequestContext.getPortalURI();
    String requestURL = Utils.getCurrentRequestURL();
    String pageNodeSelected = uiPortal.getSelectedNode().getUri();
    PageResolver pageResolver = (PageResolver) PortalContainer.getComponent(PageResolver.class);
    try {
      // TODO: ignore request URL of resources
      Page page = pageResolver.resolve(requestURL);
      context.setAttribute("wikiPage", page);
      findFirstComponentOfType(UIPageForm.class).getChild(UIFormTextAreaInput.class).setValue(page.getContent().getText());
      
      MarkupRenderingService service = (MarkupRenderingService) PortalContainer.getComponent(MarkupRenderingService.class);
      XWikiRenderer renderer = (XWikiRenderer) service.getRenderer("xwiki");
      Execution ec = renderer.getExecutionContext();
      ec.setContext(new ExecutionContext());
      WikiContext wikiContext = new WikiContext();
      wikiContext.setPortalURI(portalURI);
      wikiContext.setPortletURI(pageNodeSelected);
      WikiPageParams params = pageResolver.extractWikiPageParams(requestURL);
      wikiContext.setType(params.getType());
      wikiContext.setOwner(params.getOwner());
      wikiContext.setPageId(params.getPageId());
      ec.getContext().setProperty("wikicontext", wikiContext);

      String output = findFirstComponentOfType(UIPageForm.class).renderWikiMarkup(page.getContent().getText());
      findFirstComponentOfType(UIWikiPageContentArea.class).setHtmlOutput(output);
    } catch (Exception e) {
      context.setAttribute("wikiPage", null);
      if (log.isWarnEnabled()) {
        log.warn("An exception happens when resolving URL: " + requestURL, e);
      }
    }

    // if(portletReqContext.getApplicationMode() == PortletMode.VIEW) {
    // Check and remove edit component
    // add a component that has template is View mode HTML
    // addChild(UIFAQContainer.class, null, null) ;

    // }else if(portletReqContext.getApplicationMode() == PortletMode.EDIT) {

    // remove view component
    // Add edit component
    // UISettingForm settingForm = addChild(UISettingForm.class, null,
    // "FAQPortletSetting");
    // settingForm.setRendered(true);

    // NOTE: add this property to portlet.xml file under <supports> tag
    // <portlet-mode>edit</portlet-mode>

    // }

    super.processRender(app, context);
  }

}
