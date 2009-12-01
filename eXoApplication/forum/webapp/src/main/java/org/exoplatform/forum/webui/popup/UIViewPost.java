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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.PathNotFoundException;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.info.UIForumQuickReplyPortlet;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.UIForumContainer;
import org.exoplatform.forum.webui.UIForumDescription;
import org.exoplatform.forum.webui.UIForumLinks;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.forum.webui.UITopicDetailContainer;
import org.exoplatform.forum.webui.UITopicPoll;
import org.exoplatform.ks.bbcode.api.BBCode;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 * October 2, 2007	
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIViewPost.gtmpl",
		events = {
			@EventConfig(listeners = UIViewPost.CloseActionListener.class, phase = Phase.DECODE),
			@EventConfig(listeners = UIViewPost.ApproveActionListener.class, phase = Phase.DECODE),
			@EventConfig(listeners = UIViewPost.DeletePostActionListener.class, phase = Phase.DECODE),
			@EventConfig(listeners = UIViewPost.OpenTopicLinkActionListener.class),
			@EventConfig(listeners = UIViewPost.DownloadAttachActionListener.class, phase = Phase.DECODE)
		}
)
public class UIViewPost extends UIForm implements UIPopupComponent {
	private Post post;
	private boolean isViewUserInfo = true ;
	private ForumService forumService;
	private UserProfile userProfile;
	private List<BBCode> listBBCode = new ArrayList<BBCode>();
	public UIViewPost() {
		forumService = (ForumService) PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
	}
	
	public void setActionForm(String[] actions) {
	  this.setActions(actions);
  }
	
	@SuppressWarnings("unused")
	private UserProfile getUserProfile() throws Exception {
		try {
			userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile();
		} catch (Exception e) {
			String userName = UserHelper.getCurrentUser();
			if (userName != null) {
				try {
					userProfile = forumService.getQuickProfile(userName);
				} catch (Exception ex) {
				}
			}
		}
		return userProfile;
	}
	
	@SuppressWarnings("unused")
  private String getReplaceByBBCode(String s) throws Exception {
		try {
			s = Utils.getReplacementByBBcode(s, listBBCode, forumService);
    } catch (Exception e) {}
    return s;
	}
	
