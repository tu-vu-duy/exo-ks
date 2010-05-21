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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.webui.UIWikiPageEditForm;
import org.exoplatform.wiki.webui.UIWikiPageTitleControlArea;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.WikiMode;
import org.exoplatform.wiki.webui.control.filter.IsViewModeFilter;
import org.exoplatform.wiki.webui.control.listener.UIPageToolBarActionListener;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  events = {
    @EventConfig(listeners = AddPageActionComponent.AddPageActionListener.class)
  }
)
public class AddPageActionComponent extends UIComponent {
  
  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] { new IsViewModeFilter() });

  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }
  
  public static class AddPageActionListener extends UIPageToolBarActionListener<AddPageActionComponent> {
    @Override
    protected void processEvent(Event<AddPageActionComponent> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      Map<String, Object> uiExtensionContext = new HashMap<String, Object>();
      uiExtensionContext.put(UIWikiPortlet.class.getName(), wikiPortlet);
      processAddPageAction(uiExtensionContext);
      super.processEvent(event);
    }
  }

  public static void processAddPageAction(Map<String, Object> uiExtensionContext) {
    UIWikiPortlet wikiPortlet = (UIWikiPortlet) uiExtensionContext.get(UIWikiPortlet.class.getName());
    String pageId = (String) uiExtensionContext.get(WikiContext.PAGEID);
    UIWikiPageEditForm pageEditForm = wikiPortlet.findFirstComponentOfType(UIWikiPageEditForm.class);
    UIFormStringInput titleInput = pageEditForm.getChild(UIWikiPageTitleControlArea.class).getUIStringInput();
    UIFormTextAreaInput markupInput = pageEditForm.findComponentById(UIWikiPageEditForm.FIELD_CONTENT);
    titleInput.setValue("Untitle");
    titleInput.setEditable(true);
    markupInput.setValue("This is **sample content**");
    
    if(pageId != null && pageId.length() > 0){
      titleInput.setValue(pageId);
      titleInput.setEditable(false);
    }

    wikiPortlet.changeMode(WikiMode.NEW);
  }
  
}
