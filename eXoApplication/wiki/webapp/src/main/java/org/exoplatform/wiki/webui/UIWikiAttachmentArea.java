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

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiAttachmentArea.gtmpl",
  events = {
    @EventConfig(listeners = UIWikiAttachmentArea.DownloadAttachmentActionListener.class, phase = Phase.DECODE),
    @EventConfig(listeners = UIWikiAttachmentArea.RemoveAttachmentActionListener.class, phase = Phase.DECODE)
  }
)
public class UIWikiAttachmentArea extends UIForm {

  public UIWikiAttachmentArea() throws Exception {
    UIFormInputWithActions formInputWithActions = new UIFormInputWithActions("WikiAttachmentArea");
    formInputWithActions.addUIFormInput(new UIFormInputInfo("attachments", "attachments", null));
    formInputWithActions.setActionField("attachments", getAttachmentsList());
    addUIFormInput(formInputWithActions).setRendered(true);
  }

  private List<ActionData> getAttachmentsList() throws Exception {
    List<ActionData> attachments = new ArrayList<ActionData>();
    Page page = Utils.getCurrentWikiPage();
    for (AttachmentImpl attachdata : ((PageImpl) page).getAttachments()) {
      ActionData downloadAction = new ActionData();
      downloadAction.setActionListener("DownloadAttachment");
      downloadAction.setActionParameter(attachdata.getName());
      downloadAction.setActionType(ActionData.TYPE_LINK);
      downloadAction.setCssIconClass("AttachmentIcon ZipFileIcon");
      downloadAction.setActionName(attachdata.getName());
      downloadAction.setShowLabel(true);
      attachments.add(downloadAction);
      ActionData removeAction = new ActionData();
      removeAction.setActionListener("RemoveAttachment");
      removeAction.setActionName("RemoveAttachment");
      removeAction.setActionParameter(attachdata.getName());
      removeAction.setActionType(ActionData.TYPE_ICON);
      removeAction.setCssIconClass("RemoveFile");
      removeAction.setBreakLine(true);
      attachments.add(removeAction);
    }
    return attachments;
  }

  public void refreshAttachmentsList() throws Exception {
    getChild(UIFormInputWithActions.class).setActionField("attachments", getAttachmentsList());
  }

  static public class DownloadAttachmentActionListener extends EventListener<UIWikiAttachmentArea> {
    public void execute(Event<UIWikiAttachmentArea> event) throws Exception {
      String attId = event.getRequestContext().getRequestParameter(OBJECTID);
      Page page = Utils.getCurrentWikiPage();
      AttachmentImpl attach = ((PageImpl) page).getAttachment(attId);
      String downloadLink = attach.getDownloadURL();
      event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');");
    }
  }

  static public class RemoveAttachmentActionListener extends EventListener<UIWikiAttachmentArea> {
    public void execute(Event<UIWikiAttachmentArea> event) throws Exception {
      UIWikiAttachmentArea uiForm = event.getSource();
      String attFileId = event.getRequestContext().getRequestParameter(OBJECTID);
      Page page = Utils.getCurrentWikiPage();
      ((PageImpl) page).removeAttachment(attFileId);
      uiForm.refreshAttachmentsList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }
  
}
