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

import org.exoplatform.chrome.api.annotations.Name;
import org.exoplatform.chrome.api.annotations.NodeMapping;
import org.exoplatform.chrome.api.annotations.Path;
import org.exoplatform.chrome.api.annotations.Property;
import org.exoplatform.forum.service.Category;

@NodeMapping(name = "exo:forumCategory")
public abstract class CategoryMapping implements Category {

  @Name
  public abstract String getCategoryName() ;
  
  @Name
  public abstract String getId(); 
  
  @Path
  public abstract String getPath();
  
  @Property(name = "exo:owner")
  public abstract String getOwner();

  @Property(name = "exo:categoryOrder")
  public abstract long getCategoryOrder();
  
  //@Property(name = "exo:createTopicRole")
  public abstract String[] getCreateTopicRole();
  
  @Property(name = "exo:createdDate")
  public abstract Date getCreatedDate();
  
  @Property(name = "exo:description")
  public abstract String getDescription();
  
  //@Property(name = "exo:emailWatching")
  public abstract String[] getEmailNotification();
  
  @Property(name = "exo:forumCount")
  public abstract long getForumCount() ;

  //@Property(name = "exo:moderators")
  public abstract String[] getModerators();
  
  @Property(name = " exo:modifiedBy")
  public abstract String getModifiedBy() ;
  
  @Property(name = "exo:modifiedDate")
  public abstract Date getModifiedDate();

  //@Property(name = "exo:poster")
  public abstract String[] getPoster() ;
    
  //@Property(name = "exo:userPrivate")
  public abstract String[] getUserPrivate();

  //@Property(name = "exo:viewer")
  public abstract String[] getViewer() ;

  public abstract void setCategoryName(String categoryName) ;

  public abstract void setCategoryOrder(long categoryOrder) ;

  public abstract void setCreateTopicRole(String[] createTopicRole);

  public abstract void setCreatedDate(Date createdDate);

  public abstract void setDescription(String description);

  public abstract void setEmailNotification(String[] emailNotification);

  public abstract void setForumCount(long forumCount);

  public abstract void setId(String id);

  public abstract void setModerators(String[] moderators);

  public abstract void setModifiedBy(String modifiedBy);

  public abstract void setModifiedDate(Date modifiedDate);

  public abstract void setOwner(String owner);

  public abstract void setPath(String path);

  public abstract void setPoster(String[] poster) ;

  public abstract void setUserPrivate(String[] userPrivate) ;

  public abstract void setViewer(String[] viewer);

}
