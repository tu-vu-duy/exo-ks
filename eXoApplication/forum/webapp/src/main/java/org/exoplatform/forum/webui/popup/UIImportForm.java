package org.exoplatform.forum.webui.popup;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemExistsException;
import javax.jcr.PathNotFoundException;
import javax.jcr.nodetype.ConstraintViolationException;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.UIBreadcumbs;
import org.exoplatform.forum.webui.UICategories;
import org.exoplatform.forum.webui.UICategory;
import org.exoplatform.forum.webui.UICategoryContainer;
import org.exoplatform.forum.webui.UIForumPortlet;
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

@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =  "app:/templates/forum/webui/popup/UIImportForm.gtmpl",
		events = {
			@EventConfig(listeners = UIImportForm.SaveActionListener.class),
			@EventConfig(listeners = UIImportForm.CancelActionListener.class)
		}
)

public class UIImportForm extends UIForm implements UIPopupComponent{
	private final String FILE_UPLOAD = "FileUpload";
	private String categoryPath = null;
	public void activate() throws Exception { }
	public void deActivate() throws Exception { }

	public UIImportForm(){
		int sizeLimit = ForumUtils.getLimitUploadSize() ;
		if(sizeLimit >= 0) this.addChild(new UIFormUploadInput(FILE_UPLOAD, FILE_UPLOAD, sizeLimit));
		else this.addChild(new UIFormUploadInput(FILE_UPLOAD, FILE_UPLOAD));
		categoryPath = null;
	}
	
	public void setPath(String categoryPath){
		this.categoryPath = categoryPath;
	}

	static public class SaveActionListener extends EventListener<UIImportForm> {
    public void execute(Event<UIImportForm> event) throws Exception {
			UIImportForm importForm = event.getSource() ;

			UIForumPortlet forumPortlet = importForm.getAncestorOfType(UIForumPortlet.class) ;
			UIFormUploadInput uploadInput = (UIFormUploadInput)importForm.getChildById(importForm.FILE_UPLOAD);
			UIApplication uiApplication = importForm.getAncestorOfType(UIApplication.class) ;
			if(uploadInput.getUploadResource() == null){
				uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.file-not-found", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				return;
			}
			String fileName = uploadInput.getUploadResource().getFileName();
			MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();
			String mimeType = mimeTypeResolver.getMimeType(fileName);
			String nodePath = null;
			ForumService service = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
			if(ForumUtils.isEmpty(importForm.categoryPath)){
				nodePath = service.getForumHomePath();
			} else {
				nodePath = importForm.categoryPath;
			}
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			boolean isUdateForm = false;
			boolean isErr = false;
			try{
				if("text/xml".equals(mimeType) || "application/zip".equals(mimeType) ){
					service.importXML(nodePath,uploadInput.getUploadDataAsStream(),mimeType);
				} else {
					uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.mimetype-invalid", null, ApplicationMessage.WARNING));
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
					return;
				}
  			popupAction.deActivate() ;
  			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
  			if(!ForumUtils.isEmpty(importForm.categoryPath)){
  				UICategory category = forumPortlet.findFirstComponentOfType(UICategory.class) ;
  				category.setIsEditForum(true);
  				event.getRequestContext().addUIComponentToUpdateByAjax(category) ;
  			}else{
  				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
  			}  			
  			uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.import-successful", null));
  			event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
  			isUdateForm = true;
			} catch(PathNotFoundException pnf){
				uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.CategoryNoLongerExist", null, ApplicationMessage.WARNING));
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
				isErr = true;
			} catch (AccessDeniedException ace) {
				uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.access-denied", null, ApplicationMessage.WARNING));
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
				isErr = true;
			} catch (ConstraintViolationException con) {
				uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.constraint-violation-exception", null, ApplicationMessage.WARNING));
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
				isErr = true;
			} catch(ItemExistsException ie){
				uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.ObjectIsExist", null, ApplicationMessage.WARNING));
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
			} catch (Exception ise) {
				uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.filetype-error", null, ApplicationMessage.WARNING));
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
				return;
			}
//		remove temp file in upload service and server
			UploadService uploadService = importForm.getApplicationComponent(UploadService.class) ;
			uploadService.removeUpload(uploadInput.getUploadId()) ;
			if(!isUdateForm){
				popupAction.deActivate() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			}
			if(isErr) {
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE);
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			}
		}
	}

	static public class CancelActionListener extends EventListener<UIImportForm> {
		public void execute(Event<UIImportForm> event) throws Exception {
			UIImportForm importForm = event.getSource() ;
//		remove temp file in upload service and server
			try{
				UIFormUploadInput uploadInput = (UIFormUploadInput)importForm.getChildById(importForm.FILE_UPLOAD);
				UploadService uploadService = importForm.getApplicationComponent(UploadService.class) ;
				uploadService.removeUpload(uploadInput.getUploadId()) ;
			}catch(Exception e) {}
			
			UIForumPortlet portlet = importForm.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}