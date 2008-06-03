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

import java.util.Date;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupMessages;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
/**
 * Author : Nguyen Quang Hung
 *					hung.nguyen@exoplatform.com
 * Aug 01, 2007
 */
@ComponentConfig(
	 lifecycle = UIApplicationLifecycle.class, 
	 template = "app:/templates/forum/webui/UIForumPortlet.gtmpl"
)
public class UIForumPortlet extends UIPortletApplication {
	private	ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private boolean isCategoryRendered = true;
	private boolean isForumRendered = false;
	private boolean isTagRendered = false;
	private boolean isSeachRendered = false;
	private boolean isJumpRendered = false;
	private boolean isLogin = true ;
	private UserProfile userProfile = null;
	public UIForumPortlet() throws Exception {
		addChild(UIBreadcumbs.class, null, null) ;
		addChild(UICategoryContainer.class, null, null).setRendered(isCategoryRendered) ;
		addChild(UIForumContainer.class, null, null).setRendered(isForumRendered) ;
		addChild(UITopicsTag.class, null, null).setRendered(isTagRendered) ;
		addChild(UISearchForm.class, null, null).setRendered(isSeachRendered) ;
		addChild(UIForumLinks.class, null, null).setRendered(false) ;
		addChild(UIPopupAction.class, null, "UIForumPopupAction") ;
	}

	public void updateIsRendered(String selected) throws Exception {
		if(selected == ForumUtils.CATEGORIES) {
			isCategoryRendered = true ;
			isForumRendered = false ;
			isTagRendered = false ;
			isSeachRendered = false ;
		} else if(selected == ForumUtils.FORUM) {
			isForumRendered = true ;
			isCategoryRendered = false ;
			isTagRendered = false ;
			isSeachRendered = false ;
		} else if(selected == ForumUtils.TAG) {
			isTagRendered = true ;
			isForumRendered = false ;
			isCategoryRendered = false ;
			isSeachRendered = false ;
		} else {
			isTagRendered = false ;
			isForumRendered = false ;
			isCategoryRendered = false ;
			isSeachRendered = true ;
		}
		UICategoryContainer categoryContainer = getChild(UICategoryContainer.class).setRendered(isCategoryRendered) ;
		categoryContainer.setIsRenderJump(isJumpRendered) ;
		UIForumContainer forumContainer = getChild(UIForumContainer.class).setRendered(isForumRendered) ;
		forumContainer.setIsRenderJump(isJumpRendered) ;
		getChild(UITopicsTag.class).setRendered(isTagRendered) ;
		getChild(UISearchForm.class).setRendered(isSeachRendered) ;
	}
	
	@SuppressWarnings("unused")
  private boolean  getIsJumpRendered() {
		return isJumpRendered ;
	}

	public void renderPopupMessages() throws Exception {
		UIPopupMessages popupMess = getUIPopupMessages();
		if(popupMess == null)	return ;
		WebuiRequestContext	context =	RequestContext.getCurrentInstance() ;
		popupMess.processRender(context);
	}

	public void cancelAction() throws Exception {
		WebuiRequestContext context = RequestContext.getCurrentInstance() ;
		UIPopupAction popupAction = getChild(UIPopupAction.class) ;
		popupAction.deActivate() ;
		context.addUIComponentToUpdateByAjax(popupAction) ;
	}
	
	public UserProfile getUserProfile() {
	  return this.userProfile ;
  }
  @SuppressWarnings("deprecation")
	public void setUserProfile() throws Exception {
  	String userId = "" ;
  	try {
  		userId = ForumSessionUtils.getCurrentUser() ;
    } catch (Exception e) {
    	e.printStackTrace() ;
    }
    Date date = null ;
    if(this.userProfile != null) {
    	date = this.userProfile.getLastLoginDate() ;
    }
  	this.userProfile = forumService.getUserProfile(ForumSessionUtils.getSystemProvider(), userId, true, true, isLogin) ;
  	this.isLogin = false;
  	this.userProfile.setIsOnline(true) ;
  	if(date != null)
  		this.userProfile.setLastLoginDate(date) ;
  	this.isJumpRendered = this.userProfile.getIsShowForumJump() ;
  }
}