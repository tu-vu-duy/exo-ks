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
package org.exoplatform.forum.webui;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.PathNotFoundException;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactAttachment;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.forum.ForumFormatUtils;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumTransformHTML;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumSeach;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.popup.UIMovePostForm;
import org.exoplatform.forum.webui.popup.UIMoveTopicForm;
import org.exoplatform.forum.webui.popup.UIPageListPostHidden;
import org.exoplatform.forum.webui.popup.UIPageListPostUnApprove;
import org.exoplatform.forum.webui.popup.UIPollForm;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.forum.webui.popup.UIPopupContainer;
import org.exoplatform.forum.webui.popup.UIPostForm;
import org.exoplatform.forum.webui.popup.UIRatingForm;
import org.exoplatform.forum.webui.popup.UISplitTopicForm;
import org.exoplatform.forum.webui.popup.UITagForm;
import org.exoplatform.forum.webui.popup.UITopicForm;
import org.exoplatform.forum.webui.popup.UIViewPost;
import org.exoplatform.forum.webui.popup.UIViewPostedByUser;
import org.exoplatform.forum.webui.popup.UIViewTopicCreatedByUser;
import org.exoplatform.forum.webui.popup.UIViewUserProfile;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.portletcontainer.plugins.pc.portletAPIImp.PortletRequestImp;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template =	"app:/templates/forum/webui/UITopicDetail.gtmpl", 
		events = {
			@EventConfig(listeners = UITopicDetail.AddPostActionListener.class ),
			@EventConfig(listeners = UITopicDetail.RatingTopicActionListener.class ),
			@EventConfig(listeners = UITopicDetail.AddTagTopicActionListener.class ),
			@EventConfig(listeners = UITopicDetail.GoNumberPageActionListener.class ),
			@EventConfig(listeners = UITopicDetail.SearchFormActionListener.class ),
			
			@EventConfig(listeners = UITopicDetail.PrintActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.EditActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.DeleteActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.PrivatePostActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.QuoteActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.EditTopicActionListener.class ),	//Topic Menu
			@EventConfig(listeners = UITopicDetail.PrintPageActionListener.class ),
			@EventConfig(listeners = UITopicDetail.AddPollActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetOpenTopicActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetCloseTopicActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetLockedTopicActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetUnLockTopicActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetMoveTopicActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetStickTopicActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetUnStickTopicActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SplitTopicActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetApproveTopicActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetUnApproveTopicActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetDeleteTopicActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.MergePostActionListener.class ), //Post Menu 
			@EventConfig(listeners = UITopicDetail.MovePostActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetApprovePostActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetHiddenPostActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetUnHiddenPostActionListener.class ),	
//			@EventConfig(listeners = UITopicDetail.SetUnApproveAttachmentActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.DeletePostActionListener.class ), 
			@EventConfig(listeners = UITopicDetail.ViewPostedByUserActionListener.class ), 
			@EventConfig(listeners = UITopicDetail.ViewPublicUserInfoActionListener.class ) ,
			@EventConfig(listeners = UITopicDetail.ViewThreadByUserActionListener.class ),
			@EventConfig(listeners = UITopicDetail.QuickReplyActionListener.class),
			@EventConfig(listeners = UITopicDetail.PreviewReplyActionListener.class)
		}
)
public class UITopicDetail extends UIForm {
	private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private String categoryId ;
	private String forumId ; 
	private String topicId = "";
	private boolean viewTopic = true ;
	private Forum forum;
	private Topic topic = new Topic();
	private JCRPageList pageList ;
	private long pageSelect	= 1 ;
	private boolean isGopage = false ;
	private boolean isEditTopic = false ;
	private boolean isUpdatePageList = false ;
	private boolean isGetTopic = false ;
	private String IdPostView = "false" ;
	private String IdLastPost = "false" ;
	private List<Post>  posts ;
	private boolean isEdit = false ;
	private long maxPost = 10 ;
	private UserProfile userProfile = null;
	private String userName = " " ;
	private boolean isModeratePost = false ;
	private Map<String, UserProfile> mapUserProfile = new HashMap<String, UserProfile>();
	private Map<String, Contact> mapContact = new HashMap<String, Contact>();
	public static final String FIELD_MESSAGE_TEXTAREA = "Message" ;
	public UITopicDetail() throws Exception {
		addUIFormInput( new UIFormStringInput("gopage1", null)) ;
		addUIFormInput( new UIFormStringInput("gopage2", null)) ;
		addUIFormInput( new UIFormStringInput("search", null)) ;
		addUIFormInput( new UIFormTextAreaInput(FIELD_MESSAGE_TEXTAREA, FIELD_MESSAGE_TEXTAREA,null)) ;
		addChild(UIForumPageIterator.class, null, "TopicPageIterator") ;
		addChild(UIPostRules.class, null, null);
		this.setSubmitAction("GoNumberPage") ;
		this.setActions(new String[]{"PreviewReply","QuickReply"} );
	}
	
	@SuppressWarnings("unused")
  private UserProfile getUserProfile() {
		return userProfile ;
	}
	
	@SuppressWarnings("unused")
  private boolean isOnline(String userId) throws Exception {
		return this.forumService.isOnline(userId) ;
	}
	
