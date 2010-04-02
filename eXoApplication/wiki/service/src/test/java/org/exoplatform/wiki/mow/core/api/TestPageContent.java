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
package org.exoplatform.wiki.mow.core.api;

import org.exoplatform.wiki.mow.api.Model;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.content.ContentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PortalWiki;
import org.exoplatform.wiki.mow.core.api.wiki.WikiContainer;
import org.exoplatform.wiki.mow.core.api.wiki.WikiHome;


public class TestPageContent extends AbstractMOWTestcase {
  
  public void testAddPageContent() {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWiki wiki = portalWikiContainer.addWiki("classic");
    WikiHome wikiHomePage = wiki.getWikiHome();
    
    PageImpl wikipage = wiki.createWikiPage();
    wikipage.setName("AddContentPage");
    wikiHomePage.addWikiPage(wikipage);
    wikipage.setPageId("AddContentPage-001") ;
    assertNull(wikipage.getContent()) ;
    
    ContentImpl content = wiki.createContent() ;
    wikipage.setContent(content) ;
    content.setSyntax("xwiki_2.0") ;
    content.setText("This is a content of page") ;
    assertNotNull(wikipage.getContent()) ;    
    
  }
  
  public void testGetPageContent() {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWiki wiki = portalWikiContainer.addWiki("classic");
    WikiHome wikiHomePage = wiki.getWikiHome();
    
    PageImpl wikipage = wiki.createWikiPage();
    wikipage.setName("GetPageContent");
    wikiHomePage.addWikiPage(wikipage);
    wikipage.setPageId("AddPageContent-001") ;
    assertNull(wikipage.getContent()) ;
    
    ContentImpl content = wiki.createContent() ;
    wikipage.setContent(content) ;
    content.setSyntax("xwiki_2.0") ;
    content.setText("This is a content of page") ;
    ContentImpl addedContent = wikipage.getContent() ; 
    assertNotNull(addedContent) ;
    assertEquals(addedContent.getSyntax(), "xwiki_2.0") ;
    assertEquals(addedContent.getText(), "This is a content of page") ;    
  }

  public void testUpdatePageContent() {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWiki wiki = portalWikiContainer.addWiki("classic");
    WikiHome wikiHomePage = wiki.getWikiHome();
    
    PageImpl wikipage = wiki.createWikiPage();
    wikipage.setName("UpdatePageContent");
    wikiHomePage.addWikiPage(wikipage);
    wikipage.setPageId("UpdatePageContent-001") ;
    assertNull(wikipage.getContent()) ;
    
    ContentImpl content = wiki.createContent() ;
    wikipage.setContent(content) ;
    content.setSyntax("xwiki_2.0") ;
    content.setText("This is a content of page") ;
    ContentImpl addedContent = wikipage.getContent() ; 
    assertNotNull(addedContent) ;
    assertEquals(addedContent.getSyntax(), "xwiki_2.0") ;
    assertEquals(addedContent.getText(), "This is a content of page") ;
    
    addedContent.setText("This is a content of page - edited") ;
    addedContent.setSyntax("xwiki_2.1") ;
    
    ContentImpl updatedContent = wikipage.getContent() ;
    assertNotNull(updatedContent) ;
    assertEquals(updatedContent.getSyntax(), "xwiki_2.1") ;
    assertEquals(updatedContent.getText(), "This is a content of page - edited") ;
    
  }
  
  public void testDeletePageContent() {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWiki wiki = portalWikiContainer.addWiki("classic");
    WikiHome wikiHomePage = wiki.getWikiHome();
    
    PageImpl wikipage = wiki.createWikiPage();
    wikipage.setName("DeletePageContent");
    wikiHomePage.addWikiPage(wikipage);
    wikipage.setPageId("DeletePageContent-001") ;
    assertNull(wikipage.getContent()) ;
    
    ContentImpl content = wiki.createContent() ;
    wikipage.setContent(content) ;
    content.setSyntax("xwiki_2.0") ;
    content.setText("This is a content of page") ;
    ContentImpl addedContent = wikipage.getContent() ; 
    assertNotNull(addedContent) ;
    
    ContentImpl deleteContent = wikipage.getContent() ;
    deleteContent.remove() ;
    assertNull(wikipage.getContent()) ;
  }
}