	public String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance() ;
    return pcontainer.getPortalContainerInfo().getContainerName() ;  
  }
  public String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class) ;    
    return rService.getCurrentRepository().getConfiguration().getName() ;
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

	public void setPostView(Post post) throws Exception {
		this.post = post ;
		List<String> bbcName = new ArrayList<String>();
		List<BBCode> bbcs = new ArrayList<BBCode>();
		try {
			bbcName = forumService.getActiveBBCode();
    } catch (Exception e) {
    }
    boolean isAdd = true;
    BBCode bbCode;
    for (String string : bbcName) {
    	isAdd = true;
    	for (BBCode bbc : listBBCode) {
    		if(bbc.getTagName().equals(string) || (bbc.getTagName().equals(string.replaceFirst("=", "")) && bbc.isOption())){
    			bbcs.add(bbc);
    			isAdd = false;
    			break;
    		}
    	}
    	if(isAdd) {
    		bbCode = new BBCode();
    		if(string.indexOf("=") >= 0){
    			bbCode.setOption(true);
    			string = string.replaceFirst("=", "");
    			bbCode.setId(string+"_option");
    		}else {
    			bbCode.setId(string);
    		}
    		bbCode.setTagName(string);
    		bbcs.add(bbCode);
    	}
    }
    listBBCode.clear();
    listBBCode.addAll(bbcs);
	}
	
	@SuppressWarnings("unused")
	private Post getPostView() throws Exception {
		return post ;
	}

	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	public void setViewUserInfo(boolean isView){ this.isViewUserInfo = isView ;}
	public boolean getIsViewUserInfo(){ return this.isViewUserInfo ;}
	
	static public class DownloadAttachActionListener extends EventListener<UIViewPost> {
		public void execute(Event<UIViewPost> event) throws Exception {
			UIViewPost viewPost = event.getSource() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(viewPost) ;
		}
	}
	
	static	public class ApproveActionListener extends EventListener<UIViewPost> {
		public void execute(Event<UIViewPost> event) throws Exception {
			UIViewPost uiForm = event.getSource() ;
			Post post = uiForm.post;
			post.setIsApproved(true);
			post.setIsHidden(false);
			List<Post> posts = new ArrayList<Post>();
			posts.add(post);
			try{
				uiForm.forumService.modifyPost(posts, 1);
				uiForm.forumService.modifyPost(posts, 2);
			}catch(Exception e) {
				e.printStackTrace() ;
			}
			UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			if(popupContainer != null) {
				UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
				popupAction.deActivate();
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				UIModerationForum moderationForum = popupContainer.getChild(UIModerationForum.class);
				if(moderationForum != null)
					event.getRequestContext().addUIComponentToUpdateByAjax(moderationForum) ;
			} else {
				UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.cancelAction() ;
			}
		}
	}
	
	static	public class DeletePostActionListener extends EventListener<UIViewPost> {
		public void execute(Event<UIViewPost> event) throws Exception {
			UIViewPost uiForm = event.getSource() ;
			Post post = uiForm.post;
			try{
				String []path = post.getPath().split("/");
				int l = path.length ;
				uiForm.forumService.removePost(path[l-4], path[l-3], path[l-2], post.getId());
			}catch(Exception e) {
				e.printStackTrace() ;
			}
			UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			if(popupContainer != null) {
				UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
				popupAction.deActivate();
				UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			} else {
				UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.cancelAction() ;
			}
		}
	}

	static	public class OpenTopicLinkActionListener extends EventListener<UIViewPost> {
		public void execute(Event<UIViewPost> event) throws Exception {
			UIViewPost uiForm = event.getSource() ;
			Post post = uiForm.post;
			UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
			if(post == null){
				uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", null, ApplicationMessage.WARNING)) ;
				return ;
			}
			boolean isRead = true;
			Topic topic = null;
			Category category = null;
			Forum forum = null;
			if(uiForm.userProfile.getUserRole() > 0) {
				String path =	post.getPath();
				String []id = path.split("/") ;
				int l = id.length;
				try {
					category = uiForm.forumService.getCategory(id[l-4]);
					if(category == null) {
						uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", null, ApplicationMessage.WARNING)) ;
						return ;
					}
					String[] privateUser = category.getUserPrivate();
					if(privateUser != null && privateUser.length > 0) {
						if(privateUser.length ==1 && privateUser[0].equals(" ")){
							isRead = true;
						} else {
							isRead = ForumServiceUtils.hasPermission(privateUser, uiForm.userProfile.getUserId());
						}
					}
					if(isRead) {
						String path_ = "" ;
						forum = uiForm.forumService.getForum(id[l-4] , id[l-3] ) ;
						if(forum != null ) path_ = forum.getPath()+"/"+id[l-2] ;
						topic = uiForm.forumService.getTopicByPath(path_, false) ;
						if(forum == null || topic == null) {
							String[] s = new String[]{};
							uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.do-not-permission", s, ApplicationMessage.WARNING)) ;
							return;
						}
						if(uiForm.userProfile.getUserRole() == 1 && (forum.getModerators() != null && forum.getModerators().length > 0 && 
								ForumServiceUtils.hasPermission(forum.getModerators(), uiForm.userProfile.getUserId()))) isRead = true;
						else isRead = false;
						
						if(!isRead && !forum.getIsClosed()){
							// check for topic:
							if(!isRead && post.getIsActiveByTopic() && post.getIsApproved() && !post.getIsHidden() && topic.getIsActive() &&
									topic.getIsActiveByForum() && topic.getIsApproved() && !topic.getIsClosed() && !topic.getIsWaiting()){
								List<String> list = new ArrayList<String>();
								list = ForumUtils.addArrayToList(list, topic.getCanView());
								list = ForumUtils.addArrayToList(list, forum.getViewer());
								list = ForumUtils.addArrayToList(list, uiForm.forumService.getPermissionTopicByCategory(id[l-4], "viewer"));
								if(!list.isEmpty())list.add(topic.getOwner());
								if(!list.isEmpty() && ForumServiceUtils.hasPermission(list.toArray(new String[]{}), uiForm.userProfile.getUserId())) isRead = true;
								else isRead = false;
							} else {
								isRead = false;
							}
						}
					}
				} catch (Exception e) {
					String[] s = new String[]{};
					uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", s, ApplicationMessage.WARNING)) ;
				}
			}
			if(isRead){
				UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.updateIsRendered(ForumUtils.FORUM);
				UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
				UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
				uiForumContainer.setIsRenderChild(false) ;
				UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
				if(uiForm.userProfile.getUserRole() > 0){
					uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
					uiTopicDetail.setUpdateForum(forum) ;
					uiTopicDetail.setTopicFromCate(category.getId(), forum.getId(), topic, 0) ;
					uiTopicDetail.setIdPostView(post.getId()) ;
					uiTopicDetail.setLastPostId(post.getId());
					uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(category.getId(), forum.getId(), topic.getId()) ;
					forumPortlet.getChild(UIForumLinks.class).setValueOption((category.getId()+"/"+forum.getId() + " "));
				} else {
					String []id = post.getPath().split("/") ;
					int l = id.length;
					String categoryId=id[l-4], forumId=id[l-3], topicId=id[l-2];
					forum = uiForm.forumService.getForum(categoryId , forumId) ;
					uiTopicDetail.setUpdateForum(forum);
					uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
					uiTopicDetail.setUpdateTopic(categoryId, forumId, topicId);
					uiTopicDetail.setIdPostView(post.getId()) ;
					uiTopicDetail.setLastPostId(post.getId());
					uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(categoryId, forumId, topicId) ;
					forumPortlet.getChild(UIForumLinks.class).setValueOption((categoryId+"/"+forumId + " "));
				}
				forumPortlet.cancelAction();
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			} else {
				String[] s = new String[]{};
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.do-not-permission", s, ApplicationMessage.WARNING)) ;
				return;
			}
		}
	}
	
	static	public class CloseActionListener extends EventListener<UIViewPost> {
		public void execute(Event<UIViewPost> event) throws Exception {
			UIViewPost uiForm = event.getSource() ;
			UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			if(popupContainer != null) {
				UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
				popupAction.deActivate();
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} else {
				try {
					UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
					forumPortlet.cancelAction() ;
        } catch (Exception e) {
        	UIForumQuickReplyPortlet forumPortlet = uiForm.getAncestorOfType(UIForumQuickReplyPortlet.class) ;
        	forumPortlet.cancelAction() ;
        }
			}
		}
	}

}
