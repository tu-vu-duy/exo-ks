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
package org.exoplatform.wiki.webui.popup;

import java.net.URLEncoder;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.wiki.resolver.TitleResolver;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.tree.UITreeExplorer;
import org.exoplatform.wiki.utils.Utils;
import org.exoplatform.wiki.webui.UIWikiPortlet;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Aug 2, 2010  
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class, 
  template = "app:/templates/wiki/webui/popup/UIWikiMovePageForm.gtmpl",
  events = {
    @EventConfig(listeners = UIWikiMovePageForm.CloseActionListener.class),
    @EventConfig(listeners = UIWikiMovePageForm.MoveActionListener.class)
    }
)
public class UIWikiMovePageForm extends UIForm implements UIPopupComponent {
  
  final static public String PAGENAME_INFO= "ppageNameInfo";
  
  final static public String CURRENT_LOCATION = "currentLocationInput";

  final static public String NEW_LOCATION     = "newLocationInput";

  final static public String UITREE           = "UITreeExplorer";

  public String              MOVE             = "Move";
  
  public UIWikiMovePageForm() throws Exception {
   addChild(new UIFormInputInfo(PAGENAME_INFO, PAGENAME_INFO, null));
   addChild(new UIFormStringInput(CURRENT_LOCATION, CURRENT_LOCATION, null).setEditable(false));   
   addChild(new UIFormStringInput(NEW_LOCATION, NEW_LOCATION, null));  
   addChild(UITreeExplorer.class, null, UITREE).setRendered(true);
  }
  
  static public class CloseActionListener extends EventListener<UIWikiMovePageForm> {
    public void execute(Event<UIWikiMovePageForm> event) throws Exception {  
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIPopupContainer popupContainer = wikiPortlet.getChild(UIPopupContainer.class);
      popupContainer.cancelPopupAction();    
    }
  }
  static public class MoveActionListener extends EventListener<UIWikiMovePageForm> {
    public void execute(Event<UIWikiMovePageForm> event) throws Exception {   
      WikiService wservice = (WikiService) PortalContainer.getComponent(WikiService.class);
      PortalRequestContext prContext = Util.getPortalRequestContext();
      UIWikiPortlet uiWikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIWikiMovePageForm movePageForm = uiWikiPortlet.findFirstComponentOfType(UIWikiMovePageForm.class);
      UIFormStringInput currentLocationInput = movePageForm.getUIStringInput(CURRENT_LOCATION);
      UIFormStringInput newLocationInput = movePageForm.getUIStringInput(NEW_LOCATION);
      WikiPageParams currentLocationParams = Utils.getPageParamsFromPath(currentLocationInput.getValue());
      currentLocationParams.setPageId(TitleResolver.getPageId(currentLocationParams.getPageId(), false));
      WikiPageParams newLocationParams = Utils.getPageParamsFromPath(newLocationInput.getValue());
      if ((newLocationParams==null)||(newLocationParams.getPageId() == null)||(currentLocationInput.getValue().equals(newLocationInput.getValue().trim()))) {
        uiWikiPortlet.addMessage(new ApplicationMessage("UIWikiMovePageForm.can-not-move-to-follow-path", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiWikiPortlet.getUIPopupMessages()) ;
        return;
      }
      newLocationParams.setPageId(TitleResolver.getPageId(newLocationParams.getPageId(), false));
      wservice.movePage(currentLocationParams, newLocationParams);
      String portalURI = prContext.getPortalURI();
      StringBuilder newPageURL = new StringBuilder(portalURI + "wiki/");
      String encodedPageId= URLEncoder.encode(currentLocationParams.getPageId(),"UTF-8");
      if (newLocationParams.getType().equals(PortalConfig.PORTAL_TYPE)) {
        newPageURL.append(encodedPageId);
      } else if (newLocationParams.getType().equals(PortalConfig.GROUP_TYPE)) {
        newPageURL.append(PortalConfig.GROUP_TYPE + "/" + newLocationParams.getOwner() + "/"
            + encodedPageId);
      } else if (newLocationParams.getType().equals(PortalConfig.USER_TYPE)) {
        newPageURL.append(PortalConfig.USER_TYPE + "/" + newLocationParams.getOwner() + "/"
            + encodedPageId);
      }
      UIPopupContainer popupContainer = uiWikiPortlet.getChild(UIPopupContainer.class); 
   
      popupContainer.cancelPopupAction();
      prContext.setResponseComplete(true);      
      prContext.sendRedirect(newPageURL.toString());
    }
  }
  public void activate() throws Exception {
    // TODO Auto-generated method stub
    
  }
  public void deActivate() throws Exception {
    // TODO Auto-generated method stub
    
  }
  
  
}
