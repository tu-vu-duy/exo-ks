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

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jul 18, 2009  
 */
public interface Category {

  public abstract String getId();

  public abstract void setId(String id);

  public abstract String getOwner();

  public abstract void setOwner(String owner);

  public abstract String getPath();

  public abstract void setPath(String path);

  public abstract long getCategoryOrder();

  public abstract void setCategoryOrder(long categoryOrder);

  public abstract Date getCreatedDate();

  public abstract void setCreatedDate(Date createdDate);

  public abstract String getModifiedBy();

  public abstract void setModifiedBy(String modifiedBy);

  public abstract Date getModifiedDate();

  public abstract void setModifiedDate(Date modifiedDate);

  public abstract String getCategoryName();

  public abstract void setCategoryName(String categoryName);

  public abstract String getDescription();

  public abstract void setDescription(String description);

  public abstract String[] getModerators();

  public abstract void setModerators(String[] moderators);

  public abstract String[] getUserPrivate();

  public abstract void setUserPrivate(String[] userPrivate);

  public abstract String[] getCreateTopicRole();

  public abstract void setCreateTopicRole(String[] createTopicRole);

  public abstract String[] getPoster();

  public abstract void setPoster(String[] poster);

  public abstract String[] getViewer();

  public abstract void setViewer(String[] viewer);

  public abstract long getForumCount();

  public abstract void setForumCount(long forumCount);

  public abstract String[] getEmailNotification();

  public abstract void setEmailNotification(String[] emailNotification);

}
