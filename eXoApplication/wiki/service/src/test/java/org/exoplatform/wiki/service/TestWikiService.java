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
package org.exoplatform.wiki.service;


import java.util.Iterator;
import java.util.List;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.wiki.mow.api.Model;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.AbstractMOWTestcase;
import org.exoplatform.wiki.mow.core.api.MOWService;
import org.exoplatform.wiki.mow.core.api.WikiStoreImpl;
import org.exoplatform.wiki.mow.core.api.wiki.GroupWiki;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PortalWiki;
import org.exoplatform.wiki.mow.core.api.wiki.UserWiki;
import org.exoplatform.wiki.mow.core.api.wiki.WikiContainer;
import org.exoplatform.wiki.mow.core.api.wiki.WikiHome;


public class TestWikiService extends AbstractMOWTestcase {
  private WikiService wService ;
  
  public void setUp() throws Exception{
    super.setUp() ;
    wService = (WikiService)container.getComponentInstanceOfType(WikiService.class) ;    
  }
  
  public void testWikiService() throws Exception{
    assertNotNull(wService) ;
  }
  
  public void testGetPortalPageById() throws Exception{
    
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWiki wiki = portalWikiContainer.addWiki("classic");
    WikiHome wikiHomePage = wiki.getWikiHome();
    model.save() ;
    
    assertNotNull(wService.getPageById(PortalConfig.PORTAL_TYPE, "classic", "WikiHome")) ;    
        
    PageImpl wikipage = wiki.createWikiPage();
    wikipage.setName("testGetPortalPageById");
    wikiHomePage.addWikiPage(wikipage);
    wikipage.setPageId("testGetPortalPageById-001") ;
    model.save() ;
    
    assertNotNull(wService.getPageById(PortalConfig.PORTAL_TYPE, "classic", "testGetPortalPageById-001")) ;    
    
  }
  
  public void testGetGroupPageById() throws Exception{
    
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<GroupWiki> groupWikiContainer = wStore.getWikiContainer(WikiType.GROUP);
    GroupWiki wiki = groupWikiContainer.addWiki("platform/users");
    WikiHome wikiHomePage = wiki.getWikiHome();
    model.save() ;
    
    assertNotNull(wService.getPageById(PortalConfig.GROUP_TYPE, "platform/users", "WikiHome")) ;
    
    PageImpl wikipage = wiki.createWikiPage();
    wikipage.setName("testGetGroupPageById");
    wikiHomePage.addWikiPage(wikipage);
    wikipage.setPageId("testGetGroupPageById-001") ;
    model.save() ;
    
    assertNotNull(wService.getPageById(PortalConfig.GROUP_TYPE, "platform/users", "testGetGroupPageById-001")) ;    
    
  }
  
  public void testGetUserPageById() throws Exception{
    
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<UserWiki> userWikiContainer = wStore.getWikiContainer(WikiType.USER);
    UserWiki wiki = userWikiContainer.addWiki("john");
    WikiHome wikiHomePage = wiki.getWikiHome();
    model.save() ;
    
    assertNotNull(wService.getPageById(PortalConfig.USER_TYPE, "john", "WikiHome")) ;
    
    PageImpl wikipage = wiki.createWikiPage();
    wikipage.setName("testGetUserPageById");
    wikiHomePage.addWikiPage(wikipage);
    wikipage.setPageId("testGetUserPageById-001") ;
    model.save() ;
    
    assertNotNull(wService.getPageById(PortalConfig.USER_TYPE, "john", "testGetUserPageById-001")) ;    
    
  }
  
  public void testCreatePageAndSubPage() throws Exception{
    
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "parentPage", "WikiHome") ;
    //assertNotNull(wService.getPageById(PortalConfig.PORTAL_TYPE, "classic", "parentPage")) ;
    //Page child = wService.createPage(PortalConfig.USER_TYPE, "john", "childPage", "parentPage") ;
    
