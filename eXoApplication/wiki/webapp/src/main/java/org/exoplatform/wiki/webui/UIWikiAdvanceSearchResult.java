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
package org.exoplatform.wiki.webui;

import java.util.List;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.service.SearchResult;
import org.exoplatform.wiki.service.WikiService;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * May 14, 2010  
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiAdvanceSearchResult.gtmpl",
  events = {
      @EventConfig(listeners = UIWikiAdvanceSearchResult.DownloadAttachActionListener.class),
      @EventConfig(listeners = UIWikiAdvanceSearchResult.ViewPageActionListener.class)
  }    
)
public class UIWikiAdvanceSearchResult extends UIContainer {
  private PageList<SearchResult> results_ ;
  private String keyword ;
  
  public void setResult(PageList<SearchResult> results) {
    results_ = results ;
  }
  
  private PageList<SearchResult> getResults() {
    return results_ ;
  }
  
  private List<SearchResult> getPage(int i) throws Exception {
    return results_.getPage(i) ;
  }
  
  public void setKeyword(String keyword) { this.keyword = keyword ;}
  
  private String getKeyword () {return keyword ;}
  
  private String getCurrentWiki() throws Exception {
    return Utils.getCurrentWikiPageParams().getType();
  }
  
  private Object getObject(String path, String type) throws Exception {
    WikiService wservice = (WikiService)PortalContainer.getComponent(WikiService.class) ;
    return wservice.findByPath(path, type) ;    
  }
  
  private String getPageTitle(String path) throws Exception {
    WikiService wservice = (WikiService)PortalContainer.getComponent(WikiService.class) ;
    return wservice.getPageTitleOfAttachment(path) ;    
  }
  
  static public class DownloadAttachActionListener extends EventListener<UIWikiAdvanceSearchResult> {
    public void execute(Event<UIWikiAdvanceSearchResult> event) throws Exception {
      String params = event.getRequestContext().getRequestParameter(OBJECTID);
      String path = params.substring(0, params.lastIndexOf("/")) ;
      String fileName = params.substring(params.lastIndexOf("/") + 1) ;
      String downloadLink = Utils.getDownloadLink(path, fileName, null) ;
      event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');");
    }
  }
  static public class ViewPageActionListener extends EventListener<UIWikiAdvanceSearchResult> {
    @Override
    public void execute(Event<UIWikiAdvanceSearchResult> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      wikiPortlet.changeMode(WikiMode.VIEW);
    }
  }
}
