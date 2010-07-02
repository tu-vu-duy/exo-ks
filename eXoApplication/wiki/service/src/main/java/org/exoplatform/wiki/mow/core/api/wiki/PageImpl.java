/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.wiki.mow.core.api.wiki;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.chromattic.api.DuplicateNameException;
import org.chromattic.api.RelationshipType;
import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.Destroy;
import org.chromattic.api.annotations.ManyToOne;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.Name;
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Owner;
import org.chromattic.api.annotations.Path;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.chromattic.api.annotations.WorkspaceName;
import org.chromattic.ext.ntdef.Resource;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.content.ContentImpl;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          viet.nguyen@exoplatform.com
 * Mar 26, 2010  
 */
@PrimaryType(name = WikiNodeType.WIKI_PAGE)
public abstract class PageImpl implements Page {

  @Name
  public abstract String getName();
  public abstract void setName(String name);
  
  @Path
  public abstract String getPath();

  @WorkspaceName
  public abstract String getWorkspace();
  
  @OneToOne
  @Owner
  @MappedBy(WikiNodeType.Definition.CONTENT)
  public abstract ContentImpl getContent();  
  public abstract void setContent(ContentImpl content);
  
  @Property(name = WikiNodeType.Definition.OWNER)
  public abstract String getOwner();
  public abstract void setOwner(String owner);
  
  @Property(name = WikiNodeType.Definition.AUTHOR)
  public abstract String getAuthor();
  
  @Property(name = WikiNodeType.Definition.UPDATED_DATE)
  public abstract Date getUpdatedDate();
  
  
  @OneToOne(type = RelationshipType.EMBEDDED)
  @Owner
  public abstract MovedMixin getMovedMixin();
  public abstract void setMovedMixin(MovedMixin mix);
  
  @OneToOne(type = RelationshipType.EMBEDDED)
  @Owner
  public abstract RemovedMixin getRemovedMixin();
  public abstract void setRemovedMixin(RemovedMixin mix);
  
  @OneToOne(type = RelationshipType.EMBEDDED)
  @Owner
  public abstract RenamedMixin getRenamedMixin();
  public abstract void setRenamedMixin(RenamedMixin mix);
  
  @Create
  public abstract AttachmentImpl createAttachment();
  
  public AttachmentImpl createAttachment(String fileName, Resource contentResource) {
    if (fileName == null) {
      throw new NullPointerException();
    }
    AttachmentImpl file = createAttachment();
    file.setName(fileName) ;
    addAttachment(file) ;
    if (contentResource != null) {
      file.setContentResource(contentResource);      
    }
    return file;
  }
  
  
  @OneToMany
  public abstract Collection<AttachmentImpl> getAttachments() ;
  
  public AttachmentImpl getAttachment(String attachmentId) {
    for (AttachmentImpl att : getAttachments()) {
      if (att.getName().equals(attachmentId)) {
        return att;
      }
    }
    return null;
  }
  
  public void addAttachment(AttachmentImpl attachment) throws DuplicateNameException {
    getAttachments().add(attachment);
    
  }  
  
  public void removeAttachment(String attachmentId){
    AttachmentImpl attachment = getAttachment(attachmentId);
    if(attachment != null){
      attachment.remove();
    }
  }
  
  @ManyToOne
  public abstract PageImpl getParentPage();
  public abstract void setParentPage(PageImpl page);

  @ManyToOne
  public abstract Trash getTrash();
  public abstract void setTrash(Trash trash);
  
  @OneToMany
  public abstract Collection<PageImpl> getChildPages();
  
  public void addWikiPage(PageImpl wikiPage) throws DuplicateNameException {
    getChildPages().add(wikiPage);
  }
  
  public PageImpl getWikiPage(String pageId){
    if(WikiNodeType.Definition.WIKI_HOME_NAME.equalsIgnoreCase(pageId)){
      return this;
    }
    Iterator<PageImpl> iter = getChildPages().iterator();
    while(iter.hasNext()) {
      PageImpl page = (PageImpl)iter.next() ;
      if (pageId.equals(page.getName()))  return page ;         
    }
    return null ;
  }
  
  @Destroy
  public abstract void remove();
}