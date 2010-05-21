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
import java.util.List;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.core.api.content.ContentImpl;
import org.exoplatform.wiki.service.SearchData;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * May 14, 2010  
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiAdvanceSearchForm.gtmpl",
  events = {
      @EventConfig(listeners = UIWikiAdvanceSearchForm.SearchActionListener.class)     
            
    }
)

public class UIWikiAdvanceSearchForm extends UIForm {
  final static String TEXT = "text".intern() ;
  final static String WIKI_SPACES = "wikiSpaces".intern() ;
  
  
  public UIWikiAdvanceSearchForm() throws Exception {
    addChild(new UIFormStringInput(TEXT, TEXT, null)) ;
    List<SelectItemOption<String>> spaces = new ArrayList<SelectItemOption<String>>() ;
    spaces.add(new SelectItemOption<String>("current", "current")) ;
    UIFormSelectBox selectSpaces = new UIFormSelectBox(WIKI_SPACES, WIKI_SPACES, spaces);
    addChild(selectSpaces) ;
    this.setActions(new String[]{"Search"});
  }
  
  
  static public class SearchActionListener extends EventListener<UIWikiAdvanceSearchForm> {
    public void execute(Event<UIWikiAdvanceSearchForm> event) throws Exception {
      UIWikiAdvanceSearchForm uiSearch = event.getSource() ;
      String text = uiSearch.getUIStringInput(TEXT).getValue() ;
      String space = uiSearch.getUIFormSelectBox(WIKI_SPACES).getValue() ;
      WikiPageParams  params = Utils.getCurrentWikiPageParams() ;      
      WikiService wservice = (WikiService)PortalContainer.getComponent(WikiService.class) ;
      SearchData data = new SearchData(text, null, null, null) ;
      PageList<ContentImpl> results = wservice.searchContent(params.getType(), params.getOwner(), data) ;
      UIWikiAdvanceSearchResult uiSearchResults = uiSearch.getParent().findFirstComponentOfType(UIWikiAdvanceSearchResult.class) ;
      uiSearchResults.setResult(results) ;   
      
    }
  }  
}
