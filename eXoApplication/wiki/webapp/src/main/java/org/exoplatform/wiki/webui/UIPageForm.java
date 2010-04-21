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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.wiki.commons.URLResolver;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.rendering.MarkupRenderingService;
import org.exoplatform.wiki.rendering.Renderer;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Nov
 * 5, 2009
 */

@ComponentConfig(lifecycle = UIFormLifecycle.class,
                 template = "app:/templates/wiki/webui/UIPageForm.gtmpl",
                 events = {
                   @EventConfig(listeners = UIPageForm.RenderActionListener.class)
                   }
                 )
public class UIPageForm extends UIForm {

  private Page page;
  
  public UIPageForm() {
    UIFormTextAreaInput markupInput = new UIFormTextAreaInput("Markup",
                                                              "Markup",
                                                              "This is **bold**");
    addUIFormInput(markupInput).setRendered(true);
    setActions(new String[] { "Render" });
  }

  static public class RenderActionListener extends EventListener<UIPageForm> {

    public void execute(Event<UIPageForm> event) throws Exception {
      UIPageForm thisForm = event.getSource();

      UIFormTextAreaInput markupInput = thisForm.findComponentById("Markup");
      String markup = markupInput.getValue();

      if(thisForm.getPage() != null){
        thisForm.getPage().getContent().setText(markup);
        //TODO: study to call ChromatticSession.save() instead of following code
        PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
        HttpServletRequest request = portalRequestContext.getRequest();
        HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(request);
        String requestURL = requestWrapper.getRequestURL().toString();
        URLResolver urlResolver = new URLResolver();
        WikiPageParams params = urlResolver.extractPageParams(requestURL);
        WikiService wikiService = (WikiService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(WikiService.class);
        wikiService.updatePage(params.getType(), params.getOwner(), thisForm.getPage());
      }
      
      String output = thisForm.renderWikiMarkup(markup);

      UIWikiPortlet parent = thisForm.getParent();
      parent.setHtmlOutput(output);
      event.getRequestContext().addUIComponentToUpdateByAjax(parent);

    }

  }

  /*
   * public String getWikiPage() { String link = null; PortalRequestContext
   * portalRequestContext = Util.getPortalRequestContext();
   * PortletRequestContext portletRequestContext =
   * WebuiRequestContext.getCurrentInstance(); String portalURI =
   * portalRequestContext.getPortalURI(); portalRequestContext.
   * PortletPreferences portletPreferences = getPortletPreferences(); String
   * repository = portletPreferences.getValue(UICLVPortlet.REPOSITORY, null);
   * String workspace = portletPreferences.getValue(UICLVPortlet.WORKSPACE,
   * null); String baseURI = portletRequestContext.getRequest().getScheme() +
   * "://" + portletRequestContext.getRequest().getServerName() + ":" +
   * String.format("%s", portletRequestContext.getRequest().getServerPort());
   * String basePath = portletPreferences.getValue(UICLVPortlet.BASE_PATH,
   * null); link = baseURI + portalURI + basePath + "/" + repository + "/" +
   * workspace + node.getPath(); return link; }
   */

  public String renderWikiMarkup(String markup) throws Exception {
    MarkupRenderingService renderingService = (MarkupRenderingService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(MarkupRenderingService.class);
    Renderer xwikiRenderer = renderingService.getRenderer("xwiki");
    String output = xwikiRenderer.render(markup);
    return output;
  }

  public void setPage(Page page){
    this.page = page;
  }
  
  public Page getPage(){
    return this.page;
  }
  
  private MarkupRenderingService getMarkupRenderingService() {

    return new MarkupRenderingService(); // TODO: replace with
    // getApplicationComponent(MarkupRenderingService.class);
  }

}
