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
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * 06-03-2008, 04:41:47
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
		template =	"app:/templates/forum/webui/popup/UIPageListPostUnApprove.gtmpl",
		events = {
			@EventConfig(listeners = UIPageListPostUnApprove.OpenPostLinkActionListener.class),
			@EventConfig(listeners = UIPageListPostUnApprove.UnApproveActionListener.class),
			@EventConfig(listeners = UIPageListPostUnApprove.CancelActionListener.class,phase = Phase.DECODE )
		}
)
public class UIPageListPostUnApprove extends UIForm implements UIPopupComponent {
	private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private String categoryId, forumId, topicId ;
  private List<Post> listPost = new ArrayList<Post>() ;
  
	public UIPageListPostUnApprove() throws Exception {
		addChild(UIForumPageIterator.class, null, "PageListPostUnApprove") ;
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
		UIForumPageIterator forumPageIterator = this.getChild(UIForumPageIterator.class) ;
		JCRPageList pageList  = forumService.getPosts(ForumSessionUtils.getSystemProvider(), this.categoryId, this.forumId, this.topicId, "false", "");
		forumPageIterator.updatePageList(pageList) ;
		pageList.setPageSize(6) ;
		long page = forumPageIterator.getPageSelected() ;
		List<Post> posts = pageList.getPage(page) ;
    if(!posts.isEmpty()) {
      for (Post post : posts) {
        if(getUIFormCheckBoxInput(post.getId()) != null) {
          getUIFormCheckBoxInput(post.getId()).setChecked(false) ;
        }else {
          addUIFormInput(new UIFormCheckBoxInput(post.getId(), post.getId(), false) );
        }
      }
    }
    this.listPost = posts ;
		return posts ;
	}
  
  private Post getPost(String postId) {
    for (Post post : this.listPost) {
      if(post.getId().equals(postId)) return post ;
    }
    return null ;
  }
	
	static	public class OpenPostLinkActionListener extends EventListener<UIPageListPostUnApprove> {
    public void execute(Event<UIPageListPostUnApprove> event) throws Exception {
			UIPageListPostUnApprove uiForm = event.getSource() ;
			String postId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      Post post = uiForm.getPost(postId) ;
      UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
      UIViewTopic viewTopic = popupAction.activate(UIViewTopic.class, 700) ;
      viewTopic.setPostView(post) ;
      viewTopic.setViewUserInfo(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

  static  public class UnApproveActionListener extends EventListener<UIPageListPostUnApprove> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UIPageListPostUnApprove> event) throws Exception {
      UIPageListPostUnApprove postUnApprove = event.getSource() ;
      List<UIComponent>listChild = postUnApprove.getChildren() ;
      //List<Post> listPost = new ArrayList<Post>() ;
      Post post = new Post() ;
      boolean haveCheck = false ;
      for (UIComponent child : listChild) {
        if (child instanceof UIFormCheckBoxInput) {
          if(((UIFormCheckBoxInput)child).isChecked()) {
            haveCheck = true ;
            post = postUnApprove.getPost(child.getName()) ;
            post.setIsApproved(true) ;
            postUnApprove.forumService.savePost(ForumSessionUtils.getSystemProvider(), postUnApprove.categoryId, postUnApprove.forumId, postUnApprove.topicId, post, false) ;
          }
        }
      }
      if(!haveCheck) {
        throw new MessageException(new ApplicationMessage("UIPageListPostUnApprove.sms.notCheck", null)) ;
      }
      UIForumPortlet forumPortlet = postUnApprove.getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
      UITopicDetail topicDetail = forumPortlet.findFirstComponentOfType(UITopicDetail.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIPageListPostUnApprove> {
    public void execute(Event<UIPageListPostUnApprove> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
    }
  }
}