	public void setUpdateTopic(String categoryId, String forumId, String topicId, boolean viewTopic) throws Exception {
		this.categoryId = categoryId ;
		this.forumId = forumId ;
		this.topicId = topicId ;
		this.viewTopic = viewTopic ;
		this.isUpdatePageList = true ;
		String userName = ForumSessionUtils.getCurrentUser() ;
		if(userName ==null || userName.length() <= 0 || !this.viewTopic) {
			userName = "guest" ;
		}
		UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class) ;
		forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath((categoryId + "/" + forumId + "/" + topicId)) ;
		this.topic = forumService.getTopic(ForumSessionUtils.getSystemProvider(), categoryId, forumId, topicId, userName) ;
		if(!userName.equals("guest"))	forumPortlet.setUserProfile() ;
	}
	
	public void setTopicFromCate(String categoryId, String forumId, Topic topic, boolean viewTopic) throws Exception {
		this.categoryId = categoryId ;
		this.forumId = forumId ;
		this.topicId = topic.getId() ;
		this.viewTopic = viewTopic ;
		this.isUpdatePageList = true ;
		String userName = ForumSessionUtils.getCurrentUser() ;
		if(userName ==null || userName.length() <= 0 || !this.viewTopic) {
			userName = "guest" ;
		}
		UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class) ;
		boolean isGetService = false ;
		if(userName !=null && userName.length() > 0) {
			if(!userName.equals(this.userName)) {
				if(this.userProfile != null) {
					String []topicsRead = this.userProfile.getReadTopic() ;
					if(topicsRead != null && topicsRead.length > 0) {
						if(!ForumFormatUtils.isStringInStrings(topicsRead, this.topicId)) {
							isGetService = true ;
						} 
					} else isGetService = true ;
				} else isGetService = true ;
			} 
			this.userName = userName ;
		}
		if(isGetService) {
			this.topic = forumService.getTopic(ForumSessionUtils.getSystemProvider(), categoryId, forumId, topic.getId(), userName) ;
			forumPortlet.setUserProfile() ;
		} else {
			this.topic = topic ;
		}
		forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath((categoryId + "/" + forumId + "/" + topicId)) ;
	}
	
	public void setUpdateContainer(String categoryId, String forumId, Topic topic, long numberPage) throws Exception {
		if(this.topicId == null || !this.topicId.equals(topic.getId())) this.userName = "" ;
		this.categoryId = categoryId ;
		this.forumId = forumId ;
		this.topicId = topic.getId() ;
		this.viewTopic = false ;
		this.pageSelect = numberPage ;
		this.isGopage = true ;
		this.isEditTopic = false ;
		UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class) ;
		String userName = ForumSessionUtils.getCurrentUser() ;
		boolean isGetService = false ;
		if(userName !=null && userName.length() > 0) {
			if(!userName.equals(this.userName)) {
				if(this.userProfile != null) {
					String []topicsRead = this.userProfile.getReadTopic() ;
					if(topicsRead != null && topicsRead.length > 0) {
						if(!ForumFormatUtils.isStringInStrings(topicsRead, this.topicId)) {
							isGetService = true ;
						} 
					} else isGetService = true ;
				} else isGetService = true ;
			} 
			this.userName = userName ;
		}
		if(isGetService || this.isGetTopic) {
			this.topic = forumService.getTopic(ForumSessionUtils.getSystemProvider(), categoryId, forumId, topic.getId(), userName) ;
			forumPortlet.setUserProfile() ;
			this.isGetTopic = false ;
		} else {
			this.topic = topic ;
		}
		this.getChild(UIForumPageIterator.class).setSelectPage(numberPage) ;
		forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath((categoryId + "/" + forumId + "/" + topicId)) ;
	}
	
	public void setUpdateForum(Forum forum) throws Exception {
		this.forum = forum ;
	}
  
  @SuppressWarnings("unused")
  private boolean isCanPostReply() throws Exception {
  	String userName = this.userProfile.getUserId() ;
    boolean isMod = ForumServiceUtils.hasPermission(forum.getModerators(), userName) ;
    if(this.userProfile.getUserRole() > 0 && !isMod) {
      List<String> listUser = new ArrayList<String>() ;
      listUser.addAll(ForumServiceUtils.getUserPermission(this.topic.getCanPost())) ;
      listUser.addAll(ForumServiceUtils.getUserPermission(this.forum.getReplyTopicRole())) ;
      if(!listUser.isEmpty() && listUser.size() > 0) {
        if(!listUser.contains(userName)) {
          return false ;
        }
      }
    }
    return true ;
  }
	
	@SuppressWarnings("unused")
  private Forum getForum() throws Exception {
		return this.forum ;
	}
	
	@SuppressWarnings("unused")
	private String getIdPostView() {
		if(this.IdPostView.equals("true")){
			this.IdPostView = "false" ;
			return this.IdLastPost ;
		}
		String temp = this.IdPostView ;
		this.IdPostView = "false" ;
		return temp ;
	}
	
	public void setIdPostView(String IdPostView) {
	  this.IdPostView = IdPostView ;
  }
	
	public void setGetTopic(boolean isGetTopic) {
		this.isGetTopic = isGetTopic ;
	}
	
	public void setIsEditTopic( boolean isEditTopic) {
		this.isEditTopic = isEditTopic ;
	}

	public void setUpdatePageList(JCRPageList pageList) throws Exception {
		this.pageList = pageList ;
		this.isUpdatePageList = false ;
	}
  
	@SuppressWarnings("unused")
	private Topic getTopic() throws Exception {
		try {
			if(this.isEditTopic) {
				this.topic = forumService.getTopic(ForumSessionUtils.getSystemProvider(), categoryId, forumId, topicId, "guest") ;
				this.isEditTopic = false ;
			}
			return this.topic ;
		} catch (Exception e) {
			e.printStackTrace();
			return null ;
		}
	}
  
  @SuppressWarnings("unused")
  private boolean userCanView() {
    try {
      List<String> listUser = new ArrayList<String>() ;
      Topic topic = this.getTopic() ;
      listUser.addAll(ForumServiceUtils.getUserPermission(topic.getCanView())) ;
      Forum forum = this.getForum() ;
      listUser.addAll(ForumServiceUtils.getUserPermission(forum.getViewForumRole())) ;
      if(listUser.isEmpty()|| listUser.size() == 0 || listUser.contains(this.getUserProfile().getUserId())) return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false ;
  }
  
	@SuppressWarnings("unused")
  private String getFileSource(ForumAttachment attachment) throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    try {
    	InputStream input = attachment.getInputStream() ;
    	String fileName = attachment.getName() ;
    	return ForumSessionUtils.getFileSource(input, fileName, dservice);
    } catch (PathNotFoundException e) {
	    return null;
    }
  }

		@SuppressWarnings("unused")
	private Contact getPersonalContact(String userId) throws Exception {
			Contact contact ;
		if(mapContact.containsKey(userId)){
			contact = mapContact.get(userId) ;
		} else {
			contact = ForumSessionUtils.getPersonalContact(userId) ;
			mapContact.put(userId, contact) ;
		}
		if(contact == null) {
			contact = new Contact() ;
			contact.setId(userId) ;
		}
		return contact ;
	}
	
	@SuppressWarnings("unused")
  private String getAvatarUrl(Contact contact) throws Exception {
		DownloadService dservice = getApplicationComponent(DownloadService.class) ;
		try {
			ContactAttachment attachment = contact.getAttachment() ; 
    	InputStream input = attachment.getInputStream() ;
    	String fileName = attachment.getFileName() ;
    	return ForumSessionUtils.getFileSource(input, fileName, dservice);
    } catch (NullPointerException e) {
	    return "/forum/skin/DefaultSkin/webui/background/Avatar1.gif";
    }
	}
	@SuppressWarnings("unused")
	private void initPage() throws Exception {
		this.userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
		if(this.isUpdatePageList) {
			String isApprove = "" ;
			String isHidden = "" ;
			String userLogin = this.userProfile.getUserId();
			Topic topic = this.topic ;
			long role = this.userProfile.getUserRole() ;
			if(role >=2){ isHidden = "false" ;}
			if(role == 1) {
				if(!ForumServiceUtils.hasPermission(forum.getModerators(), userLogin)){
					isHidden = "false" ;
				}
			}
			if(this.forum.getIsModeratePost() || topic.getIsModeratePost()) {
				if(isHidden.equals("false") && !(this.topic.getOwner().equals(userLogin))) isApprove = "true" ;
			}
			this.pageList = this.forumService.getPosts(ForumSessionUtils.getSystemProvider(), this.categoryId, this.forumId, topicId, isApprove, isHidden, "", userLogin)	; 
			this.isUpdatePageList = false ;
		}
		long maxPost = this.userProfile.getMaxPostInPage() ;
		if(maxPost > 0) this.maxPost = maxPost ;
		pageList.setPageSize(this.maxPost) ;
		UIForumPageIterator forumPageIterator = this.getChild(UIForumPageIterator.class) ;
		forumPageIterator.updatePageList(this.pageList) ;
		if(IdPostView.equals("true")){
			forumPageIterator.setSelectPage(pageList.getAvailablePage()) ;
		}
		this.isModeratePost = this.topic.getIsModeratePost() ;
	}
	
	@SuppressWarnings("unused")
  private boolean getIsModeratePost(){return this.isModeratePost; }
	public void setUpdatePostPageList(boolean isUpdatePageList) {
	  this.isUpdatePageList = isUpdatePageList;
  }
	@SuppressWarnings("unused")
  private long getPageSelect() {return this.pageSelect ;}
	
	@SuppressWarnings({ "unchecked", "unused" })
	private List<Post> getPostPageList() throws Exception {
		if(this.pageList == null) return null ;
		if(!this.isGopage) {
			UIForumPageIterator forumPageIterator = this.getChild(UIForumPageIterator.class) ;
			this.pageSelect = forumPageIterator.getPageSelected() ;
			long availablePage = this.pageList.getAvailablePage() ;
			if(this.pageSelect > availablePage) {
				this.pageSelect = availablePage ;
				forumPageIterator.setSelectPage(availablePage);
			}
		}
		if(this.pageSelect < 1) return null ;
		this.posts = this.pageList.getPage(this.pageSelect) ;
		if(this.posts.size() > 0 && this.posts != null) {
			for (Post post : this.posts) {
				if(getUIFormCheckBoxInput(post.getId()) != null) {
					getUIFormCheckBoxInput(post.getId()).setChecked(false) ;
				}else {
					addUIFormInput(new UIFormCheckBoxInput(post.getId(), post.getId(), false) );
				}
				this.IdLastPost = post.getId() ;
			}
		}
		this.isGopage = false ;
		return this.posts ;
	}
	
	@SuppressWarnings({ "unused", "unchecked" })
  private List<Post> getAllPost() throws Exception {return this.pageList.getPage(0) ;}
	
	private Post getPost(String postId) throws Exception {
		for(Post post : this.posts) {
			if(post.getId().equals(postId)) return post;
		}
		return null ;
	}
	
	@SuppressWarnings("unused")
  private void setIsEdit( boolean isEdit) {
		this.isEdit = isEdit ;
	}
	@SuppressWarnings("unused")
  private void setPostRules(boolean isNull) throws Exception {
		boolean isLock = isNull ;
		UIPostRules postRules = getChild(UIPostRules.class); 
		if(!isNull) {
			this.userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
			isLock = this.forum.getIsClosed() ;
			if(!isLock)isLock = this.forum.getIsLock() ;
			if(!isLock) {
				if(this.topic != null) { 
					isLock = this.topic.getIsClosed() ;
					if(!isLock) isLock = this.topic.getIsLock() ;
				}
			}
			String userLogin = this.userProfile.getUserId() ;
			String[] strings = this.forum.getCreateTopicRole();
			if(!isLock && strings != null && strings.length > 0) {
				if(!this.isEdit)isLock = !ForumServiceUtils.hasPermission(strings, userLogin) ;
			}
			strings = this.forum.getReplyTopicRole() ;
			if(!isLock && strings != null && strings.length > 0) {
				if(!this.isEdit)isLock = !ForumServiceUtils.hasPermission(strings, userLogin) ;
			}
			strings = this.topic.getCanPost() ;
			if(!isLock && strings != null && strings.length > 0) {
				if(!this.isEdit)isLock = !ForumServiceUtils.hasPermission(strings, userLogin) ;
			}
		}
		postRules.setLock(isLock) ;
		postRules.setUserProfile(this.userProfile) ;
	}
	
	@SuppressWarnings("unused")
  private UserProfile getUserInfo(String userName) throws Exception {
		UserProfile userProfile = this.forumService.getUserInfo(ForumSessionUtils.getSystemProvider(), userName);
		if (userProfile.getUser() == null) {
    	User user = ForumSessionUtils.getUserByUserId(userName) ; 
			userProfile.setUser(user) ;
			mapUserProfile.put(userName, userProfile) ;
    }
		return userProfile ;
	}
	
	static public class AddPostActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIPostForm postForm = popupContainer.addChild(UIPostForm.class, null, null) ;
			postForm.setPostIds(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, topicDetail.topic) ;
			postForm.updatePost("", false, false, null) ;
			topicDetail.viewTopic = false ;
			popupContainer.setId("UIAddPostContainer") ;
			popupAction.activate(popupContainer, 700, 460) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static public class RatingTopicActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			String userName = ForumSessionUtils.getCurrentUser() ;
			String[] userVoteRating = topicDetail.topic.getUserVoteRating() ;
			boolean erro = false ;
			for (String string : userVoteRating) {
				if(string.equalsIgnoreCase(userName)) erro = true ; 
			}
			if(!erro) {
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIRatingForm ratingForm = popupAction.createUIComponent(UIRatingForm.class, null, null) ;
				ratingForm.updateRating(topicDetail.topic, topicDetail.categoryId, topicDetail.forumId) ;
				popupAction.activate(ratingForm, 300, 145) ;
				topicDetail.viewTopic = false ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				topicDetail.isEditTopic = true ;
			} else {
				Object[] args = { userName };
				throw new MessageException(new ApplicationMessage("UITopicDetail.sms.VotedRating", args, ApplicationMessage.WARNING)) ;
			}
		}
	}
	
	static public class AddTagTopicActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, "UITagFormContainer") ;
			UITagForm tagForm = popupContainer.addChild(UITagForm.class, null, null) ;
			tagForm.setTopicPathAndTagId(topicDetail.topic.getPath(), topicDetail.topic.getTagId()) ;
			popupAction.activate(popupContainer, 240, 280) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static public class SearchFormActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			String path = topicDetail.topic.getPath() ;
			UIFormStringInput formStringInput = topicDetail.getUIStringInput("search") ;
			String text = formStringInput.getValue() ;
			if(text != null && text.trim().length() > 0 && path != null) {
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.updateIsRendered(1) ;
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				UICategories categories = categoryContainer.getChild(UICategories.class);
				categories.setIsRenderChild(true) ;
				ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
				List<ForumSeach> list = forumService.getQuickSeach(ForumSessionUtils.getSystemProvider(), text+",,post", path);
				UIForumListSeach listSeachEvent = categories.getChild(UIForumListSeach.class) ;
				listSeachEvent.setListSeachEvent(list) ;
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath("ForumSeach") ;
				formStringInput.setValue("") ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			} else {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UIQuickSeachForm.msg.checkEmpty", args, ApplicationMessage.WARNING)) ;
			}
		}
	}

	static public class PrintActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
