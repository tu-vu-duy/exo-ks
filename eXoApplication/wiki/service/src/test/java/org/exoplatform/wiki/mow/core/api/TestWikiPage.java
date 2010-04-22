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
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PortalWiki;
import org.exoplatform.wiki.mow.core.api.wiki.WikiContainer;
import org.exoplatform.wiki.mow.core.api.wiki.WikiHome;


public class TestWikiPage extends AbstractMOWTestcase {

  public void testAddWikiHome() {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWiki wiki = portalWikiContainer.addWiki("classic");
    WikiHome wikiHomePage = wiki.getWikiHome();
    assertNotNull(wikiHomePage) ;
  }

  public void testAddWikiPage() {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWiki wiki = portalWikiContainer.addWiki("classic");
    WikiHome wikiHomePage = wiki.getWikiHome();
    
    PageImpl wikipage = wiki.createWikiPage();
    wikipage.setName("AddWikiPage");
    
    wikiHomePage.addWikiPage(wikipage);
    assertSame(wikipage, wikiHomePage.getChildPages().iterator().next());
  }
  
  public void testGetWikiPageById() {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWiki wiki = portalWikiContainer.addWiki("classic");
    WikiHome wikiHomePage = wiki.getWikiHome();
    
    PageImpl wikipage = wiki.createWikiPage();
    wikipage.setName("CreateWikiPage");
    wikiHomePage.addWikiPage(wikipage);
    wikipage.setPageId("CreateWikiPage-001") ;
    assertNotNull(wikiHomePage.getWikiPage("CreateWikiPage-001")) ;    
  }
  
  public void testUpdateWikiPage() {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWiki wiki = portalWikiContainer.addWiki("classic");
    WikiHome wikiHomePage = wiki.getWikiHome();
    
    PageImpl wikipage = wiki.createWikiPage();    
    wikipage.setName("UpdateWikiPage");
    wikiHomePage.addWikiPage(wikipage);
    wikipage.setPageId("UpdateWikiPage-001") ;
    wikipage.setOwner("Root") ;
    
    PageImpl addedPage = wikiHomePage.getWikiPage("UpdateWikiPage-001") ;
    assertEquals(addedPage.getName(), "UpdateWikiPage") ;
    assertEquals(addedPage.getPageId(), "UpdateWikiPage-001") ;
    wikipage.setOwner("Demo") ;
    wikipage.setPageId("UpdateWikiPage-001-edited") ;
    
    PageImpl editedPage = wikiHomePage.getWikiPage("UpdateWikiPage-001-edited") ;
    assertNotNull(editedPage) ;
    assertEquals(editedPage.getOwner(), "Demo") ;    
  }
  
  public void testDeleteWikiPage() {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWiki wiki = portalWikiContainer.addWiki("classic");
    WikiHome wikiHomePage = wiki.getWikiHome();
    
    PageImpl wikipage = wiki.createWikiPage();
    wikipage.setName("DeleteWikiPage");
    wikiHomePage.addWikiPage(wikipage);
    wikipage.setPageId("delete-001") ;
    PageImpl deletePage = wikiHomePage.getWikiPage("delete-001") ;
    assertNotNull(deletePage) ;
    
    deletePage.remove() ;
    assertNull(wikiHomePage.getWikiPage("delete-001")) ;    
  }
  
  public void testHello() {
    String str = "http://hostname/$CONTAINER/$ACCESS/$SITE/wiki/[$OWNER_TYPE/$OWNER]/$WIKI_PAGE_URI" ;
    int i = str.indexOf("/wiki/") ;
    System.out.println("http://hostname/$CONTAINER/$ACCESS/$SITE/wiki/[$OWNER_TYPE/$OWNER]/$WIKI_PAGE_URI");
    System.out.println("i==" + i);
    System.out.println("==>" + str.substring( i + "/wiki/".length()));
  }
}
