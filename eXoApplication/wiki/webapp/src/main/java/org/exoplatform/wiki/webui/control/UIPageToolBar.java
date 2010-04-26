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
package org.exoplatform.wiki.webui.control;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.ext.UIExtension;
import org.exoplatform.webui.ext.UIExtensionManager;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/templates/wiki/webui/UIPageToolBar.gtmpl"
)
public class UIPageToolBar extends UIContainer {

  private static final String EXTENSION_TYPE = "org.exoplatform.wiki.UIPageToolBar";
  
  public Map<String, UIComponent> getActions() throws Exception {
    Map<String, UIComponent> activeActions = new ConcurrentHashMap<String, UIComponent>();
    UIExtensionManager manager = getApplicationComponent(UIExtensionManager.class);
    List<UIExtension> extensions = manager.getUIExtensions(EXTENSION_TYPE);
    if (extensions != null) {
      for (UIExtension extension : extensions) {
        UIComponent component = manager.addUIExtension(extension, null, this);
        if (component != null) {
          activeActions.put(extension.getName(), component);
        }
      }
    }
    return activeActions;
  }
  
}
