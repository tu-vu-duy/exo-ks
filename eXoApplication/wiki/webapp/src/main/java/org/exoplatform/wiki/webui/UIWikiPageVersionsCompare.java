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

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jul 13, 2010  
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiPageVersionsCompare.gtmpl",
  events = {
    @EventConfig(listeners = UIWikiPageVersionsCompare.ReturnVersionsListActionListener.class)
  }
)
public class UIWikiPageVersionsCompare extends UIContainer {

  private String differencesAsHTML;
  
  public static final String RETURN_VERSIONS_LIST = "ReturnVersionsList";

  public String getDifferencesAsHTML() {
    return differencesAsHTML;
  }

  public void setDifferencesAsHTML(String differencesAsHTML) {
    this.differencesAsHTML = differencesAsHTML;
  }
  
  static public class ReturnVersionsListActionListener extends EventListener<UIWikiPageVersionsCompare> {
    @Override
    public void execute(Event<UIWikiPageVersionsCompare> event) throws Exception {
      UIWikiPageVersionsCompare pageVersionsCompare = event.getSource();
      pageVersionsCompare.setRendered(false);
      UIWikiPageVersionsList pageVersionsList = ((UIWikiHistorySpaceArea) pageVersionsCompare.getParent()).getChild(UIWikiPageVersionsList.class);
      pageVersionsList.setRendered(true);
    }
  }
  
}
