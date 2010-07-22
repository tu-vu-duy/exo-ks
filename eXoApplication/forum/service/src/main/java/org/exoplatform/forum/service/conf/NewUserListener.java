/*
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
 */
package org.exoplatform.forum.service.conf;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumStatistic;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;

/**
 * Created by The eXo Platform SARL Author : Hung Nguyen Quang
 * hung.nguyen@exoplatform.com Nov 23, 2007 3:09:21 PM
 */
public class NewUserListener extends UserEventListener {

  //private ForumService fservice_ ;
  public NewUserListener(InitParams params) throws Exception {
    //fservice_ = fservice;
  }

  public void postSave(User user, boolean isNew) throws Exception {
  	ForumService fservice = (ForumService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class) ;
    if(isNew) {
	    //Update forum statistic
	    try{    	    	
	    	ForumStatistic statistic = fservice.getForumStatistic() ;
	      statistic.setNewMembers(user.getUserName()) ;
	      statistic.setMembersCount(statistic.getMembersCount() + 1) ;
	      fservice.saveForumStatistic(statistic) ;
	    }catch(Exception e) {
	    	e.printStackTrace() ;
	    }
	    //Create Profile
	    try{
	    	fservice.createUserProfile(user) ;
	    }catch(Exception e) {
	    	e.printStackTrace() ;
	    }
    } else {
    	fservice.updateUserProfile(user);
    }
  }

  @Override
  public void postDelete(User user) throws Exception {
    ForumService fservice = (ForumService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class) ;
    try{
    	fservice.removeProfile(user.getUserName()) ;
    	ForumStatistic statistic = fservice.getForumStatistic() ;
      if(user.getUserName().equals(statistic.getNewMembers())) {
      	fservice.updateForumStatistic() ;
      }else {
      	statistic.setMembersCount(statistic.getMembersCount() - 1) ;
        fservice.saveForumStatistic(statistic) ;
      }      
    }catch(Exception e) {
    	e.printStackTrace() ;
    }
  }
}