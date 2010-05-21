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

import java.util.ArrayList;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.core.api.content.ContentImpl;
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
  private PageList<ContentImpl> results_ = new ObjectPageList<ContentImpl>(new ArrayList<ContentImpl>(), 10);
  
  public void setResult(PageList<ContentImpl> results) {
    results_ = results ;
  }
  
  private PageList<ContentImpl> getResults() {
    return results_ ;
  }
  
  private Page getPage(String pageId) throws Exception {
    WikiService wservice = (WikiService)PortalContainer.getComponent(WikiService.class) ;
    Page page = wservice.getPageById("portal", "classic", pageId) ;
    return page ;
  }
}
