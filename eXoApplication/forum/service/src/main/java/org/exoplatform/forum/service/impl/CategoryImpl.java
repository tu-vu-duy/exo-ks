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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.service.impl ;

import java.util.Date;

import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * March 2, 2007	
 */
public class CategoryImpl implements Category {
  
	private String id;
  
	private String owner;
	private String path;
	private long categoryOrder;
	private Date createdDate;
	private String modifiedBy;
	private Date modifiedDate;
	private String name;
	private String description;
	private String[] moderators;
	private String[] userPrivate ;
	private String[] createTopicRole;
		
	private String[] viewer;
	private String[] poster;

	private long forumCount = 0;
	private String[] emailNotification;
	
	
	
	

	
	
	public CategoryImpl(String id) {
		this.id = id;
		userPrivate = new String[] {" "};
		moderators = new String[] {" "};
		emailNotification = new String [] {} ;
		viewer = new String[] {" "};
		createTopicRole = new String[] {" "};
		poster = new String[] {" "};
	}
	public CategoryImpl(){
		this(Utils.CATEGORY + IdGenerator.generate());
	}

	
	
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#getId()
   */
	public String getId(){return id;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#setId(java.lang.String)
   */
	public void setId(String id){ this.id = id;}
	 
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#getOwner()
   */
	public String getOwner(){return owner;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#setOwner(java.lang.String)
   */
	public void setOwner(String owner){this.owner=owner;}

	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#getPath()
   */
	public String getPath() {return path; }
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#setPath(java.lang.String)
   */
	public void setPath( String path) { this.path = path;}
	 
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#getCategoryOrder()
   */
	public long getCategoryOrder(){return categoryOrder;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#setCategoryOrder(long)
   */
	public void setCategoryOrder(long categoryOrder){this.categoryOrder = categoryOrder;}
	 
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#getCreatedDate()
   */
	public Date getCreatedDate(){return createdDate;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#setCreatedDate(java.util.Date)
   */
	public void setCreatedDate(Date createdDate){this.createdDate = createdDate;}
	 
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#getModifiedBy()
   */
	public String getModifiedBy(){return modifiedBy;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#setModifiedBy(java.lang.String)
   */
	public void setModifiedBy(String modifiedBy) {this.modifiedBy = modifiedBy;}
	 
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#getModifiedDate()
   */
	public Date getModifiedDate(){return modifiedDate;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#setModifiedDate(java.util.Date)
   */
	public void setModifiedDate(Date modifiedDate){this.modifiedDate = modifiedDate;}
	 
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#getCategoryName()
   */
	public String getCategoryName(){return name;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#setCategoryName(java.lang.String)
   */
	public void setCategoryName(String categoryName){this.name = categoryName;}
	 
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#getDescription()
   */
	public String getDescription(){return description;}	 
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#setDescription(java.lang.String)
   */
	public void setDescription(String description){this.description = description;}

	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#getModerators()
   */
	public String[] getModerators() {return moderators;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#setModerators(java.lang.String[])
   */
	public void setModerators(String[] moderators) { this.moderators = moderators;}

	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#getUserPrivate()
   */
	public String[] getUserPrivate(){return userPrivate;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#setUserPrivate(java.lang.String[])
   */
	public void setUserPrivate(String[] userPrivate){this.userPrivate = userPrivate;}

	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#getCreateTopicRole()
   */
	public String[] getCreateTopicRole(){return createTopicRole;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#setCreateTopicRole(java.lang.String[])
   */
	public void setCreateTopicRole(String[] createTopicRole){this.createTopicRole = createTopicRole;}
	
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#getPoster()
   */
	public String[] getPoster(){return poster;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#setPoster(java.lang.String[])
   */
	public void setPoster(String[] poster){this.poster = poster;}
	
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#getViewer()
   */
	public String[] getViewer(){return viewer;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#setViewer(java.lang.String[])
   */
	public void setViewer(String[] viewer){this.viewer = viewer;}

	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#getForumCount()
   */
	public long getForumCount() {return forumCount;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#setForumCount(long)
   */
	public void setForumCount(long forumCount) {this.forumCount = forumCount;}

	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#getEmailNotification()
   */
	public String[] getEmailNotification() { return emailNotification; }
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Category#setEmailNotification(java.lang.String[])
   */
	public void setEmailNotification(String[] emailNotification) { this.emailNotification = emailNotification; }
}
