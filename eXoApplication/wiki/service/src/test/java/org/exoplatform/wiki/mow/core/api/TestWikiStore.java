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
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.WikiStore;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PortalWiki;
import org.exoplatform.wiki.mow.core.api.wiki.PortalWikiContainer;
import org.exoplatform.wiki.mow.core.api.wiki.WikiHome;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          viet.nguyen@exoplatform.com
 * Mar 29, 2010  
 */
public class TestWikiStore extends AbstractMOWTestcase {

  public void testGetWikiStore() {
    Model model = mowService.getModel();
    WikiStore wStore = model.getMultiWiki();
    assertNotNull(wStore);
  }

  public void testAddAndGetPortalContainerWiki() {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getMultiWiki();
    PortalWikiContainer portalWiki = wStore.createPortalWikiContainer();
    assertNotNull(portalWiki);
    wStore.setPortalWikis(portalWiki);
    PortalWikiContainer pw = wStore.getPortalWikis();
    assertSame(portalWiki, pw);
  }

  public void testAddAndGetPortalClassicWiki() {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getMultiWiki();
    PortalWikiContainer portalWiki = wStore.createPortalWikiContainer();
    wStore.setPortalWikis(portalWiki);
    PortalWiki wiki = portalWiki.addWiki("classic");
    PortalWiki classicWiki = portalWiki.getWiki("classic");
    assertSame(wiki, classicWiki);
  }

  public void testGetClassicWikiHomePage() {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getMultiWiki();
    PortalWikiContainer portalWiki = wStore.createPortalWikiContainer();
    wStore.setPortalWikis(portalWiki);
    PortalWiki wiki = portalWiki.addWiki("classic");
    Page wikiHomePage = wiki.getWikiHome();
    assertNotNull(wikiHomePage);
  }

  public void testAddAndGetClassicWikiPage() {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getMultiWiki();
    PortalWikiContainer portalWiki = wStore.createPortalWikiContainer();
    wStore.setPortalWikis(portalWiki);
    PortalWiki wiki = portalWiki.addWiki("classic");
    WikiHome wikiHomePage = wiki.getWikiHome();
    PageImpl wikipage = wiki.createWikiPage();
    wikipage.setName("Hello World Wiki Page");
    wikiHomePage.addWikiPage(wikipage);
    assertSame(wikipage, wikiHomePage.getChildPages().iterator().next());
    PageImpl wikiChildPage = wiki.createWikiPage();
    wikiChildPage.setName("Hello World Wiki  Child Page");
    wikiChildPage.setParentPage(wikipage);
    assertSame(wikiChildPage, wikipage.getChildPages().iterator().next());
  }

}
