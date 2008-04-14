/***************************************************************************
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
 ***************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumFormatUtils;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.webui.EmptyNameValidator;
import org.exoplatform.forum.webui.UIBreadcumbs;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputIconSelector;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormWYSIWYGInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 * Aug 22, 2007	
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UITopicForm.gtmpl",
		events = {
			@EventConfig(listeners = UITopicForm.PreviewThreadActionListener.class, phase = Phase.DECODE), 
			@EventConfig(listeners = UITopicForm.SubmitThreadActionListener.class), 
			@EventConfig(listeners = UITopicForm.AttachmentActionListener.class), 
			@EventConfig(listeners = UITopicForm.RemoveAttachmentActionListener.class), 
			@EventConfig(listeners = UITopicForm.CancelActionListener.class,phase = Phase.DECODE),
			@EventConfig(listeners = UITopicForm.SelectTabActionListener.class, phase=Phase.DECODE),
			@EventConfig(listeners = UITopicForm.AddValuesUserActionListener.class, phase=Phase.DECODE)
		}
)
public class UITopicForm extends UIForm implements UIPopupComponent, UISelector {
	
	public static final String FIELD_THREADCONTEN_TAB = "ThreadContent" ;
	public static final String FIELD_THREADICON_TAB = "ThreadIcon" ;
	public static final String FIELD_THREADOPTION_TAB = "ThreadOption" ;
	public static final String FIELD_THREADPERMISSION_TAB = "ThreadPermission" ;
	
	
	public static final String FIELD_TOPICTITLE_INPUT = "ThreadTitle" ;
	public static final String FIELD_MESSAGE_TEXTAREA = "Message" ;
	final static public String FIELD_MESSAGECONTENT = "messageContent" ;
	public static final String FIELD_TOPICSTATUS_SELECTBOX = "TopicStatus" ;
	public static final String FIELD_TOPICSTATE_SELECTBOX = "TopicState" ;
	
	public static final String FIELD_APPROVED_CHECKBOX = "Approved" ;
	public static final String FIELD_MODERATEPOST_CHECKBOX = "ModeratePost" ;
	public static final String FIELD_NOTIFYWHENADDPOST_CHECKBOX = "NotifyWhenAddPost" ;
	public static final String FIELD_STICKY_CHECKBOX = "Sticky" ;
	
	public static final String FIELD_CANVIEW_INPUT = "CanView" ;
	public static final String FIELD_CANPOST_INPUT = "CanPost" ;
	final static public String ACT_REMOVE = "remove" ;
	final static public String FIELD_ATTACHMENTS = "attachments" ;
	
	
	private List<ForumAttachment> attachments_ = new ArrayList<ForumAttachment>() ;
	private String categoryId; 
	private String forumId ;
	private String topicId ;
	private int id = 0;
  private String userInvalid = "" ;
  private Topic topic = new Topic() ;
	@SuppressWarnings("unchecked")
	public UITopicForm() throws Exception {
		UIFormStringInput topicTitle = new UIFormStringInput(FIELD_TOPICTITLE_INPUT, FIELD_TOPICTITLE_INPUT, null);
		topicTitle.addValidator(EmptyNameValidator.class) ;
//		UIFormTextAreaInput message = new UIFormTextAreaInput(FIELD_MESSAGE_TEXTAREA, FIELD_MESSAGE_TEXTAREA, null);
		
		List<SelectItemOption<String>> ls = new ArrayList<SelectItemOption<String>>() ;
		ls.add(new SelectItemOption<String>("Open", "open")) ;
		ls.add(new SelectItemOption<String>("Closed", "closed")) ;
		UIFormSelectBox topicState = new UIFormSelectBox(FIELD_TOPICSTATE_SELECTBOX, FIELD_TOPICSTATE_SELECTBOX, ls) ;
		topicState.setDefaultValue("open");
		List<SelectItemOption<String>> ls1 = new ArrayList<SelectItemOption<String>>() ;
		ls1.add(new SelectItemOption<String>("UnLock", "unlock")) ;
		ls1.add(new SelectItemOption<String>("Locked", "locked")) ;
		UIFormSelectBox topicStatus = new UIFormSelectBox(FIELD_TOPICSTATUS_SELECTBOX, FIELD_TOPICSTATUS_SELECTBOX, ls1) ;
		topicStatus.setDefaultValue("unlock");
		
		UIFormCheckBoxInput moderatePost = new UIFormCheckBoxInput<Boolean>(FIELD_MODERATEPOST_CHECKBOX, FIELD_MODERATEPOST_CHECKBOX, false);
		UIFormCheckBoxInput checkWhenAddPost = new UIFormCheckBoxInput<Boolean>(FIELD_NOTIFYWHENADDPOST_CHECKBOX, FIELD_NOTIFYWHENADDPOST_CHECKBOX, false);
		UIFormCheckBoxInput sticky = new UIFormCheckBoxInput<Boolean>(FIELD_STICKY_CHECKBOX, FIELD_STICKY_CHECKBOX, false);
    UIFormTextAreaInput canView = new UIFormTextAreaInput(FIELD_CANVIEW_INPUT, FIELD_CANVIEW_INPUT, null);
    UIFormTextAreaInput canPost = new UIFormTextAreaInput(FIELD_CANPOST_INPUT, FIELD_CANPOST_INPUT, null);
		UIFormWYSIWYGInput formWYSIWYGInput = new UIFormWYSIWYGInput(FIELD_MESSAGECONTENT, null, null, true);
		
		UIFormInputIconSelector uiIconSelector = new UIFormInputIconSelector(FIELD_THREADICON_TAB, FIELD_THREADICON_TAB) ;
		uiIconSelector.setSelectedIcon("IconsView");
		
		
		UIFormInputWithActions threadContent = new UIFormInputWithActions(FIELD_THREADCONTEN_TAB);
		threadContent.addUIFormInput(topicTitle);
		threadContent.addUIFormInput(formWYSIWYGInput);
		threadContent.addUIFormInput(new UIFormInputInfo(FIELD_ATTACHMENTS, FIELD_ATTACHMENTS, null)) ;
		threadContent.setActionField(FIELD_THREADCONTEN_TAB, getUploadFileList()) ;

		UIFormInputWithActions threadOption = new UIFormInputWithActions(FIELD_THREADOPTION_TAB);
		threadOption.addUIFormInput(topicState);
		threadOption.addUIFormInput(topicStatus);
		threadOption.addUIFormInput(moderatePost);
		threadOption.addUIFormInput(checkWhenAddPost);
		threadOption.addUIFormInput(sticky);
		
		UIFormInputWithActions threadPermission = new UIFormInputWithActions(FIELD_THREADPERMISSION_TAB);
		threadPermission.addUIFormInput(canPost);
		threadPermission.addUIFormInput(canView);
    
    String[] childIds = new String[]{FIELD_CANVIEW_INPUT, FIELD_CANPOST_INPUT} ;
    List<ActionData> actions ;
    ActionData ad ;
    for(String string : childIds) {
      actions = new ArrayList<ActionData>() ;
      ad = new ActionData() ;
      ad.setActionListener("AddValuesUser") ;
      ad.setActionParameter(string) ;
      ad.setCssIconClass("SelectUserIcon") ;
      ad.setActionName("SelectUser");
      actions.add(ad) ;
      threadPermission.setActionField(string, actions);
    }
		
		addUIFormInput(threadContent) ;
		addUIFormInput(uiIconSelector) ;
		addUIFormInput(threadOption) ;
		addUIFormInput(threadPermission) ;
		
	}
	
	public void setTopicIds(String categoryId, String forumId) {
		this.categoryId = categoryId ;
		this.forumId = forumId ;
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	@SuppressWarnings("unused")
  private boolean getIsSelected(int id) {
		if(this.id == id) return true ;
		return false ;
	}
	
	public List<ActionData> getUploadFileList() { 
		List<ActionData> uploadedFiles = new ArrayList<ActionData>() ;
		for(ForumAttachment attachdata : attachments_) {
			ActionData fileUpload = new ActionData() ;
			fileUpload.setActionListener("") ;
			fileUpload.setActionType(ActionData.TYPE_ICON) ;
			fileUpload.setCssIconClass("AttachmentIcon ZipFileIcon") ;
			fileUpload.setActionName(attachdata.getName() + " ("+attachdata.getSize()+" Kb)" ) ;
			fileUpload.setShowLabel(true) ;
			uploadedFiles.add(fileUpload) ;
			ActionData removeAction = new ActionData() ;
			removeAction.setActionListener("RemoveAttachment") ;
			removeAction.setActionName(ACT_REMOVE);
			removeAction.setActionParameter(attachdata.getId());
			removeAction.setActionType(ActionData.TYPE_LINK) ;
			removeAction.setBreakLine(true) ;
			uploadedFiles.add(removeAction) ;
		}
		return uploadedFiles ;
	}
	public void refreshUploadFileList() throws Exception {
		UIFormInputWithActions inputSet = getChildById(FIELD_THREADCONTEN_TAB) ;
		inputSet.setActionField(FIELD_ATTACHMENTS, getUploadFileList()) ;
	}
	public void addToUploadFileList(ForumAttachment attachfile) {
		attachments_.add(attachfile) ;
	}
	public void removeFromUploadFileList(ForumAttachment attachfile) {
		attachments_.remove(attachfile);
	}	
	public void removeUploadFileList() {
		attachments_.clear() ;
	}
	public List<ForumAttachment> getAttachFileList() {
		return attachments_ ;
	}

	private String[] splitForForum (String str) throws Exception {
		return ForumFormatUtils.splitForForum(str);
	}
	
	private String unSplitForForum (String[] str) throws Exception {
		return ForumFormatUtils.unSplitForForum(str) ;
	}
	
	public void setUpdateTopic(Topic topic, boolean isUpdate) throws Exception {
		if(isUpdate) {
			this.topic =  topic ;
			this.topicId = topic.getId() ;
			UIFormInputWithActions threadContent = this.getChildById(FIELD_THREADCONTEN_TAB);
			threadContent.getUIStringInput(FIELD_TOPICTITLE_INPUT).setValue(topic.getTopicName());
			threadContent.getChild(UIFormWYSIWYGInput.class).setValue(topic.getDescription());
			
			UIFormInputWithActions threadOption = this.getChildById(FIELD_THREADOPTION_TAB);
			String stat = "open";
			if(topic.getIsClosed()) stat = "closed";
			threadOption.getUIFormSelectBox(FIELD_TOPICSTATE_SELECTBOX).setValue(stat);
			if(topic.getIsLock()) stat = "locked";
			else stat = "unlock";
			threadOption.getUIFormSelectBox(FIELD_TOPICSTATUS_SELECTBOX).setValue(stat);
			threadOption.getUIFormCheckBoxInput(FIELD_MODERATEPOST_CHECKBOX).setChecked(topic.getIsModeratePost());
			threadOption.getUIFormCheckBoxInput(FIELD_NOTIFYWHENADDPOST_CHECKBOX).setChecked(topic.getIsNotifyWhenAddPost());
			threadOption.getUIFormCheckBoxInput(FIELD_STICKY_CHECKBOX).setChecked(topic.getIsSticky());
			
			UIFormInputWithActions threadPermission = this.getChildById(FIELD_THREADPERMISSION_TAB);
			threadPermission.getUIStringInput(FIELD_CANVIEW_INPUT).setValue(unSplitForForum(topic.getCanView()));
			threadPermission.getUIStringInput(FIELD_CANPOST_INPUT).setValue(unSplitForForum(topic.getCanPost()));
      ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
      String postId = topicId.replaceFirst("topic", "post") ;
      Post post = forumService.getPost(ForumSessionUtils.getSystemProvider(), this.categoryId, this.forumId, this.topicId, postId);
      if(post.getAttachments() != null && post.getAttachments().size() > 0) {
        this.attachments_ = post.getAttachments();
        this.refreshUploadFileList();
      }
			getChild(UIFormInputIconSelector.class).setSelectedIcon(topic.getIcon());
		}
	}
  
  private String[] pilterUser(String[] users) {
    List<String> validUsers = new ArrayList<String>() ;
    try{
      for(String user : users) {
        if(ForumSessionUtils.getUserByUserId(user.trim()) != null) {
          validUsers.add(user.trim()) ;
        } else {
          if(this.userInvalid.length() > 0) this.userInvalid += ", " ;
          this.userInvalid += user.trim() ;
        }
      }
    } catch(Exception e){}
    return validUsers.toArray(new String[]{}) ;
  }
	
	static	public class PreviewThreadActionListener extends EventListener<UITopicForm> {
    public void execute(Event<UITopicForm> event) throws Exception {
			UITopicForm uiForm = event.getSource() ;
			int t = 0, k = 1 ;
			UIFormInputWithActions threadContent = uiForm.getChildById(FIELD_THREADCONTEN_TAB);
			UIFormStringInput stringInputTitle = threadContent.getUIStringInput(FIELD_TOPICTITLE_INPUT) ; 
			String topicTitle = "  " + stringInputTitle.getValue();
			topicTitle = topicTitle.trim() ;
			String message = "  " +	threadContent.getChild(UIFormWYSIWYGInput.class).getValue();
			message = message.trim() ;
			t = message.length() ;
			if(topicTitle.length() <= 3) {k = 0;}
			if(t >= 3 && k != 0) {
				String userName = ForumSessionUtils.getCurrentUser() ;
				Post postNew = new Post();
				postNew.setOwner(userName);
				postNew.setName(topicTitle);
				postNew.setCreatedDate(new Date());
				postNew.setModifiedBy(userName);
				postNew.setModifiedDate(new Date());
				postNew.setMessage(message);
				postNew.setAttachments(uiForm.attachments_) ;
				UIFormInputIconSelector uiIconSelector = uiForm.getChild(UIFormInputIconSelector.class);
				postNew.setIcon(uiIconSelector.getSelectedIcon());
				
				UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
				UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
				UIViewTopic viewTopic = popupAction.activate(UIViewTopic.class, 670) ;
				viewTopic.setPostView(postNew) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
			}else {
				String sms = "" ;
				UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
				if(k == 0) {
					sms = "Thread Title" ;
					if(t < 3) sms = "Thread Title and Message";
					Object[] args = { sms };
					uiApp.addMessage(new ApplicationMessage("NameValidator.msg.ShortText", args, ApplicationMessage.WARNING)) ;
				} else if(t < 3) {
					Object[] args = { "Message" };
					uiApp.addMessage(new ApplicationMessage("NameValidator.msg.ShortMessage", args, ApplicationMessage.WARNING)) ;
				}
			}
		}
	}
	
	static	public class SubmitThreadActionListener extends EventListener<UITopicForm> {
    public void execute(Event<UITopicForm> event) throws Exception {
			UITopicForm uiForm = event.getSource() ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			int t = 0, k = 1 ;
			UIFormInputWithActions threadContent = uiForm.getChildById(FIELD_THREADCONTEN_TAB);
			UIFormStringInput stringInputTitle = threadContent.getUIStringInput(FIELD_TOPICTITLE_INPUT) ; 
			String topicTitle = "  " + stringInputTitle.getValue();
			topicTitle = topicTitle.trim() ;
			String message = "  " +	threadContent.getChild(UIFormWYSIWYGInput.class).getValue();
			message = message.trim() ;
			t = message.length() ;
			if(topicTitle.length() <= 3) {k = 0;}
			if(t >= 3 && k != 0) {	
				UIFormInputWithActions threadOption = uiForm.getChildById(FIELD_THREADOPTION_TAB);
				// uiForm.getUIFormTextAreaInput(FIELD_MESSAGE_TEXTAREA).getValue() ;
				String topicState = threadOption.getUIFormSelectBox(FIELD_TOPICSTATE_SELECTBOX).getValue();
				String topicStatus = threadOption.getUIFormSelectBox(FIELD_TOPICSTATUS_SELECTBOX).getValue();
				
				Boolean moderatePost = (Boolean)threadOption.getUIFormCheckBoxInput(FIELD_MODERATEPOST_CHECKBOX).getValue();
				Boolean whenNewPost = (Boolean)threadOption.getUIFormCheckBoxInput(FIELD_NOTIFYWHENADDPOST_CHECKBOX).getValue();
				Boolean sticky = (Boolean)threadOption.getUIFormCheckBoxInput(FIELD_STICKY_CHECKBOX).getValue();
				UIFormInputWithActions threadPermission = uiForm.getChildById(FIELD_THREADPERMISSION_TAB);
        uiForm.userInvalid = "" ;
				String[] canView = uiForm.pilterUser(uiForm.splitForForum(threadPermission.getUIStringInput(FIELD_CANVIEW_INPUT).getValue())) ;
				String[] canPost = uiForm.pilterUser(uiForm.splitForForum(threadPermission.getUIStringInput(FIELD_CANPOST_INPUT).getValue())) ;
        if(uiForm.userInvalid.length() > 0) {
          throw new MessageException(new ApplicationMessage("UITopicForm.sms.userhavenotfound", new String[]{uiForm.userInvalid}, ApplicationMessage.WARNING)) ;
        }
				
				String userName = ForumSessionUtils.getCurrentUser() ;
				Topic topicNew = uiForm.topic;
				topicNew.setOwner(userName);
				topicNew.setTopicName(topicTitle);
				topicNew.setCreatedDate(new Date());
				topicNew.setModifiedBy(userName);
				topicNew.setModifiedDate(new Date());
				topicNew.setLastPostBy(userName);
				topicNew.setLastPostDate(new Date());
				topicNew.setDescription(message);
				
				topicNew.setIsNotifyWhenAddPost(whenNewPost);
				topicNew.setIsModeratePost(moderatePost);
				topicNew.setAttachments(uiForm.attachments_) ;
				if(topicState.equals("closed")) {
					topicNew.setIsClosed(true);
				}
				if(topicStatus.equals("locked")) {
					topicNew.setIsLock(true) ;
				}
				topicNew.setIsSticky(sticky);
				//topicNew.setIsApproved(approved);	
				
				UIFormInputIconSelector uiIconSelector = uiForm.getChild(UIFormInputIconSelector.class);
				topicNew.setIcon(uiIconSelector.getSelectedIcon());
				//topicNew.setAttachmentFirstPost(0) ;
        if(canView.length > 0) {
          String output = new String() ;
          for(String str : canView) {
            if(str.trim().length() > 0) {
              if(output != null && output.trim().length() > 0) output += "," ;
              output += str.trim() ;
            }
          }
          if(canPost.length > 0) {
            for(String string : canPost) {
              if(string != null && string.trim().length() > 0 && !ForumFormatUtils.isStringInStrings(canView, string)) {
                if(output.trim().length() > 0) output += "," ;
                output += string ;
              }
            }
            canView = ForumFormatUtils.splitForForum(output) ;
          } else {
            canView = canPost ;
          }
        }
        
				topicNew.setCanView(canView);
				topicNew.setCanPost(canPost);
				
				ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
				if(uiForm.topicId != null && uiForm.topicId.length() > 0) {
					topicNew.setId(uiForm.topicId);
					forumService.saveTopic(ForumSessionUtils.getSystemProvider(), uiForm.categoryId, uiForm.forumId, topicNew, false, false);
					forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath((uiForm.categoryId + "/" + uiForm.forumId + "/" + uiForm.topicId)) ;
				} else {
					topicNew.setVoteRating(0.0) ;
					topicNew.setUserVoteRating(new String[] {}) ;
					forumService.saveTopic(ForumSessionUtils.getSystemProvider(), uiForm.categoryId, uiForm.forumId, topicNew, true, false);
				}
				uiForm.topic = new Topic();
				forumPortlet.cancelAction() ;
				WebuiRequestContext context = RequestContext.getCurrentInstance() ;
				context.addUIComponentToUpdateByAjax(forumPortlet) ;
			} else {
				String sms = "" ;
				UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
				if(k == 0) {
					sms = "Thread Title" ;
					if(t < 3) sms = "Thread Title and Message";
					Object[] args = { sms };
					uiApp.addMessage(new ApplicationMessage("NameValidator.msg.ShortText", args, ApplicationMessage.WARNING)) ;
				} else if(t < 3) {
					Object[] args = { "Message" };
					uiApp.addMessage(new ApplicationMessage("NameValidator.msg.ShortMessage", args, ApplicationMessage.WARNING)) ;
				}
			}
		}
	}
	
	static public class AttachmentActionListener extends EventListener<UITopicForm> {
    public void execute(Event<UITopicForm> event) throws Exception {
			UITopicForm uiForm = event.getSource() ;
			UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			UIPopupAction uiChildPopup = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
			UIAttachFileForm attachFileForm = uiChildPopup.activate(UIAttachFileForm.class, 500) ;
			attachFileForm.updateIsTopicForm(true) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
		}
	}
	
	static public class RemoveAttachmentActionListener extends EventListener<UITopicForm> {
    public void execute(Event<UITopicForm> event) throws Exception {
			UITopicForm uiTopicForm = event.getSource() ;
			String attFileId = event.getRequestContext().getRequestParameter(OBJECTID);
			//BufferAttachment attachfile = new BufferAttachment();
			for (ForumAttachment att : uiTopicForm.attachments_) {
				if (att.getId().equals(attFileId)) {
					//attachfile = (BufferAttachment) att;
					uiTopicForm.removeFromUploadFileList(att);
          uiTopicForm.attachments_.remove(att) ;
          break ;
				}
			}
			uiTopicForm.refreshUploadFileList() ;
		}
	}
	
	static	public class CancelActionListener extends EventListener<UITopicForm> {
    public void execute(Event<UITopicForm> event) throws Exception {
			UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}

	static	public class SelectTabActionListener extends EventListener<UITopicForm> {
		public void execute(Event<UITopicForm> event) throws Exception {
			String id = event.getRequestContext().getRequestParameter(OBJECTID)	;
			UITopicForm topicForm = event.getSource();
			topicForm.id = Integer.parseInt(id);
			event.getRequestContext().addUIComponentToUpdateByAjax(topicForm.getParent()) ;
		}
	}
  
  public void updateSelect(String selectField, String value ) throws Exception {
    UIFormTextAreaInput stringInput = getUIFormTextAreaInput(selectField) ;
    String values = stringInput.getValue() ;
    boolean canAdd = true ;
    if(values != null && values.trim().length() > 0) {
      if(!ForumFormatUtils.isStringInStrings(values.split(","), value)){
        if(values.trim().lastIndexOf(",") == (values.trim().length() - 1)) values = values.trim() ;
        else values = values.trim() + ",";
      } else {
        canAdd = false ;
      }
    } else {
      values = "" ;
    }
    if(canAdd) {
      values = values.trim() + value ;
      stringInput.setValue(values) ;
    }
  }
  
  static  public class AddValuesUserActionListener extends EventListener<UITopicForm> {
    public void execute(Event<UITopicForm> event) throws Exception {
      UITopicForm uiTopicForm = event.getSource() ;
      String childId = event.getRequestContext().getRequestParameter(OBJECTID)  ;
      if(childId != null && childId.length() > 0) {
        UIPopupContainer popupContainer = uiTopicForm.getAncestorOfType(UIPopupContainer.class) ;
        UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
        UIGroupSelector uiGroupSelector = popupAction.activate(UIGroupSelector.class, 500) ;
        uiGroupSelector.setType("0") ;
        //uiGroupSelector.setSelectedGroups(null) ;
        uiGroupSelector.setComponent(uiTopicForm, new String[]{childId}) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
      }
    }
  }
}