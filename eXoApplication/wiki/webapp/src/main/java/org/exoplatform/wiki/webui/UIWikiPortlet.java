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

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.ext.UIExtensionManager;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.rendering.impl.RenderingServiceImpl;
import org.exoplatform.wiki.resolver.PageResolver;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.webui.control.UIPageToolBar;
import org.exoplatform.wiki.webui.control.action.AddPageActionComponent;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Nov
 * 5, 2009
 */

@ComponentConfig(lifecycle = UIApplicationLifecycle.class, template = "app:/templates/wiki/webui/UIWikiPortlet.gtmpl")
public class UIWikiPortlet extends UIPortletApplication {
  
  private WikiMode mode = WikiMode.VIEW;
  
  public UIWikiPortlet() throws Exception {
    super();
    try {
      UIPopupContainer uiPopupContainer = addChild(UIPopupContainer.class, null, null) ;
      uiPopupContainer.setId("UIWikiPopupContainer") ;
      uiPopupContainer.getChild(UIPopupWindow.class).setId("UIWikiPopupWindow") ;
      
      addChild(UIWikiUpperArea.class, null, null).setRendered(true);
      addChild(UIWikiPageArea.class, null, null).setRendered(true);
      addChild(UIWikiBottomArea.class, null, null).setRendered(true);
      addChild(UIWikiSearchSpaceArea.class, null, null).setRendered(false);
      addChild(UIWikiMaskWorkspace.class, null, "UIWikiMaskWorkspace");
    } catch (Exception e) {
      log.error("An exception happens when init WikiPortlet", e);
    }
  }

  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    WikiPageParams pageParams = Utils.getCurrentWikiPageParams();
    if (WikiContext.ADDPAGE.equalsIgnoreCase(pageParams.getParameter(WikiContext.ACTION))) {
      UIExtensionManager manager = getApplicationComponent(UIExtensionManager.class);
      Map<String, Object> uiExtensionContext = new HashMap<String, Object>();
      uiExtensionContext.put(UIWikiPortlet.class.getName(), this);
      uiExtensionContext.put(WikiContext.PAGETITLE, pageParams.getParameter(WikiContext.PAGETITLE));
      if(manager.accept(UIPageToolBar.EXTENSION_TYPE, WikiContext.ADDPAGE, uiExtensionContext)){
        AddPageActionComponent.processAddPageAction(uiExtensionContext);
      }
    }
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
      
      RenderingServiceImpl renderingService = (RenderingServiceImpl) PortalContainer.getComponent(RenderingService.class);
      Execution ec = renderingService.getExecutionContext();
      ec.setContext(new ExecutionContext());
      WikiContext wikiContext = new WikiContext();
      wikiContext.setPortalURI(portalURI);
      wikiContext.setPortletURI(pageNodeSelected);
      WikiPageParams params = pageResolver.extractWikiPageParams(requestURL);
      wikiContext.setType(params.getType());
      wikiContext.setOwner(params.getOwner());
      wikiContext.setPageId(params.getPageId());
      ec.getContext().setProperty(WikiContext.WIKICONTEXT, wikiContext);
      
      ((UIWikiPageTitleControlArea)findComponentById(UIWikiPageControlArea.TITLE_CONTROL)).getUIFormInputInfo().setValue(page.getContent().getTitle());
      findFirstComponentOfType(UIWikiPageContentArea.class).renderWikiMarkup(page.getContent().getText(), page.getContent().getSyntax());
      UIWikiBreadCrumb wikiBreadCrumb = findFirstComponentOfType(UIWikiBreadCrumb.class);
      WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class);
      wikiBreadCrumb.setBreadCumbs(wikiService.getBreadcumb(params.getType(), params.getOwner(), page.getName()));
    } catch (Exception e) {
      context.setAttribute("wikiPage", null);
      findFirstComponentOfType(UIWikiPageContentArea.class).setHtmlOutput(null);
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
  
  public WikiMode getWikiMode(){
    return mode;
  }
  
  public void changeMode(WikiMode newMode){
    WikiMode oldMode = mode;
    mode = newMode;
    switch(oldMode){
      case VIEW:
        switch(mode){
          case EDIT:
            switchViewEditMode(true);
            break;
          case NEW:
            switchViewNewMode(true);
            break;
          case SEARCH:
            switchViewSearchMode(true);
            break;
        }
        break;
      case EDIT:
        switch(mode){
          case VIEW:
            switchViewEditMode(false);
            break;
          
        }
        break;
      case NEW:
        switch(mode){
          case VIEW:
            switchViewNewMode(false);
            break;
        }
        break;
      case SEARCH:
        switch(mode){
          case VIEW:
            switchViewSearchMode(false);
            break;
        }
        break;
    }
  }
  
  private void switchViewEditMode(boolean isViewToEdit){
    findFirstComponentOfType(UIWikiPageControlArea.class).setRendered(!isViewToEdit);
    findFirstComponentOfType(UIWikiPageContentArea.class).setRendered(!isViewToEdit);
    UIWikiPageEditForm wikiPageEditForm = findFirstComponentOfType(UIWikiPageEditForm.class).setRendered(isViewToEdit);
    if(!isViewToEdit){
      wikiPageEditForm.getChild(UIWikiSidePanelArea.class).setRendered(isViewToEdit);
    }
  }
  
  private void switchViewNewMode(boolean isViewToNew){
    findFirstComponentOfType(UIWikiPageControlArea.class).setRendered(!isViewToNew);
    findFirstComponentOfType(UIWikiPageContentArea.class).setRendered(!isViewToNew);
    UIWikiPageEditForm wikiPageEditForm = findFirstComponentOfType(UIWikiPageEditForm.class).setRendered(isViewToNew);
    findFirstComponentOfType(UIWikiBottomArea.class).setRendered(!isViewToNew);
    if(!isViewToNew){
      wikiPageEditForm.getChild(UIWikiSidePanelArea.class).setRendered(isViewToNew);
    }
  }
  
  private void switchViewSearchMode(boolean isViewToSearch){
    findFirstComponentOfType(UIWikiPageControlArea.class).setRendered(!isViewToSearch);
    findFirstComponentOfType(UIWikiPageArea.class).setRendered(!isViewToSearch);
    findFirstComponentOfType(UIWikiBottomArea.class).setRendered(!isViewToSearch);
    findFirstComponentOfType(UIWikiSearchSpaceArea.class).setRendered(isViewToSearch);
  }
  
}
