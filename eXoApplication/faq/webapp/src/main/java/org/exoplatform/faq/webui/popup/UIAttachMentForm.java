/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.faq.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIAnswersPortlet;
import org.exoplatform.faq.webui.UIWatchContainer;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormUploadInput;

/**
 * Created by The eXo Platform SAS
 * Author : Mai Van Ha
 *          ha_mai_van@exoplatform.com
 * Apr 29, 2008 ,9:41:42 AM 
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/templates/faq/webui/popup/UIAttachMentForm.gtmpl",
    events = {
      @EventConfig(listeners = UIAttachMentForm.SaveActionListener.class), 
      @EventConfig(listeners = UIAttachMentForm.CancelActionListener.class)
    }
)

public class UIAttachMentForm extends UIForm implements UIPopupComponent {
  private static int numberUpload = 5 ;
  private static final String FILE_UPLOAD = "FileUpload" ;
  private boolean isChangeAvatar = false;
  
  public void setIsChangeAvatar(boolean changeAvatar){
  	this.isChangeAvatar = changeAvatar;
  }
  
  public void setNumberUpload(int number){
  	numberUpload = number;
  	int sizeLimit = FAQUtils.getLimitUploadSize() ;
  	for(int i = 0 ; i < numberUpload; i ++) {
      if(sizeLimit >= 0) addChild(new UIFormUploadInput(FILE_UPLOAD + i, FILE_UPLOAD + i, sizeLimit)) ;
      else addChild(new UIFormUploadInput(FILE_UPLOAD + i, FILE_UPLOAD + i)) ;
    }
  }
  
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  public UIAttachMentForm() {
    this.setRendered(false) ;
  }
  
  static public class SaveActionListener extends EventListener<UIAttachMentForm> {
    @SuppressWarnings("static-access")
    public void execute(Event<UIAttachMentForm> event) throws Exception {
      UIAttachMentForm attachMentForm = event.getSource() ;
			UploadService uploadService = attachMentForm.getApplicationComponent(UploadService.class) ;

      List<FileAttachment> listFileAttachment = new ArrayList<FileAttachment>() ;
      long fileSize = 0 ;
      for(int i = 0 ; i < attachMentForm.numberUpload; i ++) {
        UIFormUploadInput uploadInput = attachMentForm.getChildById(FILE_UPLOAD + i) ;
        UploadResource uploadResource = uploadInput.getUploadResource() ;
        
        if(uploadResource == null) {
        	continue;
        }
        
        if(uploadResource != null && uploadResource.getUploadedSize() > 0) {
          FileAttachment fileAttachment = new FileAttachment() ;
          fileAttachment.setName(uploadResource.getFileName()) ;
          fileAttachment.setInputStream(uploadInput.getUploadDataAsStream()) ;
          fileAttachment.setMimeType(uploadResource.getMimeType()) ;
          fileSize = (long)uploadResource.getUploadedSize() ;
          fileAttachment.setSize(fileSize) ;
          fileAttachment.setId("file" + IdGenerator.generate()) ;
          fileAttachment.setNodeName(IdGenerator.generate() + uploadResource.getFileName().substring(uploadResource.getFileName().lastIndexOf(".")));
          listFileAttachment.add(fileAttachment) ;
        } else {
        	UIApplication uiApp = attachMentForm.getAncestorOfType(UIApplication.class) ;
        	uiApp.addMessage(new ApplicationMessage("UIAttachMentForm.msg.size-of-file-is-0", new Object[]{uploadResource.getFileName()}, ApplicationMessage.WARNING)) ;
        	event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        	return ;
        }
//      remove temp file in upload service and server
  			uploadService.removeUpload(uploadInput.getUploadId()) ;
      }
      
      if(listFileAttachment.isEmpty()) {
        UIApplication uiApp = attachMentForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAttachMentForm.msg.file-not-found", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      
      UIPopupContainer popupContainer = attachMentForm.getAncestorOfType(UIPopupContainer.class) ;
      if(attachMentForm.isChangeAvatar) {
      	if(listFileAttachment.get(0).getMimeType().indexOf("image") < 0){
      		UIApplication uiApp = attachMentForm.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIAttachMentForm.msg.fileIsNotImage", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
      	}
      	if(listFileAttachment.get(0).getSize() >= (2 * 1048576)){
      		UIApplication uiApp = attachMentForm.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIAttachMentForm.msg.avatar-upload-long", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
      	}
      	FAQService service = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class);
      	//SessionProvider sessionProvider = FAQUtils.getSystemProvider();
      	service.saveUserAvatar(FAQUtils.getCurrentUser(), listFileAttachment.get(0));
      	String avatarUrl = FAQUtils.getFileSource(((FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class))
																																				.getUserAvatar(FAQUtils.getCurrentUser()), 
																									attachMentForm.getApplicationComponent(DownloadService.class)) ;
				if(avatarUrl == null || avatarUrl.trim().length() < 1)
					avatarUrl = "/faq/skin/DefaultSkin/webui/background/Avatar1.gif"; //TODO should to get from resource bundle
      	//sessionProvider.close();
      	UIWatchContainer watchContainer = attachMentForm.getAncestorOfType(UIWatchContainer.class);
      	UISettingForm settingForm = watchContainer.getChild(UISettingForm.class);
      	settingForm.setAvatarUrl(avatarUrl);
      	event.getRequestContext().addUIComponentToUpdateByAjax(watchContainer);
      	
      	UIPopupAction popupAction = watchContainer.getChild(UIPopupAction.class) ;
      	popupAction.deActivate() ;
      	event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      	return;
      } else{
        UIQuestionForm questionForm = popupContainer.getChild(UIQuestionForm.class) ;
        if(questionForm == null) {
          UIAnswersPortlet portlet = attachMentForm.getAncestorOfType(UIAnswersPortlet.class) ;
          UIQuestionManagerForm questionManagerForm = portlet.findFirstComponentOfType(UIQuestionManagerForm.class) ;
          questionForm = questionManagerForm.getChildById(questionManagerForm.UI_QUESTION_FORM) ;
        }
        questionForm.setListFileAttach(listFileAttachment) ;
        questionForm.refreshUploadFileList() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(questionForm) ;
      }
      
      UIPopupAction uiPopupAction = popupContainer.getChild(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  
  static public class CancelActionListener extends EventListener<UIAttachMentForm> {
  	@SuppressWarnings("static-access")
    public void execute(Event<UIAttachMentForm> event) throws Exception {
      UIAttachMentForm uiAttachMent = event.getSource() ;
//    remove temp file in upload service and server      
      UploadService uploadService = uiAttachMent.getApplicationComponent(UploadService.class) ;
      UIFormUploadInput uploadInput ;
      for(int i = 0 ; i < uiAttachMent.numberUpload; i ++) {
        try{
        	uploadInput = uiAttachMent.getChildById(FILE_UPLOAD + i) ;
    			uploadService.removeUpload(uploadInput.getUploadId()) ;
        }catch(Exception e){}
      }
      
      if(uiAttachMent.isChangeAvatar){
      	UIWatchContainer popupContainer = uiAttachMent.getAncestorOfType(UIWatchContainer.class) ;
      	UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
      	popupAction.deActivate() ;
      	event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      } else {
	      UIPopupContainer popupContainer = uiAttachMent.getAncestorOfType(UIPopupContainer.class) ;
	      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
	      popupAction.deActivate() ;
	      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      }
    }
  }
}