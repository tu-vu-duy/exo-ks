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
package org.exoplatform.wiki.rendering.xwiki;

import org.exoplatform.wiki.mow.api.Model;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.AbstractMOWTestcase;
import org.exoplatform.wiki.mow.core.api.WikiStoreImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PortalWiki;
import org.exoplatform.wiki.mow.core.api.wiki.WikiContainer;
import org.exoplatform.wiki.mow.core.api.wiki.WikiHome;
import org.exoplatform.wiki.rendering.MarkupRenderingService;
import org.exoplatform.wiki.service.WikiContext;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Nov
 * 5, 2009
 */
public class TestXWikiRenderer extends AbstractMOWTestcase {

  XWikiRenderer renderer;

  protected void setUp() throws Exception {
    super.setUp();
    MarkupRenderingService service = new MarkupRenderingService();
    renderer = (XWikiRenderer) service.getRenderer("xwiki");
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testRender() throws Exception {
    assertEquals("<p>This is <strong>bold</strong></p>", renderer.render("This is **bold**"));
  }

  public void testLinks() throws Exception {
    assertTrue(renderer.render("[[OK]]").contains("wikicreatelink"));
  }
  
  public void testRenderAnExistedInternalLink() throws Exception {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWiki wiki = portalWikiContainer.addWiki("classic");
    WikiHome wikiHomePage = wiki.getWikiHome();
    
    PageImpl wikipage = wiki.createWikiPage();
    wikipage.setName("CreateWikiPage1");
    wikiHomePage.addWikiPage(wikipage);
    wikipage.setPageId("CreateWikiPage-001") ;
    
    Execution ec = renderer.getExecutionContext();
    ec.setContext(new ExecutionContext());
    WikiContext wikiContext = new WikiContext();
    wikiContext.setPortalURI("http://loclahost:8080/portal/classic");
    wikiContext.setPortletURI("wiki");
    wikiContext.setType("portal");
    wikiContext.setOwner("classic");
    wikiContext.setPageId("CreateWikiPage-001");
    
    ec.getContext().setProperty("wikicontext", wikiContext);
    
    String expectedHtml = "<p><span class=\"wikilink\"><a href=\"http://loclahost:8080/portal/classic/wiki/CreateWikiPage-001\">CreateWikiPage-001</a></span></p>";
    assertEquals(expectedHtml, renderer.render("[[CreateWikiPage-001>>CreateWikiPage-001]]"));
    assertEquals(expectedHtml, renderer.render("[[CreateWikiPage-001>>classic.CreateWikiPage-001]]"));
    assertEquals(expectedHtml, renderer.render("[[CreateWikiPage-001>>portal:classic.CreateWikiPage-001]]"));
  }
  
  public void testRenderAttachmentsAndImages() throws Exception {
    Execution ec = renderer.getExecutionContext();
    ec.setContext(new ExecutionContext());
    WikiContext wikiContext = new WikiContext();
    wikiContext.setPortalURI("http://loclahost:8080/portal/classic");
    wikiContext.setPortletURI("wiki");
    wikiContext.setType("portal");
    wikiContext.setOwner("classic");
    wikiContext.setPageId("CreateWikiPage-001");
    
    ec.getContext().setProperty("wikicontext", wikiContext);
    
    String expectedAttachmentHtml = "<p><span class=\"wikiexternallink\"><a href=\"/portal/rest/jcr/db1/ws/exo:applications/eXoWiki/wikis/classic/WikiHome/CreateWikiPage/eXoWikiHome.png\">eXoWikiHome.png</a></span></p>";  
    assertEquals(expectedAttachmentHtml, renderer.render("[[eXoWikiHome.png>>attach:eXoWikiHome.png]]"));
    assertEquals(expectedAttachmentHtml, renderer.render("[[eXoWikiHome.png>>attach:CreateWikiPage-001@eXoWikiHome.png]]"));
    assertEquals(expectedAttachmentHtml, renderer.render("[[eXoWikiHome.png>>attach:classic.CreateWikiPage-001@eXoWikiHome.png]]"));
    assertEquals(expectedAttachmentHtml, renderer.render("[[eXoWikiHome.png>>attach:portal:classic.CreateWikiPage-001@eXoWikiHome.png]]"));
    
    String expectedImageHtml = "<p><img src=\"/portal/rest/jcr/db1/ws/exo:applications/eXoWiki/wikis/classic/WikiHome/CreateWikiPage/eXoWikiHome.png\" alt=\"eXoWikiHome.png\"/></p>";
    assertEquals(expectedImageHtml, renderer.render("[[image:eXoWikiHome.png]]"));
    assertEquals(expectedImageHtml, renderer.render("[[image:CreateWikiPage-001@eXoWikiHome.png]]"));
    assertEquals(expectedImageHtml, renderer.render("[[image:classic.CreateWikiPage-001@eXoWikiHome.png]]"));
    assertEquals(expectedImageHtml, renderer.render("[[image:portal:classic.CreateWikiPage-001@eXoWikiHome.png]]"));
    
    String expectedFreeStandingImageHtml = "<p><img src=\"/portal/rest/jcr/db1/ws/exo:applications/eXoWiki/wikis/classic/WikiHome/CreateWikiPage/eXoWikiHome.png\" class=\"wikimodel-freestanding\" alt=\"eXoWikiHome.png\"/></p>";
    assertEquals(expectedFreeStandingImageHtml, renderer.render("image:eXoWikiHome.png"));
    assertEquals(expectedFreeStandingImageHtml, renderer.render("image:CreateWikiPage-001@eXoWikiHome.png"));
    assertEquals(expectedFreeStandingImageHtml, renderer.render("image:classic.CreateWikiPage-001@eXoWikiHome.png"));
    assertEquals(expectedFreeStandingImageHtml, renderer.render("image:portal:classic.CreateWikiPage-001@eXoWikiHome.png"));
  }

}
