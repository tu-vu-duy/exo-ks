/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.forum.service.impl.mapping;

import java.util.Date;
import java.util.List;

import org.exoplatform.chrome.api.annotations.Name;
import org.exoplatform.chrome.api.annotations.NodeMapping;
import org.exoplatform.chrome.api.annotations.Path;
import org.exoplatform.chrome.api.annotations.Property;
import org.exoplatform.forum.service.Forum;



@NodeMapping(name = "exo:forum")
public abstract class ForumMapping implements Forum {

  @Path
  public abstract String getPath() ;
  
  @Name
  public abstract String getId();
  
  @Property(name = "exo:owner")
  public abstract String getOwner();
  
  @Property(name = "exo:name")
  public abstract String getForumName();

  @Property(name = "exo:forumOrder")
  public abstract int getForumOrder();
  
  @Property(name = "exo:createdDate")
  public abstract Date getCreatedDate();
  
  @Property(name = " exo:modifiedBy")
  public abstract String getModifiedBy() ;
  
  @Property(name = "exo:modifiedDate")
  public abstract Date getModifiedDate();  
  
  @Property(name = "exo:description")
  public abstract String getDescription();
  
  @Property(name = "exo:topicCount")
  public abstract long getTopicCount() ;

  @Property(name = "exo:postCount")
  public abstract long getPostCount() ;
  
  @Property(name = "exo:emailWatching")
  public abstract String[] getEmailNotification();  
  
  @Property(name = "exo:moderators")
  public abstract String[] getModerators();
  
  @Property(name = "exo:poster")
  public abstract String[] getPoster() ;
  
  @Property(name = "exo:viewer")
  public abstract String[] getViewer() ; 
    
  @Property(name = "exo:userPrivate")
  public abstract String[] getUserPrivate();

  @Property(name = "exo:isClosed")
  public abstract boolean getIsClosed();
  
  @Property(name = "exo:banIPs")
  public abstract List<String> getBanIP();

  @Property(name = "exo:isModerateTopic")
  public abstract boolean getIsModeratePost();

  @Property(name = "exo:isModeratePost")
  public abstract boolean getIsModerateTopic();

  @Property(name = "exo:createTopicRole")
  public abstract String [] getCreateTopicRole();
  
  @Property(name = "exo:isAutoAddEmailNotify") 
 public abstract boolean getIsAutoAddEmailNotify();
  
  @Property(name = "exo:isLock") 
   public abstract boolean getIsLock();

  @Property(name = "exo:notifyWhenAddPost") 
   public abstract String [] getNotifyWhenAddPost();
  
  @Property(name = "exo:notifyWhenAddTopic")
   public abstract String [] getNotifyWhenAddTopic();

  @Property(name = "exo:lastTopicPath")
  public abstract String getLastTopic();
  
  
  public String getLastTopicPath() {
    String result = null;
    if (getLastTopic() != null) {
      result = getLastTopic() ;
      if(result.trim().length() > 0){
        if(result.lastIndexOf("/") > 0){
          result = getPath() + result.substring(result.lastIndexOf("/"));
        } else {
          result = getPath() + "/" + result;
        }
      }  
    }
    return result;
  }
  
  public void setLastTopicPath(String lastTopicPath) {
    setLastTopic(lastTopicPath);
  }

  public String getCategoryId(){
    if(getPath() != null && getPath().length() > 0) {
      String[] arr = getPath().split("/");
      String result = arr[arr.length - 2];
      return result;
    }
    return null;
  }

  public abstract void setBanIP(List<String> banIPs);
  
  public abstract void setCreateTopicRole(String[] createTopicRole) ;

  public abstract void setCreatedDate(Date createdDate);

  public abstract void setDescription(String description);

  public abstract void setEmailNotification(String[] emailNotification);

  public abstract void setForumName(String forumName) ;

  public abstract void setForumOrder(int forumOrder) ;

  public abstract void setId(String id) ;

  public abstract void setIsAutoAddEmailNotify(boolean isAutoAddEmailNotify);

  public abstract void setIsClosed(boolean isClosed);

  public abstract void setIsLock(boolean isLock);

  public abstract void setIsModeratePost(boolean isModeratePost);

  public abstract void setIsModerateTopic(boolean isModerateTopic);

  public abstract void setLastTopic(String lastTopicPath);

  public abstract void setModerators(String[] moderators);

  public abstract void setModifiedBy(String modifiedBy);

  public abstract void setModifiedDate(Date modifiedDate);

  public abstract void setNotifyWhenAddPost(String[] notifyWhenAddPost) ;

  public abstract void setNotifyWhenAddTopic(String[] notifyWhenAddTopic);

  public abstract void setOwner(String owner);

  public abstract void setPath(String path);

  public abstract void setPostCount(long postCount);

  public abstract void setPoster(String[] poster);

  public abstract void setTopicCount(long topicCount) ;

  public abstract void setViewer(String[] viewer);

}