//			UITopicDetail topicDetail = event.getSource() ;
		}
	}

	static public class GoNumberPageActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			int idbt = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID)) ;
			UIFormStringInput stringInput1 = topicDetail.getUIStringInput("gopage1") ;
			UIFormStringInput stringInput2 = topicDetail.getUIStringInput("gopage2") ;
			String numberPage = "" ;
			if(idbt == 1) {
				numberPage = stringInput1.getValue() ;
			} else {
				numberPage = stringInput2.getValue() ;
			}
			stringInput1.setValue("") ; stringInput2.setValue("") ;
			if(numberPage != null && numberPage.trim().length() > 0) {
				try {
					long page = Long.parseLong(numberPage.trim()) ;
					if(page < 0) {
						Object[] args = { "go page" };
						throw new MessageException(new ApplicationMessage("NameValidator.msg.Invalid-number", args, ApplicationMessage.WARNING)) ;
					} else {
						if(page == 0) {
							page = (long)1;
						} else if(page > topicDetail.pageList.getAvailablePage()){
							page = topicDetail.pageList.getAvailablePage() ;
						}
						topicDetail.isGopage = true ;
						topicDetail.pageSelect = page ;
						topicDetail.getChild(UIForumPageIterator.class).setSelectPage(page) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
					}
	      } catch (NumberFormatException e) {
		      Object[] args = { "go page" };
					throw new MessageException(new ApplicationMessage("NameValidator.msg.Invalid-number", args, ApplicationMessage.WARNING)) ;
	      }
			}
		}
	}

	static public class EditActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			String postId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIPostForm postForm = popupContainer.addChild(UIPostForm.class, null, null) ;
			postForm.setPostIds(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, topicDetail.topic) ;
			postForm.updatePost(postId, false, false, topicDetail.getPost(postId)) ;
			topicDetail.viewTopic = false ;
			popupContainer.setId("UIEditPostContainer") ;
			popupAction.activate(popupContainer, 700, 460) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static public class DeleteActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			String postId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			topicDetail.forumService.removePost(ForumSessionUtils.getSystemProvider(), topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, postId) ;
			topicDetail.isUpdatePageList = true ;
			event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail.getParent()) ;
		}
	}
	
	static public class QuoteActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			String postId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIPostForm postForm = popupContainer.addChild(UIPostForm.class, null, null) ;
			postForm.setPostIds(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, topicDetail.topic) ;
			postForm.updatePost(postId, true, false, topicDetail.getPost(postId)) ;
			topicDetail.viewTopic = false ;
			popupContainer.setId("UIQuoteContainer") ;
			popupAction.activate(popupContainer, 700, 460) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static public class PrivatePostActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			String postId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIPostForm postForm = popupContainer.addChild(UIPostForm.class, null, null) ;
			postForm.setPostIds(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, topicDetail.topic) ;
			postForm.updatePost(postId, false, true, topicDetail.getPost(postId)) ;
			topicDetail.viewTopic = false ;
			popupContainer.setId("UIPrivatePostContainer") ;
			popupAction.activate(popupContainer, 700, 460) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
