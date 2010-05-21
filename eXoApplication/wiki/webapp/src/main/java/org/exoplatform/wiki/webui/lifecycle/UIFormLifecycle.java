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
package org.exoplatform.wiki.webui.lifecycle;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputBase;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * May 21, 2010  
 */
public class UIFormLifecycle extends org.exoplatform.webui.core.lifecycle.UIFormLifecycle {

  public void processDecode(UIForm uicomponent, WebuiRequestContext context) throws Exception
  {
     //    HttpServletRequest httpRequest = (HttpServletRequest)context.getRequest() ;
     uicomponent.setSubmitAction(null);
     //    if(ServletFileUpload.isMultipartContent(new ServletRequestContext(httpRequest))) {
     //      processMultipartRequest(uiForm, context) ;
     //    } else {
     processNormalRequest(uicomponent, context);
     //    }
     List<UIComponent> children = uicomponent.getChildren();
     for (UIComponent uiChild : children)
     {
        uiChild.processDecode(context);
     }
     String action = uicomponent.getSubmitAction();
     String subComponentId = context.getRequestParameter(UIForm.SUBCOMPONENT_ID);
     if (subComponentId == null || subComponentId.trim().length() < 1)
     {
        Event<UIComponent> event = uicomponent.createEvent(action, Event.Phase.DECODE, context);
        if (event != null)
        {
           event.broadcast();
        }
        return;
     }
     UIComponent uiSubComponent = uicomponent.findComponentById(subComponentId);
     Event<UIComponent> event = null;
     if(uiSubComponent != null){
       event = uiSubComponent.createEvent(action, Event.Phase.DECODE, context);
     }
     if (event == null)
     {
        event = uicomponent.createEvent(action, Event.Phase.DECODE, context);
     }
     if (event != null)
     {
        event.broadcast();
     }
  }
  
  private void processNormalRequest(UIForm uiForm, WebuiRequestContext context) throws Exception
  {
     List<UIFormInputBase> inputs = new ArrayList<UIFormInputBase>();
     uiForm.findComponentOfType(inputs, UIFormInputBase.class);
     uiForm.setSubmitAction(context.getRequestParameter(UIForm.ACTION));
     for (UIFormInputBase input : inputs)
     {
        if (!input.isValid())
        {
           continue;
        }
        String inputValue = context.getRequestParameter(input.getId());
        if (inputValue == null || inputValue.trim().length() == 0)
        {
           inputValue = context.getRequestParameter(input.getName());
        }
        input.decode(inputValue, context);
     }
  }
  
}
