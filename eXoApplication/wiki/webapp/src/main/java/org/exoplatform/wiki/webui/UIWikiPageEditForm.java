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
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.wiki.webui.control.UIPageToolBar;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * May 14, 2010  
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiPageEditForm.gtmpl"
)
public class UIWikiPageEditForm extends UIForm {

  public static final String FIELD_TITLE   = "Title";
  public static final String FIELD_CONTENT = "Markup";
  
  public UIWikiPageEditForm() throws Exception{
    addChild(UIPageToolBar.class, null, "UIWikiPageEditForm_PageToolBar").setRendered(true);
    UIFormTextAreaInput titleInput = new UIFormTextAreaInput(UIWikiPageContentArea.FIELD_TITLE,
                                                             UIWikiPageContentArea.FIELD_TITLE,
                                                             "Untitle");
    UIFormTextAreaInput markupInput = new UIFormTextAreaInput(UIWikiPageContentArea.FIELD_CONTENT,
                                                              UIWikiPageContentArea.FIELD_CONTENT,
                                                              "This is **sample content**");
    addUIFormInput(titleInput).setRendered(true);
    addUIFormInput(markupInput).setRendered(true);
  }
  
}
