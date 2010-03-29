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
package org.exoplatform.wiki.webui;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.wiki.mow.api.Attachment;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.api.content.Content;
import org.exoplatform.wiki.mow.api.content.ContentItem;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class PageResolver {

  public PageResolver() {

  }

  public Page resolve(String requestURI) throws Exception {

    WikiType type = extractWikiType(requestURI);
    String owner = extractOwner(requestURI);
    String pageID = extractWikiPageUri(requestURI);

    Page page = loadWikiPage(type, owner, pageID);

    if (page == null) {
      page = pageNotFound(owner, pageID);
    }

    return page;

  }

  private WikiType extractWikiType(String requestURL) throws Exception {
    UIPortal uiPortal = Util.getUIPortal();
    PageNavigation nav = uiPortal.getSelectedNavigation();
    if (PortalConfig.PORTAL_TYPE.equalsIgnoreCase(nav.getOwnerType())) {
      return WikiType.PORTAL;
    } else if (PortalConfig.GROUP_TYPE.equalsIgnoreCase(nav.getOwnerType())) {
      return WikiType.GROUP;
    } else {
      return WikiType.USER;
    }

  }

  private Page loadWikiPage(WikiType type, String owner, String pageID) {
    return new FakePage(type, owner, pageID, "Fake user page");
  }

  private Page pageNotFound(String owner, String pageID) {
    return new FakePage(WikiType.PORTAL, owner, pageID, "Page does not exist");
  }

  private String extractOwner(String requestURI) throws Exception {
    UIPortal uiPortal = Util.getUIPortal();
    PageNavigation nav = uiPortal.getSelectedNavigation();
    return nav.getOwnerId();
  }

  private String extractWikiPageUri(String requestURI) throws Exception {
    UIPortal uiPortal = Util.getUIPortal();
    PageNode node = uiPortal.getSelectedNode();
    String nodeuri = node.getUri();
    int beginIndex = requestURI.indexOf(nodeuri) + nodeuri.length();
    int endIndex = requestURI.length();
    String pageID = requestURI.substring(beginIndex, endIndex);
    if (pageID.length() == 0) {
      pageID = "WikiHome"; // TODO use a const
    }
    return pageID;
  }

  class FakePage implements Page {

    String         owner;

    String         uri;

    String         content;

    final WikiType type;

    public FakePage(WikiType type, String owner, String wikiPageURI, String content) {
      this.owner = owner;
      this.uri = wikiPageURI;
      this.content = content;
      this.type = type;
    }

    public Collection<Attachment> getAttachments() {
      return Collections.emptyList();
    }

    public Content getContent() {
      return new FakeContent(this);
    }

    public String getOwner() {
      return owner;
    }

    public String toString() {
      return getContent().getText();
    }
  }

  class FakeContent implements Content {

    private FakePage page;

    public FakeContent(FakePage page) {
      this.page = page;
    }

    public String getSyntax() {
      return "xwiki";
    }

    public List<ContentItem> getChildren() {
      return Collections.emptyList();
    }

    public String getId() {
      return page.uri;
    }

    public String getText() {
      return "page: " + page.uri + "\n type:" + page.type + "\n owner:" + page.owner
          + "\n content:" + page.content;
    }

  }

}
