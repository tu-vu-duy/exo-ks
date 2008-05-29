/***************************************************************************
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
 ***************************************************************************/
package org.exoplatform.forum.webui.popup;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumPathNotFoundException;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.UIBreadcumbs;
import org.exoplatform.forum.webui.UICategory;
import org.exoplatform.forum.webui.UICategoryContainer;
import org.exoplatform.forum.webui.UIForumContainer;
import org.exoplatform.forum.webui.UIForumDescription;
import org.exoplatform.forum.webui.UIForumLinks;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicContainer;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.forum.webui.UITopicDetailContainer;
import org.exoplatform.forum.webui.UITopicPoll;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Apr 30, 2008 - 8:19:21 AM  
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIShowBookMarkForm.gtmpl",
		events = {
			@EventConfig(listeners = UIShowBookMarkForm.OpenLinkActionListener.class, phase=Phase.DECODE), 
			@EventConfig(listeners = UIShowBookMarkForm.DeleteLinkActionListener.class), 
			@EventConfig(listeners = UIShowBookMarkForm.CancelActionListener.class, phase=Phase.DECODE)
		}
)
public class UIShowBookMarkForm extends UIForm implements UIPopupComponent{
	ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private UserProfile userProfile ;
	public UIShowBookMarkForm() {
	  // TODO Auto-generated constructor stub
  }
	
  public void activate() throws Exception {  }
  public void deActivate() throws Exception {  }
  
  @SuppressWarnings("unused")
  private String[] getBookMark() throws Exception {
  	String []bookMark = new String[]{}; 
  	this.userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
  	bookMark = this.userProfile.getBookmark() ;
  	return bookMark ;
  } 
  
  //TODO: Need to remove and make new code for below action !!!
  static  public class OpenLinkActionListener extends EventListener<UIShowBookMarkForm> {
    public void execute(Event<UIShowBookMarkForm> event) throws Exception {
    	UIShowBookMarkForm bookMark = event.getSource() ;
    	String path = event.getRequestContext().getRequestParameter(OBJECTID)	;
    	UIForumPortlet forumPortlet = bookMark.getAncestorOfType(UIForumPortlet.class) ;
    	UIApplication uiApp = bookMark.getAncestorOfType(UIApplication.class) ;
    		if(path.indexOf("topic") > 0) {
      		String []id = path.split("/") ;
      		int length = id.length ;
      		forumPortlet.updateIsRendered(2);
    			UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
    			UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
    			uiForumContainer.setIsRenderChild(false) ;
    			UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
    			Forum forum = bookMark.forumService.getForum(ForumSessionUtils.getSystemProvider(),id[length-3] , id[length-2] ) ;
    			Topic topic = bookMark.forumService.getTopicByPath(ForumSessionUtils.getSystemProvider(), path, false) ;
    			if(forum == null || topic == null) {
    				uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", null, ApplicationMessage.WARNING)) ;
    				return ;
    			}
    			uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
    			uiTopicDetail.setTopicFromCate(id[length-3], id[length-2] , topic, true) ;
    			uiTopicDetail.setUpdateForum(forum) ;
    			uiTopicDetail.setIdPostView("true") ;
    			uiTopicDetailContainer.getChild(UITopicPoll.class).updatePoll(id[length-3], id[length-2] , topic) ;
    			forumPortlet.getChild(UIForumLinks.class).setValueOption((id[length-3] + "/" + id[length-2] + " "));
      	} else if(path.indexOf("forum") > 0){
      		String []id = path.split("/") ;
      		int length = id.length ;
      		Forum forum = bookMark.forumService.getForum(ForumSessionUtils.getSystemProvider(),id[length-2] , id[length-1] ) ;
      		if(forum == null) {
    				uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", null, ApplicationMessage.WARNING)) ;
    				return ;
    			}
    			forumPortlet.updateIsRendered(2);
    			UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
    			uiForumContainer.setIsRenderChild(true) ;
    			uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
    			UITopicContainer uiTopicContainer = uiForumContainer.getChild(UITopicContainer.class) ;
    			uiTopicContainer.setUpdateForum(id[length-2], forum) ;
    			forumPortlet.getChild(UIForumLinks.class).setValueOption((id[length-2]+"/"+id[length-1]));
  			} else if(path.indexOf("category") > 0){
  				String categoryId = path.substring(path.lastIndexOf("/")+1) ;
  				try {
  					forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(categoryId);
  				}catch(ForumPathNotFoundException e) {
  					uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", null, ApplicationMessage.WARNING)) ;
    				return ;
  				}
      		UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
  				categoryContainer.getChild(UICategory.class).updateByBreadcumbs(categoryId) ;
  				categoryContainer.updateIsRender(false) ;
  				forumPortlet.updateIsRendered(1);
  				
  			} else {
  				return;
  			}
      forumPortlet.cancelAction() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
    }
  }

  static  public class DeleteLinkActionListener extends EventListener<UIShowBookMarkForm> {
  	public void execute(Event<UIShowBookMarkForm> event) throws Exception {
  		String path = event.getRequestContext().getRequestParameter(OBJECTID)	;
  		UIShowBookMarkForm bookMark = event.getSource() ;
  		bookMark.forumService.saveUserBookmark(ForumSessionUtils.getSystemProvider(), bookMark.userProfile.getUserId(), path, false) ;
  		UIForumPortlet forumPortlet = bookMark.getAncestorOfType(UIForumPortlet.class) ;
  		forumPortlet.setUserProfile() ;
  		event.getRequestContext().addUIComponentToUpdateByAjax(bookMark.getParent()) ;
  	}
  }

  static  public class CancelActionListener extends EventListener<UIShowBookMarkForm> {
  	public void execute(Event<UIShowBookMarkForm> event) throws Exception {
  		UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
  		forumPortlet.cancelAction() ;
  	}
  }
}
