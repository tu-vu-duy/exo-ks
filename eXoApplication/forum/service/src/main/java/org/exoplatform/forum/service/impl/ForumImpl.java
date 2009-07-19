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
package org.exoplatform.forum.service.impl ;

import java.util.Date;
import java.util.List;

import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.services.jcr.util.IdGenerator;
/**
 * March 2, 2007	
 */
public class ForumImpl implements Forum {
	private String id;
	private String owner;
	private String path ;
	private int forumOrder;
	private Date createdDate;
	private String modifiedBy;
	private Date modifiedDate;
	private String lastTopicPath;
	private String name;
	private String description;
	private long postCount = 0;
	private long topicCount = 0;
	
	private String[] notifyWhenAddTopic ;
	private String[] notifyWhenAddPost ;
	private boolean isAutoAddEmailNotify = true ;
	private boolean isModerateTopic = false ;
	private boolean isModeratePost = false ;
	private boolean isClosed = false ;
	private boolean isLock = false ;

	private String[] moderators;
	private String[] createTopicRole;
	
	private String[] viewer;
	private String[] poster;
	
	private String[] emailNotification;
	private List<String> banIPs;
	
	
	public ForumImpl() {
		notifyWhenAddTopic = new String[] {};
		notifyWhenAddPost = new String[] {};
		viewer = new String[] {};
		createTopicRole = new String[] {};
		moderators = new String[] {};
		poster = new String[] {};
		emailNotification = new String[] {};
		id = Utils.FORUM + IdGenerator.generate() ;
	}
	
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#getId()
   */
	public String getId(){return id;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#setId(java.lang.String)
   */
	public void setId(String id){this.id = id;}
	
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#getCategoryId()
   */
	public String getCategoryId(){
		if(path != null && path.length() > 0) {
			String[] arr = path.split("/");
			return arr[arr.length - 2];
		}
		return null;
	}
	
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#getOwner()
   */
	public String getOwner(){return owner;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#setOwner(java.lang.String)
   */
	public void setOwner(String owner){this.owner = owner;}
	
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#getPath()
   */
	public String getPath() {return path; }
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#setPath(java.lang.String)
   */
	public void setPath( String path) { this.path = path;}
	
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#getForumOrder()
   */
	public int getForumOrder(){return forumOrder;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#setForumOrder(int)
   */
	public void setForumOrder(int forumOrder){this.forumOrder = forumOrder;}
	
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#getCreatedDate()
   */
	public Date getCreatedDate(){return createdDate;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#setCreatedDate(java.util.Date)
   */
	public void setCreatedDate(Date createdDate){this.createdDate = createdDate;}
	
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#getModifiedBy()
   */
	public String getModifiedBy(){return modifiedBy;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#setModifiedBy(java.lang.String)
   */
	public void setModifiedBy(String modifiedBy){this.modifiedBy = modifiedBy;}
	
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#getModifiedDate()
   */
	public Date getModifiedDate(){return modifiedDate;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#setModifiedDate(java.util.Date)
   */
	public void setModifiedDate(Date modifiedDate){this.modifiedDate = modifiedDate;}
	
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#getLastTopicPath()
   */
	public String getLastTopicPath(){return lastTopicPath;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#setLastTopicPath(java.lang.String)
   */
	public void setLastTopicPath(String lastTopicPath){this.lastTopicPath = lastTopicPath;}
	
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#getForumName()
   */
	public String getForumName(){return name;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#setForumName(java.lang.String)
   */
	public void setForumName(String forumName){this.name = forumName;}
	
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#getDescription()
   */
	public String getDescription(){return description;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#setDescription(java.lang.String)
   */
	public void setDescription(String description){this.description = description;}
	
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#getPostCount()
   */
	public long getPostCount(){return postCount;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#setPostCount(long)
   */
	public void setPostCount(long postCount){this.postCount = postCount;}
	
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#getTopicCount()
   */
	public long getTopicCount(){return topicCount;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#setTopicCount(long)
   */
	public void setTopicCount(long topicCount){this.topicCount = topicCount;}
	
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#getNotifyWhenAddTopic()
   */
	public String[] getNotifyWhenAddTopic() { return notifyWhenAddTopic;	}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#setNotifyWhenAddTopic(java.lang.String[])
   */
	public void setNotifyWhenAddTopic(String[] notifyWhenAddTopic) {this.notifyWhenAddTopic = notifyWhenAddTopic;}
	
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#getNotifyWhenAddPost()
   */
	public String[] getNotifyWhenAddPost() {return notifyWhenAddPost; }
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#setNotifyWhenAddPost(java.lang.String[])
   */
	public void setNotifyWhenAddPost(String[] notifyWhenAddPost) { this.notifyWhenAddPost = notifyWhenAddPost;}
	
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#getIsModerateTopic()
   */
	public boolean getIsModerateTopic() { return isModerateTopic;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#setIsModerateTopic(boolean)
   */
	public void setIsModerateTopic(boolean isModerateTopic) { this.isModerateTopic = isModerateTopic;}
 
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#getIsModeratePost()
   */
	public boolean getIsModeratePost() { return isModeratePost;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#setIsModeratePost(boolean)
   */
	public void setIsModeratePost(boolean isModeratePost) { this.isModeratePost = isModeratePost;}
	
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#getIsClosed()
   */
	public boolean getIsClosed() { return isClosed;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#setIsClosed(boolean)
   */
	public void setIsClosed(boolean isClosed) { this.isClosed = isClosed;}
	
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#getIsLock()
   */
	public boolean getIsLock() { return isLock;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#setIsLock(boolean)
   */
	public void setIsLock(boolean isLock) { this.isLock = isLock;}
	
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#getCreateTopicRole()
   */
	public String[] getCreateTopicRole(){return createTopicRole;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#setCreateTopicRole(java.lang.String[])
   */
	public void setCreateTopicRole(String[] createTopicRole){this.createTopicRole = createTopicRole;}
	
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#getPoster()
   */
	public String[] getPoster(){return poster;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#setPoster(java.lang.String[])
   */
	public void setPoster(String[] poster){this.poster = poster;}
	
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#getViewer()
   */
	public String[] getViewer(){return viewer;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#setViewer(java.lang.String[])
   */
	public void setViewer(String[] viewer){this.viewer = viewer;}
	
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#getModerators()
   */
	public String[] getModerators(){return moderators;}	
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#setModerators(java.lang.String[])
   */
	public void setModerators(String[] moderators){this.moderators = moderators;}

	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#getEmailNotification()
   */
	public String[] getEmailNotification(){return emailNotification;}	
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#setEmailNotification(java.lang.String[])
   */
	public void setEmailNotification(String[] emailNotification){this.emailNotification = emailNotification;}

	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#getBanIP()
   */
	public List<String> getBanIP() { return banIPs;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#setBanIP(java.util.List)
   */
	public void setBanIP(List<String> banIPs) { this.banIPs = banIPs;}

	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#getIsAutoAddEmailNotify()
   */
	public boolean getIsAutoAddEmailNotify() { return isAutoAddEmailNotify;}
	/* (non-Javadoc)
   * @see org.exoplatform.forum.service.Forum#setIsAutoAddEmailNotify(boolean)
   */
	public void setIsAutoAddEmailNotify(boolean isAutoAddEmailNotify) {this.isAutoAddEmailNotify = isAutoAddEmailNotify;}
}
