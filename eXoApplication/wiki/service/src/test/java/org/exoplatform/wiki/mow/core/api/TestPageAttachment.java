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

import java.util.Collection;
import java.util.Iterator;

import org.chromattic.ext.ntdef.Resource;
import org.exoplatform.wiki.mow.api.Model;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.content.ContentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PortalWiki;
import org.exoplatform.wiki.mow.core.api.wiki.WikiContainer;
import org.exoplatform.wiki.mow.core.api.wiki.WikiHome;
import org.exoplatform.wiki.service.WikiService;


public class TestPageAttachment extends AbstractMOWTestcase {
  
  public void testAddPageAttachment() {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWiki wiki = portalWikiContainer.addWiki("classic");
    WikiHome wikiHomePage = wiki.getWikiHome();
    
    PageImpl wikipage = wiki.createWikiPage();
    wikipage.setName("AddPageAttachment");
    wikiHomePage.addWikiPage(wikipage);
    wikipage.setPageId("AddPageAttachment-001") ;
    assertNull(wikipage.getContent()) ;
    
    AttachmentImpl attachment1 = wikipage.createAttachment("attachment1.jpg", Resource.createPlainText("foo")) ;
    attachment1.setCreator("you") ;    
    assertEquals(attachment1.getFilename(), "attachment1.jpg") ;
    assertNotNull(attachment1.getContentResource()) ;
    attachment1.setContentResource(Resource.createPlainText("foo - Updated")) ;    
    
    AttachmentImpl attachment2 = wikipage.createAttachment("attachment2.jpg", Resource.createPlainText("faa")) ;    
    attachment2.setCreator("me") ;
    assertEquals(attachment2.getFilename(), "attachment2.jpg") ;
    assertNotNull(attachment2.getContentResource()) ;
    attachment2.setContentResource(Resource.createPlainText("faa - Updated")) ;
  }
  
  public void testGetPageAttachment() throws Exception{
    WikiService wService = (WikiService)container.getComponentInstanceOfType(WikiService.class) ;
    PageImpl wikipage = (PageImpl)wService.getPageById("portal", "classic", "AddPageAttachment-001") ;
    Collection<AttachmentImpl> attachments = wikipage.getAttachments() ;
    assertEquals(attachments.size(), 2) ;
    Iterator<AttachmentImpl> iter = attachments.iterator() ;
    AttachmentImpl att1 = iter.next() ;
    assertNotNull(att1.getContentResource()) ;
    assertEquals(new String(att1.getContentResource().getData()), "foo - Updated") ;
    assertEquals(att1.getWeightInBytes(), "foo - Updated".getBytes().length) ;
    assertEquals(att1.getCreator(), "you") ;
    
    AttachmentImpl att2 = iter.next() ;
    assertNotNull(att2.getContentResource()) ;
    assertEquals(new String(att2.getContentResource().getData()), "faa - Updated") ;
    assertEquals(att2.getWeightInBytes(), "faa - Updated".getBytes().length) ;
    assertEquals(att2.getCreator(), "me") ;
  }
  
  
}