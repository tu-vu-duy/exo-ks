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
package org.exoplatform.forum.service;

import java.util.Date;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jul 18, 2009  
 */
public interface Forum {

  public abstract String getId();

  public abstract void setId(String id);

  /**
   * This method should:
   * Calculate the category id	base on the forum id
   * @return The category id
   */
  public abstract String getCategoryId();

  public abstract String getOwner();

  public abstract void setOwner(String owner);

  public abstract String getPath();

  public abstract void setPath(String path);

  public abstract int getForumOrder();

  public abstract void setForumOrder(int forumOrder);

  public abstract Date getCreatedDate();

  public abstract void setCreatedDate(Date createdDate);

  public abstract String getModifiedBy();

  public abstract void setModifiedBy(String modifiedBy);

  public abstract Date getModifiedDate();

  public abstract void setModifiedDate(Date modifiedDate);

  public abstract String getLastTopicPath();

  public abstract void setLastTopicPath(String lastTopicPath);

  public abstract String getForumName();

  public abstract void setForumName(String forumName);

  public abstract String getDescription();

  public abstract void setDescription(String description);

  public abstract long getPostCount();

  public abstract void setPostCount(long postCount);

  public abstract long getTopicCount();

  public abstract void setTopicCount(long topicCount);

  public abstract String[] getNotifyWhenAddTopic();

  public abstract void setNotifyWhenAddTopic(String[] notifyWhenAddTopic);

  public abstract String[] getNotifyWhenAddPost();

  public abstract void setNotifyWhenAddPost(String[] notifyWhenAddPost);

  public abstract boolean getIsModerateTopic();

  public abstract void setIsModerateTopic(boolean isModerateTopic);

  public abstract boolean getIsModeratePost();

  public abstract void setIsModeratePost(boolean isModeratePost);

  public abstract boolean getIsClosed();

  public abstract void setIsClosed(boolean isClosed);

  public abstract boolean getIsLock();

  public abstract void setIsLock(boolean isLock);

  public abstract String[] getCreateTopicRole();

  public abstract void setCreateTopicRole(String[] createTopicRole);

  public abstract String[] getPoster();

  public abstract void setPoster(String[] poster);

  public abstract String[] getViewer();

  public abstract void setViewer(String[] viewer);

  public abstract String[] getModerators();

  public abstract void setModerators(String[] moderators);

  public abstract String[] getEmailNotification();

  public abstract void setEmailNotification(String[] emailNotification);

  public abstract List<String> getBanIP();

  public abstract void setBanIP(List<String> banIPs);

  public abstract boolean getIsAutoAddEmailNotify();

  public abstract void setIsAutoAddEmailNotify(boolean isAutoAddEmailNotify);

}
