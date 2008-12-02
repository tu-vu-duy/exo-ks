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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.UIForumKeepStickPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 * 06-03-2008, 04:41:47
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template =	"app:/templates/forum/webui/popup/UIPageListPostUnApprove.gtmpl",
		events = {
			@EventConfig(listeners = UIPageListPostUnApprove.OpenPostLinkActionListener.class),
			@EventConfig(listeners = UIPageListPostUnApprove.UnApproveActionListener.class),
			@EventConfig(listeners = UIPageListPostUnApprove.CancelActionListener.class,phase = Phase.DECODE ),
			@EventConfig(listeners = UIForumKeepStickPageIterator.GoPageActionListener.class)
		}
)
public class UIPageListPostUnApprove extends UIForumKeepStickPageIterator implements UIPopupComponent {
	private ForumService forumService ;
	private String categoryId, forumId, topicId ;
	private List<Post> listAllPost = new ArrayList<Post>() ;
	
	public UIPageListPostUnApprove() throws Exception {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
		this.setActions(new String[] {"UnApprove","Cancel"});
	}

	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	@SuppressWarnings("unused")
	private UserProfile getUserProfile() throws Exception {
		return this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
	}
	
	public void setUpdateContainer(String categoryId, String forumId, String topicId) {
		this.categoryId = categoryId ; this.forumId = forumId ; this.topicId = topicId ;
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	private List<Post> getPosts() throws Exception {
		pageList	= forumService.getPosts(ForumSessionUtils.getSystemProvider(), this.categoryId, this.forumId, this.topicId, "false", "", "", "");
//		this.updatePageList(pageList) ;
		pageList.setPageSize(6) ;
		List<Post> posts = pageList.getPage(pageSelect);
		pageSelect = pageList.getCurrentPage();
		if(posts == null) posts = new ArrayList<Post>();
		if(!posts.isEmpty()) {
			for (Post post : posts) {
				if(getUIFormCheckBoxInput(post.getId()) != null) {
					getUIFormCheckBoxInput(post.getId()).setChecked(false) ;
				}else {
					addUIFormInput(new UIFormCheckBoxInput(post.getId(), post.getId(), false) );
				}
			}
		}
		this.listAllPost = pageList.getPage(0);
		return posts ;
	}
	
	private Post getPost(String postId) {
		for (Post post : this.listAllPost) {
			if(post.getId().equals(postId)) return post ;
		}
		return null ;
	}

	@SuppressWarnings("unchecked")
	private List<String> getIdSelected() throws Exception{
		List<UIComponent> children = this.getChildren() ;
		List<String> ids = new ArrayList<String>() ;
		for (int i = 0; i <= this.maxPage; i++) {
			if(this.getListChecked(i) != null)ids.addAll(this.getListChecked(i));
		}
		for(UIComponent child : children) {
			if(child instanceof UIFormCheckBoxInput) {
				if(((UIFormCheckBoxInput)child).isChecked()) {
					if(!ids.contains(child.getName()))ids.add(child.getName());
				}
			}
		}
		this.cleanCheckedList();
		return ids;
	}
	static	public class OpenPostLinkActionListener extends EventListener<UIPageListPostUnApprove> {
		public void execute(Event<UIPageListPostUnApprove> event) throws Exception {
			UIPageListPostUnApprove uiForm = event.getSource() ;
			String postId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			Post post = uiForm.getPost(postId) ;
			UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
			UIViewPost viewPost = popupAction.activate(UIViewPost.class, 700) ;
			viewPost.setPostView(post) ;
			viewPost.setViewUserInfo(false) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static	public class UnApproveActionListener extends EventListener<UIPageListPostUnApprove> {
		public void execute(Event<UIPageListPostUnApprove> event) throws Exception {
			UIPageListPostUnApprove postUnApprove = event.getSource() ;
			Post post = new Post() ;
			boolean haveCheck = false ;
			List<Post> posts = new ArrayList<Post>();
			for (String postId : postUnApprove.getIdSelected()) {
				haveCheck = true ;
				post = postUnApprove.getPost(postId) ;
				if(post != null) {
					post.setIsApproved(true) ;
					posts.add(post) ;
				}
			}
			if(!haveCheck) {
				throw new MessageException(new ApplicationMessage("UIPageListPostUnApprove.sms.notCheck", null)) ;
			} else {
				SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
				try {
					postUnApprove.forumService.modifyPost(sProvider, posts, 1) ;
				}finally {
					sProvider.close();
				}
			}
			if(posts.size() == postUnApprove.listAllPost.size()) {
				UIForumPortlet forumPortlet = postUnApprove.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.cancelAction() ;
				UITopicDetail topicDetail = forumPortlet.findFirstComponentOfType(UITopicDetail.class) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
			} else {
				event.getRequestContext().addUIComponentToUpdateByAjax(postUnApprove.getParent()) ;
			}
		}
	}
	
	static	public class CancelActionListener extends EventListener<UIPageListPostUnApprove> {
		public void execute(Event<UIPageListPostUnApprove> event) throws Exception {
			UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}
}
