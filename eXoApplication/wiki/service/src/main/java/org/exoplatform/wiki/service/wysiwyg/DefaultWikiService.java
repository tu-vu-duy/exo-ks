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
package org.exoplatform.wiki.service.wysiwyg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.service.SearchData;
import org.exoplatform.wiki.service.SearchResult;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.gwt.wysiwyg.client.plugin.link.LinkConfig;
import org.xwiki.gwt.wysiwyg.client.wiki.Attachment;
import org.xwiki.gwt.wysiwyg.client.wiki.WikiPage;
import org.xwiki.gwt.wysiwyg.client.wiki.WikiService;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.WikiReference;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jun 24, 2010  
 */
@Component
public class DefaultWikiService implements WikiService {

  private static Log log = ExoLogger.getLogger("wiki:GWTWikiService"); 
  
  /**
   * The component used to serialize Wiki document references.
   */
  @Requirement
  private EntityReferenceSerializer<String> entityReferenceSerializer;

  /**
   * Used to construct a valid document reference.
   */
  @Requirement("default/reference")
  private DocumentReferenceResolver<EntityReference> defaultReferenceDocumentReferenceResolver;
  
  @Override
  public Attachment getAttachment(String wikiName,
                                  String spaceName,
                                  String pageName,
                                  String attachmentName) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Attachment> getAttachments(String wikiName, String spaceName, String pageName) {
    try {
      if (log.isTraceEnabled()) {
        log.trace("Getting attachments of page : " + wikiName + "." + spaceName + "." + pageName);
      }
      List<Attachment> attachments = new ArrayList<Attachment>();
      DocumentReference documentReference = prepareDocumentReference(wikiName, spaceName, pageName);
      org.exoplatform.wiki.service.WikiService wservice = (org.exoplatform.wiki.service.WikiService) PortalContainer.getComponent(org.exoplatform.wiki.service.WikiService.class);
      Page page = wservice.getExsitedOrNewDraftPageById(wikiName, spaceName, pageName);
      Collection<AttachmentImpl> attachs = ((PageImpl) page).getAttachments();
      for (AttachmentImpl attach : attachs) {
        Attachment currentAttach = new Attachment();
        currentAttach.setFileName(attach.getName());
        currentAttach.setURL(attach.getDownloadURL());
        currentAttach.setMimeType(attach.getContentResource().getMimeType());
        currentAttach.setReference(this.entityReferenceSerializer.serialize(new AttachmentReference(attach.getName(), documentReference)));
        attachments.add(currentAttach);
      }
      return attachments;
    } catch (Exception e) {
      throw new RuntimeException("Failed to retrieve the list of attachments.", e);
    }
  }

  @Override
  public List<Attachment> getImageAttachments(String wikiName, String spaceName, String pageName) {
    List<Attachment> imageAttachments = new ArrayList<Attachment>();
    List<Attachment> allAttachments = getAttachments(wikiName, spaceName, pageName);
    for (Attachment attachment : allAttachments) {
      if (log.isTraceEnabled()) {
        log.trace("MimeType of attachment " + attachment.getFileName() + " is : " + attachment.getMimeType());
      }
      if (attachment.getMimeType().startsWith("image/")) {
        imageAttachments.add(attachment);
      }
    }
    return imageAttachments;
  }

  @Override
  public List<WikiPage> getMatchingPages(String keyword, int start, int count) {
    String quote = "'";
    String doubleQuote = "''";
    String escapedKeyword = keyword.replaceAll(quote, doubleQuote).toLowerCase();
    org.exoplatform.wiki.service.WikiService wservice = (org.exoplatform.wiki.service.WikiService) PortalContainer.getComponent(org.exoplatform.wiki.service.WikiService.class);
    SearchData data = new SearchData(null, escapedKeyword, null, null);
    try {
      PageList<SearchResult> results = wservice.search("portal", "classic", data);
      return prepareDocumentResultsList(results);
    } catch (Exception e) {
      throw new RuntimeException("Failed to search XWiki pages.", e);
    }
  }

  @Override
  public LinkConfig getPageLink(String wikiName,
                                String spaceName,
                                String pageName,
                                String revision,
                                String anchor) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getPageNames(String wikiName, String spaceName) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<WikiPage> getRecentlyModifiedPages(int start, int count) {
    // TODO: implement wiki search service by author and sort by date to get recently modified pages
    return new ArrayList<WikiPage>();
  }

  @Override
  public List<String> getSpaceNames(String wikiName) {
    // TODO: get all wikiOwner of the wikiName
    return new ArrayList<String>();
  }

  @Override
  public List<String> getVirtualWikiNames() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Boolean isMultiWiki() {
    return false;
  }

  /**
   * Helper function to prepare a list of {@link WikiPage}s (with full name, title, etc) from a list of document names.
   * 
   * @param results the list of the documents to include in the list
   * @return the list of {@link WikiPage}s corresponding to the passed names
   * @throws Exception if anything goes wrong retrieving the documents
   */
  private List<WikiPage> prepareDocumentResultsList(PageList<SearchResult> results) throws Exception {
    List<WikiPage> pages = new ArrayList<WikiPage>();
    for (SearchResult result : results.getAll()) {
      WikiPage page = new WikiPage();
      String nodeName = result.getNodeName();
      if(nodeName != null && nodeName.length() > 0 && nodeName.startsWith("/")) {
        nodeName = nodeName.substring(1);
      }
      page.setName(nodeName);
      page.setTitle(result.getTitle());
      page.setURL("http://localhost:8080/ksdemo/classic/wiki/WikiHome");
      pages.add(page);
    }
    return pages;
  }
  
  /**
   * Gets a document reference from the passed parameters, handling the empty wiki, empty space or empty page name.
   * 
   * @param wikiType the wiki type : portal, group, user
   * @param wikiOwner the wiki owner
   * @param pageId the wiki pageId
   * @return the completed {@link DocumentReference} corresponding to the passed
   *         parameters, with all the missing values completed with defaults
   */
  private DocumentReference prepareDocumentReference(String wikiType, String wikiOwner, String pageId) {
    EntityReference reference = null;
    if (!StringUtils.isEmpty(wikiType)) {
      reference = new EntityReference(wikiType, EntityType.WIKI);
    }
    if (!StringUtils.isEmpty(wikiOwner)) {
      reference = new EntityReference(wikiOwner, EntityType.SPACE, reference);
    }
    if (!StringUtils.isEmpty(pageId)) {
      reference = new EntityReference(pageId, EntityType.DOCUMENT, reference);
    }

    DocumentReference resolvedReference = this.defaultReferenceDocumentReferenceResolver.resolve(reference);
    if (StringUtils.isEmpty(wikiType)) {
      resolvedReference.setWikiReference(new WikiReference("portal"));
    }
    return resolvedReference;
  }
  
}
