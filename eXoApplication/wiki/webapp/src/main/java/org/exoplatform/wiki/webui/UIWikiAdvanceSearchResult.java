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

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.wiki.commons.Utils;
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
  template = "app:/templates/wiki/webui/UIWikiAdvanceSearchResult.gtmpl"
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
}