   /* Page page = wService.getPageById(PortalConfig.PORTAL_TYPE, "classic", "parentPage") ;
    assertNotNull(page) ;*/
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    /*WikiContainer<PortalWiki> userWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWiki wiki = userWikiContainer.getWiki("classic");
    WikiHome wikiHomePage = wiki.getWikiHome();
    PageImpl addedParent = wikiHomePage.getWikiPage("parentPage") ;    
    assertNotNull(addedParent) ;*/
    
    /*MOWService mowService = (MOWService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(MOWService.class);
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<PortalWiki> userWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWiki wiki = userWikiContainer.getWiki("classic");
    PageImpl child = wiki.createWikiPage() ;
    WikiHome wikiHomePage = wiki.getWikiHome();
    child.setName("hello") ;
    wikiHomePage.addWikiPage(child) ;
    child.setPageId("hello") ;
    model.save() ;*/
    String statement = "jcr:path LIKE '/exo:applications/eXoWiki/wikis/classic/%' AND pageId='parentPage'" ;
    PageImpl wikiPage = null;
    if(statement != null) {            
      Iterator<PageImpl> result = wStore.getSession()
        .createQueryBuilder(PageImpl.class)
        .where(statement).get().objects() ;
      if(result.hasNext()) wikiPage = result.next() ;
    }
    //assertNotNull(wikiPage) ;
    System.out.println("hello ===>" + wikiPage);
    /*PageImpl haha = wiki.createWikiPage() ;
    haha.setName("haha") ;
    wikiPage.addWikiPage(haha) ;
    haha.setPageId("haha") ;
    model.save() ;*/
    /*Page page = wService.getPageById("portal", "classic", "haha") ;
    assertNotNull(page) ;
    System.out.println("hello ===>" + page);*/
    
  }
  
  public void testGetBreadcumb() throws Exception {
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "Breadcumb1", "WikiHome") ;
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "Breadcumb2", "Breadcumb1") ;
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "Breadcumb3", "Breadcumb2") ;
    List<BreadcumbData> breadCumbs = wService.getBreadcumb(PortalConfig.PORTAL_TYPE, "classic", "Breadcumb3");
    assertEquals(4, breadCumbs.size());
    assertEquals("WikiHome", breadCumbs.get(0).getId());
    assertEquals("Breadcumb1", breadCumbs.get(1).getId());
    assertEquals("Breadcumb2", breadCumbs.get(2).getId());
    assertEquals("Breadcumb3", breadCumbs.get(3).getId());
    wService.createPage(PortalConfig.GROUP_TYPE, "/platform/users", "GroupBreadcumb1", "WikiHome") ;
    wService.createPage(PortalConfig.GROUP_TYPE, "/platform/users/", "GroupBreadcumb2", "GroupBreadcumb1") ;
    wService.createPage(PortalConfig.GROUP_TYPE, "platform/users", "GroupBreadcumb3", "GroupBreadcumb2") ;
    breadCumbs = wService.getBreadcumb(PortalConfig.GROUP_TYPE, "/platform/users", "GroupBreadcumb3");
    assertEquals(4, breadCumbs.size());
    assertEquals("WikiHome", breadCumbs.get(0).getId());
    assertEquals("GroupBreadcumb1", breadCumbs.get(1).getId());
    assertEquals("GroupBreadcumb2", breadCumbs.get(2).getId());
    assertEquals("GroupBreadcumb3", breadCumbs.get(3).getId());
    wService.createPage(PortalConfig.USER_TYPE, "john", "UserBreadcumb1", "WikiHome") ;
    wService.createPage(PortalConfig.USER_TYPE, "john", "UserBreadcumb2", "UserBreadcumb1") ;
    wService.createPage(PortalConfig.USER_TYPE, "john", "UserBreadcumb3", "UserBreadcumb2") ;
    breadCumbs = wService.getBreadcumb(PortalConfig.USER_TYPE, "john", "UserBreadcumb3");
    assertEquals(4, breadCumbs.size());
    assertEquals("WikiHome", breadCumbs.get(0).getId());
    assertEquals("UserBreadcumb1", breadCumbs.get(1).getId());
    assertEquals("UserBreadcumb2", breadCumbs.get(2).getId());
    assertEquals("UserBreadcumb3", breadCumbs.get(3).getId());
  }
  
}
