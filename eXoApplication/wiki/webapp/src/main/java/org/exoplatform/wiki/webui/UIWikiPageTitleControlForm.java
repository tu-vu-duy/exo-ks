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
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.wiki.webui.lifecycle.UIFormLifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * May 14, 2010  
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiPageTitleControlForm.gtmpl"
)
public class UIWikiPageTitleControlForm extends UIForm {

  public static final String FIELD_TITLEINFO   = "TitleInfo";
  public static final String FIELD_TITLEINPUT   = "TitleInput";
  
  public UIWikiPageTitleControlForm() {
    UIFormInputInfo titleInfo = new UIFormInputInfo(FIELD_TITLEINFO, FIELD_TITLEINFO, FIELD_TITLEINFO);
    titleInfo.setRendered(false);
    addUIFormInput(titleInfo);
    UIFormStringInput titleInput = new UIFormStringInput(FIELD_TITLEINPUT, FIELD_TITLEINPUT, FIELD_TITLEINPUT);
    titleInput.setRendered(false);
    addUIFormInput(titleInput);
  }
  
  public UIFormInputInfo getUIFormInputInfo(){
    return getUIFormInputInfo(FIELD_TITLEINFO);
  }
  
  public UIFormStringInput getUIStringInput(){
    return getUIStringInput(FIELD_TITLEINPUT);
  }
  
  public void toInfoMode(){
    getUIFormInputInfo(FIELD_TITLEINFO).setRendered(true);
    getUIStringInput(FIELD_TITLEINPUT).setRendered(false);
  }
  
  public void toInputMode(){
    getUIFormInputInfo(FIELD_TITLEINFO).setRendered(false);
    getUIStringInput(FIELD_TITLEINPUT).setRendered(true);
  }
  
}
