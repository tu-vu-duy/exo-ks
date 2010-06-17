/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.wiki.webui.control.action;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.resolver.PageResolver;
import org.exoplatform.wiki.resolver.TitleResolver;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.webui.UIWikiPageContentArea;
import org.exoplatform.wiki.webui.UIWikiPageControlArea;
import org.exoplatform.wiki.webui.UIWikiPageEditForm;
import org.exoplatform.wiki.webui.UIWikiPageTitleControlArea;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.WikiMode;
import org.exoplatform.wiki.webui.control.filter.IsEditModeFilter;
import org.exoplatform.wiki.webui.control.listener.UIPageToolBarActionListener;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  events = {
    @EventConfig(listeners = SavePageActionComponent.SavePageActionListener.class, phase = Phase.DECODE)
  }
)
public class SavePageActionComponent extends UIComponent {

  public static final String ACTION = "SavePage";
  
  private static final Log log = ExoLogger.getLogger("wiki:SavePageActionComponent");
  
  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] { new IsEditModeFilter() });

  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }
  
  public static class SavePageActionListener extends UIPageToolBarActionListener<SavePageActionComponent> {
    @Override
    protected void processEvent(Event<SavePageActionComponent> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class);
      UIWikiPageTitleControlArea pageTitleControlForm = wikiPortlet.findComponentById(UIWikiPageControlArea.TITLE_CONTROL);
      UIWikiPageContentArea pageContentArea = wikiPortlet.findFirstComponentOfType(UIWikiPageContentArea.class);
      UIWikiPageEditForm pageEditForm = wikiPortlet.findFirstComponentOfType(UIWikiPageEditForm.class);
      UIFormStringInput titleInput = pageEditForm.getChild(UIWikiPageTitleControlArea.class).getUIStringInput();
      UIFormTextAreaInput markupInput = pageEditForm.findComponentById(UIWikiPageEditForm.FIELD_CONTENT);
      UIFormSelectBox syntaxTypeSelectBox = pageEditForm.findComponentById(UIWikiPageEditForm.FIELD_SYNTAX);
      
      String title = titleInput.getValue();
      String markup = markupInput.getValue();
      try {
        String requestURL = Utils.getCurrentRequestURL();
        PageResolver pageResolver = (PageResolver) PortalContainer.getComponent(PageResolver.class);
        WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class);
        WikiPageParams pageParams = pageResolver.extractWikiPageParams(requestURL);
        Page page = pageResolver.resolve(requestURL);
        if (wikiPortlet.getWikiMode() == WikiMode.EDIT) {
          page.getContent().setText(markup);
          page.getContent().setSyntax(syntaxTypeSelectBox.getValue());
          pageTitleControlForm.getUIFormInputInfo().setValue(title);
          pageContentArea.renderWikiMarkup(markup, syntaxTypeSelectBox.getValue());
          
          if(!pageEditForm.getTitle().equals(title)) {
            page.getContent().setTitle(title);
            String newPageId = TitleResolver.getPageId(title, false) ;
            wikiService.renamePage(pageParams.getType(), pageParams.getOwner(), page.getName(), newPageId, title) ;
            pageParams.setPageId(newPageId) ;
            event.getSource().redirectToNewPage(pageParams, URLEncoder.encode(newPageId, "UTF-8"));            
          }
                    
        } else if (wikiPortlet.getWikiMode() == WikiMode.NEW) {          
          
          Page subPage = wikiService.createPage(pageParams.getType(), pageParams.getOwner(), title, page.getName());
          subPage.getContent().setText(markup);
          subPage.getContent().setSyntax(syntaxTypeSelectBox.getValue());
          
          wikiPortlet.changeMode(WikiMode.VIEW);
          String pageId = TitleResolver.getPageId(title, false);          
          event.getSource().redirectToNewPage(pageParams, URLEncoder.encode(pageId, "UTF-8"));
          return;
        }
        
      } catch (Exception e) {
        log.error("An exception happens when saving the page with title:" + title, e);
        uiApp.addMessage(new ApplicationMessage("UIPageToolBar.msg.Exception", null, ApplicationMessage.ERROR));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      
      wikiPortlet.changeMode(WikiMode.VIEW);
      super.processEvent(event);
    }
  }
  
  private void redirectToNewPage(WikiPageParams currentPageParams, String newPageId) throws Exception {
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    String portalURI = portalRequestContext.getPortalURI();
    UIPortal uiPortal = Util.getUIPortal();
    String pageNodeSelected = uiPortal.getSelectedNode().getUri();
    StringBuilder sb = new StringBuilder();
    sb.append(portalURI);
    sb.append(pageNodeSelected);
    sb.append("/");
    if(!PortalConfig.PORTAL_TYPE.equalsIgnoreCase(currentPageParams.getType())){
      sb.append(currentPageParams.getType().toLowerCase());
      sb.append("/");
      sb.append(org.exoplatform.wiki.utils.Utils.validateWikiOwner(currentPageParams.getType(), currentPageParams.getOwner()));
      sb.append("/");
    }
    sb.append(newPageId);
    System.out.println("sb.toString() ==>" + sb.toString());
    portalRequestContext.sendRedirect(sb.toString());
  }
  
}
