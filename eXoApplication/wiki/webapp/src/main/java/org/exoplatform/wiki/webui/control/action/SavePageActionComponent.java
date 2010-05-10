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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.resolver.PageResolver;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiResource;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.webui.PageMode;
import org.exoplatform.wiki.webui.UIWikiPageContentArea;
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
    @EventConfig(listeners = SavePageActionComponent.SavePageActionListener.class)
  }
)
public class SavePageActionComponent extends UIComponent {

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
      UIWikiPageContentArea pageContentArea = wikiPortlet.findFirstComponentOfType(UIWikiPageContentArea.class);
      UIFormTextAreaInput titleInput = pageContentArea.findComponentById(UIWikiPageContentArea.FIELD_TITLE);
      UIFormTextAreaInput markupInput = pageContentArea.findComponentById(UIWikiPageContentArea.FIELD_CONTENT);
      String title = titleInput.getValue();
      String markup = markupInput.getValue();
      try {
        String requestURL = Utils.getCurrentRequestURL();
        PageResolver pageResolver = (PageResolver) PortalContainer.getComponent(PageResolver.class);
        Page page = pageResolver.resolve(requestURL);
        if (pageContentArea.getPageMode() == PageMode.EXISTED) {
          page.getContent().setText(markup);
          for(WikiResource file :  pageContentArea.getAttachments()){
            AttachmentImpl att = ((PageImpl)page).createAttachment(file.getName(), file) ;
            Utils.reparePermissions(att) ;
          }
        } else if (pageContentArea.getPageMode() == PageMode.NEW) {
          WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class);
          WikiPageParams pageParams = pageResolver.extractWikiPageParams(requestURL);
          Page subPage = wikiService.createPage(pageParams.getType(), pageParams.getOwner(), title, page.getPageId());
          subPage.getContent().setText(markup);
          for(WikiResource file :  pageContentArea.getAttachments()){
            AttachmentImpl att = ((PageImpl)subPage).createAttachment(file.getName(), file) ;
            Utils.reparePermissions(att) ;            
          }
          /*String redirect = createUrlOfNewPage();
         prContext.getResponse().sendRedirect(redirect);*/
        }
        pageContentArea.renderWikiMarkup(markup);
      } catch (Exception e) {
        log.error("An exception happens when saving the page with title:" + title, e);
        uiApp.addMessage(new ApplicationMessage("UIPageToolBar.msg.Exception", null, ApplicationMessage.ERROR));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      
      wikiPortlet.setWikiMode(WikiMode.VIEW);
      pageContentArea.setAttachments(new ArrayList<WikiResource>());
      pageContentArea.removeChildById(UIWikiPageContentArea.FIELD_TITLE);
      pageContentArea.removeChildById(UIWikiPageContentArea.FIELD_CONTENT);
      super.processEvent(event);
    }
  }
}
