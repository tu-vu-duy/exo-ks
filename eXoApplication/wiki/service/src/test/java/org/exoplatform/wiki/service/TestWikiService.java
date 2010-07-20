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


import java.util.List;

import org.chromattic.api.ChromatticSession;
import org.chromattic.ext.ntdef.Resource;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.wiki.mow.api.Model;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.AbstractMOWTestcase;
import org.exoplatform.wiki.mow.core.api.WikiStoreImpl;
import org.exoplatform.wiki.mow.core.api.content.ContentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.GroupWiki;
import org.exoplatform.wiki.mow.core.api.wiki.MovedMixin;
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
    PortalWiki wikiACME = portalWikiContainer.addWiki("acme");
    wikiACME.getWikiHome() ;
    WikiHome wikiHomePage = wiki.getWikiHome();
    model.save() ;
    
    assertNotNull(wService.getPageById(PortalConfig.PORTAL_TYPE, "classic", "WikiHome")) ;    
        
    PageImpl wikipage = wiki.createWikiPage();
    wikipage.setName("testGetPortalPageById-001");
    wikiHomePage.addWikiPage(wikipage);
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
    wikipage.setName("testGetGroupPageById-001");
    wikiHomePage.addWikiPage(wikipage);
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
    wikipage.setName("testGetUserPageById-001");
    wikiHomePage.addWikiPage(wikipage);
    model.save() ;
    
    assertNotNull(wService.getPageById(PortalConfig.USER_TYPE, "john", "testGetUserPageById-001")) ;    
    
  }
  
  public void testCreatePageAndSubPage() throws Exception{    
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "parentPage", "WikiHome") ;
    assertNotNull(wService.getPageById(PortalConfig.PORTAL_TYPE, "classic", "parentPage")) ;
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "childPage", "parentPage") ;
    assertNotNull(wService.getPageById(PortalConfig.PORTAL_TYPE, "classic", "childPage")) ;
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
  
  public void testMovePage() throws Exception{    
    
    //moving page in same space
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "oldParent", "WikiHome") ;
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "child", "oldParent") ;
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "newParent", "WikiHome") ;
    
    assertNotNull(wService.getPageById(PortalConfig.PORTAL_TYPE, "classic", "oldParent")) ;
    assertNotNull(wService.getPageById(PortalConfig.PORTAL_TYPE, "classic", "child")) ;
    assertNotNull(wService.getPageById(PortalConfig.PORTAL_TYPE, "classic", "newParent")) ;
    
    assertTrue(wService.movePage("child", "newParent", "portal", "classic", "classic")) ;    
    assertFalse(wService.movePage("childWrong", "newParent", "portal", "classic", "classic")) ;
    
    wService.createPage(PortalConfig.PORTAL_TYPE, "acme", "acmePage", "WikiHome") ;
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "classicPage", "WikiHome") ;
    
    assertTrue(wService.movePage("classicPage", "acmePage", "portal", "classic", "acme")) ;
  }
  
  public void testAddMixin() throws Exception{    
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "mixinPage", "WikiHome") ;
    PageImpl page = (PageImpl)wService.getPageById("portal", "classic", "mixinPage") ;
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    ChromatticSession session = wStore.getSession() ;
    MovedMixin mix = session.create(MovedMixin.class) ;
    session.setEmbedded(page, MovedMixin.class, mix) ;
    assertSame(mix, page.getMovedMixin()) ;
    assertSame(page, mix.getEntity()) ;
    
  }
  
  public void testDeletePage() throws Exception{    
    PageImpl page = (PageImpl)wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "deletePage", "WikiHome") ;
    assertTrue(wService.deletePage(PortalConfig.PORTAL_TYPE, "classic", "deletePage")) ;
    assertNull(wService.getPageById(PortalConfig.PORTAL_TYPE, "classic", "deletePage")) ; 
    assertFalse(wService.deletePage(PortalConfig.PORTAL_TYPE, "classic", "WikiHome")) ;
  }
  
  public void testRenamePage() throws Exception{    
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "currentPage", "WikiHome") ;
    assertTrue(wService.renamePage(PortalConfig.PORTAL_TYPE, "classic", "currentPage", "renamedPage", "renamedPage")) ;
    assertNotNull(wService.getPageById(PortalConfig.PORTAL_TYPE, "classic", "renamedPage")) ;  
    
  }
  
  public void testSearchRenamedPage() throws Exception{    
    PageImpl page = (PageImpl)wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "Page", "WikiHome") ;
    page.getContent().setText("This is a rename page test") ;
    assertTrue(wService.renamePage(PortalConfig.PORTAL_TYPE, "classic", "Page", "Page01", "Page01")) ;
    assertEquals(1, wService.searchRenamedPage(PortalConfig.PORTAL_TYPE, "classic", "Page").size()) ;
  }
  
  public void testSearchContent() throws Exception {
    
    PageImpl kspage = (PageImpl)wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "knowledge suite", "WikiHome") ;
    kspage.getContent().setText("forum faq wiki") ;
    
    PageImpl cspage = (PageImpl)wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "collaboration suite", "WikiHome") ;
    cspage.getContent().setText("calendar mail contact chat") ;
    
    //fulltext search
    SearchData data = new SearchData("suite", null, null, "/exo:applications/eXoWiki/wikis/classic") ;
    PageList<ContentImpl> result = wService.searchContent("portal", "classic", data) ;
    assertEquals(2, result.getAll().size()) ;
    
  //title search
    data = new SearchData(null, "knowledge", null, null) ;
    result = wService.searchContent("portal", "classic", data) ;
    assertEquals(1, result.getAll().size()) ;
    
    data = new SearchData(null, "collaboration", null, null) ;
    result = wService.searchContent("portal", "classic", data) ;
    assertEquals(1, result.getAll().size()) ;
    
  //content search
    data = new SearchData(null, null, "forum", null) ;
    result = wService.searchContent("portal", "classic", data) ;
    assertEquals(1, result.getAll().size()) ;
    
    data = new SearchData(null, null, "calendar", null) ;
    result = wService.searchContent("portal", "classic", data) ;
    assertEquals(1, result.getAll().size()) ;
    
  //content & title search
    data = new SearchData(null, "suite", "forum", null) ;
    result = wService.searchContent("portal", "classic", data) ;
    assertEquals(1, result.getAll().size()) ;
    
  }
  
  public void testSearch() throws Exception {
    
    PageImpl kspage = (PageImpl)wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "knowledge", "WikiHome") ;
    kspage.getContent().setText("forum faq wiki exoplatform") ;
    
    AttachmentImpl attachment1 = kspage.createAttachment("attachment1.txt", Resource.createPlainText("foo")) ;
    attachment1.setCreator("you") ;    
    assertEquals(attachment1.getName(), "attachment1.txt") ;
    assertNotNull(attachment1.getContentResource()) ;
    attachment1.setContentResource(Resource.createPlainText("exoplatform content mamagement")) ;    
    
    SearchData data = new SearchData("exoplatform", null, null, null) ;
    
    PageList<SearchResult> result = wService.search("portal", "classic", data) ;
    assertEquals(2, result.getAll().size()) ;    
      
  }
  
  public void testGetPageTitleOfAttachment() throws Exception {
    PageImpl kspage = (PageImpl)wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "GetPageTitleOfAttachment", "WikiHome") ;
    kspage.getContent().setText("forum faq wiki exoplatform") ;
    AttachmentImpl attachment1 = kspage.createAttachment("attachment1.txt", Resource.createPlainText("foo")) ;
    attachment1.setCreator("you") ;    
    assertEquals(attachment1.getName(), "attachment1.txt") ;
    assertNotNull(attachment1.getContentResource()) ;
    attachment1.setContentResource(Resource.createPlainText("exoplatform content mamagement")) ;
    
    assertEquals("GetPageTitleOfAttachment", wService.getPageTitleOfAttachment(attachment1.getJCRContentPath())) ;
    
  }
  
  public void testGetAttachmentAsStream() throws Exception {
    PageImpl kspage = (PageImpl)wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "GetAttachmentAsStream", "WikiHome") ;
    kspage.getContent().setText("forum faq wiki exoplatform") ;
    AttachmentImpl attachment1 = kspage.createAttachment("attachment.txt", Resource.createPlainText("this is a text attachment")) ;
    attachment1.setCreator("john") ;    
    assertEquals(attachment1.getName(), "attachment.txt") ;
    assertNotNull(attachment1.getContentResource()) ;
    
    assertNotNull(wService.getAttachmentAsStream(attachment1.getPath()+"/jcr:content")) ;
    
  }
  
 /* public void testRevisionMixin() throws Exception {
    PageImpl page = (PageImpl)wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "RevisionPage", "WikiHome") ;
    page.getContent().setText("forum faq wiki exoplatform") ;
    AttachmentImpl attachment1 = page.createAttachment("attachment.txt", Resource.createPlainText("this is a text attachment")) ;
    
    
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    ChromatticSession session = wStore.getSession() ;
    RevisionMixin revision = session.create(RevisionMixin.class) ;
    session.setEmbedded(page, RevisionMixin.class, revision) ;
    
    page.setRevisionMixin(revision) ;
    assertTrue(page.getRevisionMixin().getIsCheckedOut()) ;
    attachment1.setCreator("john") ;
    page.getContent().setText("forum faq wiki exoplatform edited") ;
    page.getRevisionMixin().checkIn() ;
    assertFalse(page.getRevisionMixin().getIsCheckedOut()) ;
    //System.out.println("page.getRevisionMixin().getIsCheckedOut() ==>" + );
        
  }*/
  
  
}
