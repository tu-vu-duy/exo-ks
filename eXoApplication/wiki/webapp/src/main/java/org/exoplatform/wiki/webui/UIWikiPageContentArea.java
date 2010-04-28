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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.wiki.rendering.MarkupRenderingService;
import org.exoplatform.wiki.rendering.Renderer;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiPageContentArea.gtmpl",
  events = {
      @EventConfig(listeners = UIWikiPageContentArea.SubmitActionListener.class)
    }
)
public class UIWikiPageContentArea extends UIForm {

  private String htmlOutput;
  
  public UIWikiPageContentArea(){
    setActions(new String[] { "Submit" });
  }
  
  public String getHtmlOutput() {
    return htmlOutput;
  }

  public void setHtmlOutput(String output) {
    this.htmlOutput = output;
  }
  
  public String renderWikiMarkup(String markup) throws Exception {
    MarkupRenderingService renderingService = (MarkupRenderingService) PortalContainer.getComponent(MarkupRenderingService.class);
    Renderer xwikiRenderer = renderingService.getRenderer("xwiki");
    String output = xwikiRenderer.render(markup);
    return output;
  }
  
  public WikiMode getWikiMode(){
    return getAncestorOfType(UIWikiPortlet.class).getWikiMode();
  }
  
  static public class SubmitActionListener extends EventListener<UIWikiPageContentArea> {
    @Override
    public void execute(Event<UIWikiPageContentArea> event) throws Exception {
    }
    
  }
}