//--------------------------------	 Topic Menu		-------------------------------------------//
	static public class EditTopicActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UITopicForm topicForm = popupContainer.addChild(UITopicForm.class, null, null) ;
			topicForm.setTopicIds(topicDetail.categoryId, topicDetail.forumId, topicDetail.forum) ;
			topicForm.setUpdateTopic(topicDetail.topic, true) ;
			popupContainer.setId("UIEditTopicContainer") ;
			popupAction.activate(popupContainer, 700, 460) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			topicDetail.isEditTopic = true ;
		}
	}
	
	static public class PrintPageActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
//			UITopicDetail topicDetail = event.getSource() ;
		}
	}

	static public class AddPollActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			Topic topic = topicDetail.topic ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPollForm	pollForm = popupAction.createUIComponent(UIPollForm.class, null, null) ;
			pollForm.setTopicPath(topic.getPath()) ;
			popupAction.activate(pollForm, 655, 455) ;
			topicDetail.isEditTopic = true ;
		}
	}

	static public class SetOpenTopicActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			Topic topic = topicDetail.topic ;
			if(topic.getIsClosed()) {
				topic.setIsClosed(false) ;
				topicDetail.forumService.modifyTopic(ForumSessionUtils.getSystemProvider(), topic, 1) ;
				topicDetail.viewTopic = false ;
				topicDetail.isEditTopic = true ;
				event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail.getParent()) ;
			} else {
				Object[] args = { topic.getTopicName() };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.Open", args, ApplicationMessage.WARNING)) ;
			}
		}
	}

	static public class SetCloseTopicActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			Topic topic = topicDetail.topic ;
			if(!topic.getIsClosed()) {
				topic.setIsClosed(true) ;
				topicDetail.forumService.modifyTopic(ForumSessionUtils.getSystemProvider(), topic, 1) ;
				topicDetail.viewTopic = false ;
				topicDetail.isEditTopic = true ;
				event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail.getParent()) ;
			} else {
				Object[] args = { topic.getTopicName() };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.Close", args, ApplicationMessage.WARNING)) ;
			}
		}
	}

	static public class SetLockedTopicActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			Topic topic = topicDetail.topic ;
			if(!topic.getIsLock()) {
				topic.setIsLock(true) ;
				topicDetail.forumService.modifyTopic(ForumSessionUtils.getSystemProvider(), topic, 2) ;
				topicDetail.viewTopic = false ;
				topicDetail.isEditTopic = true ;
				event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail.getParent()) ;
			} else {
				Object[] args = { topic.getTopicName() };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.Locked", args, ApplicationMessage.WARNING)) ;
			}
		}
	}

	static public class SetUnLockTopicActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			Topic topic = topicDetail.topic ;
			if(topic.getIsLock()) {
				topic.setIsLock(false) ;
				topicDetail.forumService.modifyTopic(ForumSessionUtils.getSystemProvider(), topic, 2) ;
				topicDetail.viewTopic = false ;
				topicDetail.isEditTopic = true ;
				event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail.getParent()) ;
			} else {
				Object[] args = { topic.getTopicName() };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.UnLock", args, ApplicationMessage.WARNING)) ;
			}
		}
	}

	static public class SetMoveTopicActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIMoveTopicForm moveTopicForm = popupAction.createUIComponent(UIMoveTopicForm.class, null, null) ;
			List <Topic> topics = new ArrayList<Topic>();
			topics.add(topicDetail.topic) ;
			topicDetail.isEditTopic = true ;
			moveTopicForm.updateTopic(topicDetail.forumId, topics, true);
			popupAction.activate(moveTopicForm, 400, 420) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static public class SetStickTopicActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			Topic topic = topicDetail.topic ;
			if(!topic.getIsSticky()) {
				topic.setIsSticky(true) ;
				topicDetail.forumService.modifyTopic(ForumSessionUtils.getSystemProvider(), topic, 4) ;
				topicDetail.viewTopic = false ;
				topicDetail.isEditTopic = true ;
				event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
			} else {
				Object[] args = { topic.getTopicName() };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.Stick", args, ApplicationMessage.WARNING)) ;
			}
		}
	}

	static public class SetUnStickTopicActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			Topic topic = topicDetail.topic ;
			if(topic.getIsSticky()) {
				topic.setIsSticky(false) ;
				topicDetail.forumService.modifyTopic(ForumSessionUtils.getSystemProvider(), topic, 4) ;
				topicDetail.viewTopic = false ;
				topicDetail.isEditTopic = true ;
				event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
			} else {
				Object[] args = { topic.getTopicName() };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.UnStick", args, ApplicationMessage.WARNING)) ;
			}
		}
	}

	static public class SplitTopicActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			List<Post>list = topicDetail.getAllPost() ;
			if(list.size() > 1) {
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UISplitTopicForm splitTopicForm = popupAction.createUIComponent(UISplitTopicForm.class, null, null) ;
				splitTopicForm.setListPost(list) ;
				splitTopicForm.setTopic(topicDetail.topic) ;
				splitTopicForm.setUserProfile(topicDetail.userProfile) ;
				popupAction.activate(splitTopicForm, 700, 550) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} else {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.NotSplit", args, ApplicationMessage.WARNING)) ;
			}
    }
	}

	static public class SetApproveTopicActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
      Topic topic = topicDetail.topic;
      topic.setIsApproved(true) ;
      topicDetail.forumService.modifyTopic(ForumSessionUtils.getSystemProvider(), topic, 3) ;
      topicDetail.viewTopic = false ;
      topicDetail.isEditTopic = true ;
      event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
    }
  }
  
	static public class SetUnApproveTopicActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
      Topic topic = topicDetail.topic;
      topic.setIsApproved(false) ;
      topicDetail.forumService.modifyTopic(ForumSessionUtils.getSystemProvider(), topic, 3) ;
      topicDetail.viewTopic = false ;
      topicDetail.isEditTopic = true ;
      event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
		}
	}

	static public class SetDeleteTopicActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			Topic topic = topicDetail.topic ;
			topicDetail.forumService.removeTopic(ForumSessionUtils.getSystemProvider(), topicDetail.categoryId, topicDetail.forumId, topic.getId()) ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
			uiForumContainer.setIsRenderChild(true) ;
			UIBreadcumbs breadcumbs = forumPortlet.getChild(UIBreadcumbs.class) ;
			breadcumbs.setUpdataPath((topicDetail.categoryId + "/" + topicDetail.forumId)) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiForumContainer) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(breadcumbs) ;
		}
	}

	//---------------------------------	Post Menu	 --------------------------------------//
	static public class MergePostActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
		}
	}

	static public class MovePostActionListener extends EventListener<UITopicDetail> {
    @SuppressWarnings("unchecked")
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			List<Post> posts = new ArrayList<Post>();
			List<UIComponent> children = topicDetail.getChildren() ;
			for(UIComponent child : children) {
				if(child instanceof UIFormCheckBoxInput) {
					if(((UIFormCheckBoxInput)child).isChecked()) {
						posts.add(topicDetail.getPost(((UIFormCheckBoxInput)child).getName()));
					}
				}
			}
			if(posts.size() > 0) {
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIMovePostForm movePostForm = popupAction.createUIComponent(UIMovePostForm.class, null, null) ;
				movePostForm.updatePost(topicDetail.topicId, posts);
				popupAction.activate(movePostForm, 400, 430) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} else {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UITopicDetail.msg.notCheck", args, ApplicationMessage.WARNING)) ;
			}
		}
	}

	static public class SetApprovePostActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
      UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
      UIPageListPostUnApprove postUnApprove = popupContainer.addChild(UIPageListPostUnApprove.class, null, null) ;
      postUnApprove.setUpdateContainer(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId) ;
      popupContainer.setId("PageListPostUnApprove") ;
      popupAction.activate(popupContainer, 500, 360) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static public class SetHiddenPostActionListener extends EventListener<UITopicDetail> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
      boolean haveCheck = false ;
      Post post = new Post() ;
      List<UIComponent> children = topicDetail.getChildren() ;
      for(UIComponent child : children) {
        if(child instanceof UIFormCheckBoxInput) {
          if(((UIFormCheckBoxInput)child).isChecked()) {
            haveCheck = true ;
            post = topicDetail.getPost(((UIFormCheckBoxInput)child).getName());
            post.setIsHidden(true) ;
            topicDetail.forumService.savePost(ForumSessionUtils.getSystemProvider(), topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, post, false) ;
          }
        }
      }
      if(haveCheck) {
        event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
      } else {
        throw new MessageException(new ApplicationMessage("UITopicDetail.msg.notCheck", new String[]{}, ApplicationMessage.WARNING)) ;
      }
		}
	}

	static public class SetUnHiddenPostActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
    	UITopicDetail topicDetail = event.getSource() ;
      UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
      UIPageListPostHidden listPostHidden = popupContainer.addChild(UIPageListPostHidden.class, null, null) ;
      listPostHidden.setUpdateContainer(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId) ;
      popupContainer.setId("PageListPostHidden") ;
      popupAction.activate(popupContainer, 500, 360) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static public class SetUnApproveAttachmentActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
		}
	}
  
	static public class DeletePostActionListener extends EventListener<UITopicDetail> {
    @SuppressWarnings("unchecked")
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			List<UIComponent> children = topicDetail.getChildren() ;
			List<Post> posts = new ArrayList<Post>() ;
			for(UIComponent child : children) {
				if(child instanceof UIFormCheckBoxInput) {
					if(((UIFormCheckBoxInput)child).isChecked()) {
						posts.add(topicDetail.getPost(((UIFormCheckBoxInput)child).getName()));
					}
				}
			}
			if(posts.size() > 0) {
				for(Post post : posts) {
					topicDetail.forumService.removePost(ForumSessionUtils.getSystemProvider(), topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, post.getId()) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
				}
			} else {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UITopicDetail.msg.notCheck", args, ApplicationMessage.WARNING)) ;
			}
		}
	}

	static public class ViewPublicUserInfoActionListener extends EventListener<UITopicDetail> {
	  public void execute(Event<UITopicDetail> event) throws Exception {
	    UITopicDetail topicDetail = event.getSource() ;
      String userId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      UIViewUserProfile viewUserProfile = popupAction.createUIComponent(UIViewUserProfile.class, null, null) ;
      UserProfile userProfile ;
      if(topicDetail.mapUserProfile.containsKey(userId)){
      	userProfile = topicDetail.mapUserProfile.get(userId) ;
  		} else {
      	userProfile = topicDetail.forumService.getUserInfo(ForumSessionUtils.getSystemProvider(), userId) ;
      	User user = ForumSessionUtils.getUserByUserId(userId) ; 
      	userProfile.setUser(user) ;
  		}
      viewUserProfile.setUserProfile(userProfile) ;
      viewUserProfile.setUserProfileLogin(topicDetail.userProfile) ;
      Contact contact = null ;
      if(topicDetail.mapContact.containsKey(userId)) {
      	contact = topicDetail.mapContact.get(userId) ;
      }
      viewUserProfile.setContact(contact) ;
      popupAction.activate(viewUserProfile, 670, 400, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
	  }
	}
	static public class ViewPostedByUserActionListener extends EventListener<UITopicDetail> {
	  public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
      String userId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
      @SuppressWarnings("unused")
      UIViewPostedByUser viewPostedByUser = popupContainer.addChild(UIViewPostedByUser.class, null, null) ;
      viewPostedByUser.setUserProfile(userId) ;
      popupContainer.setId("ViewPostedByUser") ;
      popupAction.activate(popupContainer, 760, 350) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
	  }
	}
  
	static public class ViewThreadByUserActionListener extends EventListener<UITopicDetail> {
	  public void execute(Event<UITopicDetail> event) throws Exception {
	    UITopicDetail topicDetail = event.getSource() ;
	    String userId = event.getRequestContext().getRequestParameter(OBJECTID) ;
	    UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
	    UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
	    UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
	    @SuppressWarnings("unused")
	    UIViewTopicCreatedByUser topicCreatedByUser = popupContainer.addChild(UIViewTopicCreatedByUser.class, null, null) ;
      topicCreatedByUser.setUserId(userId) ;
	    popupContainer.setId("ViewTopicCreatedByUser") ;
	    popupAction.activate(popupContainer, 760, 350) ;
	    event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
	  }
	}
	
	static public class QuickReplyActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			ForumAdministration forumAdministration = topicDetail.forumService.getForumAdministration(ForumSessionUtils.getSystemProvider()) ;
			UIFormTextAreaInput textAreaInput = topicDetail.getUIFormTextAreaInput(FIELD_MESSAGE_TEXTAREA) ;
			String message = textAreaInput.getValue() ;
			boolean isOffend = false ;
			String checksms = message ;
			if(checksms != null && checksms.trim().length() > 3) {
				String stringKey = forumAdministration.getCensoredKeyword();
				if(stringKey != null && stringKey.length() > 0) {
					stringKey = stringKey.toLowerCase() ;
					String []censoredKeyword = ForumFormatUtils.splitForForum(stringKey) ;
					checksms = checksms.toLowerCase().trim();
					for (String string : censoredKeyword) {
			      if(checksms.indexOf(string.trim().toLowerCase()) >= 0) {isOffend = true ;break;}
		      }
				}
				StringBuffer buffer = new StringBuffer();
				for (int j = 0; j < message.length(); j++) {
					char c = message.charAt(j); 
					if((int)c == 9){
						buffer.append("&nbsp;&nbsp;&nbsp; ") ;
					} else if((int)c == 10){
						buffer.append("<br/>") ;
					}	else if((int)c == 60){
						buffer.append("&lt;") ;
					} else if((int)c == 62){
						buffer.append("&gt;") ;
					} else {
						buffer.append(c) ;
					}
	      }
				String userName = topicDetail.userProfile.getUserId() ;
				PortletRequestImp request = event.getRequestContext().getRequest();
				String remoteAddr = request.getRemoteAddr();
				Topic topic = topicDetail.topic ;
				Post post = new Post() ;
				post.setName("Re: " + topic.getTopicName()) ;
				post.setMessage(buffer.toString()) ;
				post.setOwner(userName) ;
				post.setRemoteAddr(remoteAddr) ;
				post.setIcon(topic.getIcon());
				post.setIsApproved(false) ;
				post.setIsHidden(isOffend) ;
				topicDetail.forumService.savePost(ForumSessionUtils.getSystemProvider(), topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, post, true) ;
        topicDetail.setUpdatePostPageList(true);
        textAreaInput.setValue("") ;
        boolean hasTopicMod = false ;
				if(topicDetail.topic != null) hasTopicMod = topicDetail.topic.getIsModeratePost() ;
				if(isOffend || hasTopicMod) {
					Object[] args = { "" };
					UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
					if(isOffend)uiApp.addMessage(new ApplicationMessage("MessagePost.msg.isOffend", args, ApplicationMessage.WARNING)) ;
					else {
						args = new Object[]{ "thread", "post" };
						uiApp.addMessage(new ApplicationMessage("MessagePost.msg.isModerate", args, ApplicationMessage.WARNING)) ;
					}
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
					topicDetail.IdPostView = "false";
				} else {
					topicDetail.IdPostView = "true";
				}
				event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
			}else {
				String[] args = new String[] { topicDetail.getLabel(FIELD_MESSAGE_TEXTAREA) } ;
				throw new MessageException(new ApplicationMessage("MessagePost.msg.message-empty", args)) ;
			}
		}
	}
	
	static public class PreviewReplyActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;	
			String message = topicDetail.getUIStringInput(FIELD_MESSAGE_TEXTAREA).getValue() ;
			String checksms = (message) ;
			if(checksms != null && checksms.trim().length() > 3) {
				StringBuffer buffer = new StringBuffer();
				for (int j = 0; j < message.length(); j++) {
					char c = message.charAt(j); 
					if((int)c == 9){
						buffer.append("&nbsp;&nbsp;&nbsp; ") ;
					} else if((int)c == 10){
						buffer.append("<br/>") ;
					}	else if((int)c == 60){
						buffer.append("&lt;") ;
					} else if((int)c == 62){
						buffer.append("&gt;") ;
					} else {
						buffer.append(c) ;
					}
	      } 
				String userName = topicDetail.userProfile.getUserId() ;
				Topic topic = topicDetail.topic ;
				Post post = new Post() ;
				post.setName("Re: " + topic.getTopicName()) ;
				post.setMessage(buffer.toString()) ;
				post.setOwner(userName) ;
				post.setRemoteAddr("") ;
				post.setIcon(topic.getIcon());
				post.setIsApproved(false) ;
				post.setCreatedDate(new Date()) ;
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class).setRendered(true)	;
				UIViewPost viewPost = popupAction.activate(UIViewPost.class, 670) ;
				viewPost.setPostView(post) ;
				viewPost.setViewUserInfo(false) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			}else {
				String[] args = new String[] { topicDetail.getLabel(FIELD_MESSAGE_TEXTAREA) } ;
				throw new MessageException(new ApplicationMessage("MessagePost.msg.message-empty", args)) ;
			}
		}
	}
  
}
