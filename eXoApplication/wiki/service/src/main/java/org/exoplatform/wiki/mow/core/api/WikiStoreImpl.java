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
package org.exoplatform.wiki.mow.core.api;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Owner;
import org.chromattic.api.annotations.PrimaryType;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiStore;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.wiki.GroupWikiContainer;
import org.exoplatform.wiki.mow.core.api.wiki.PortalWikiContainer;
import org.exoplatform.wiki.mow.core.api.wiki.UserWikiContainer;
import org.exoplatform.wiki.mow.core.api.wiki.WikiContainer;

/**
 * A Wiki store for portal, group and user wikis
 * 
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
@PrimaryType(name = "wiki:store")
public abstract class WikiStoreImpl implements WikiStore {

  public void addWiki(WikiType wikiType, String name) {
    getWikiContainer(wikiType).addWiki(name);
  }

  public Wiki getWiki(WikiType wikiType, String name) {
    return getWikiContainer(wikiType).getWiki(name);
  }

  public Collection<Wiki> getWikis() {
    Collection<Wiki> col = new CopyOnWriteArraySet<Wiki>();
    col.addAll(getPortalWikis().getAllWikis());
    col.addAll(getGroupWikis().getAllWikis());
    col.addAll(getUserWikis().getAllWikis());
    return col;
  }

  @SuppressWarnings("unchecked")
  private <W extends Wiki> WikiContainer<W> getWikiContainer(WikiType wikiType) {
    if (wikiType == WikiType.PORTAL) {
      return (WikiContainer<W>) getPortalWikis();
    } else if (wikiType == WikiType.GROUP) {
      return (WikiContainer<W>) getGroupWikis();
    } else if (wikiType == WikiType.USER) {
      return (WikiContainer<W>) getUserWikis();
    } else {
      throw new UnsupportedOperationException();
    }
  }

  @OneToOne
  @Owner
  @MappedBy("portalwikis")
  public abstract PortalWikiContainer getPortalWikis();

  public abstract void setPortalWikis(PortalWikiContainer pContainer);

  @Create
  public abstract PortalWikiContainer createPortalWikiContainer();

  @OneToOne
  @Owner
  @MappedBy("groupwikis")
  public abstract GroupWikiContainer getGroupWikis();

  @Create
  public abstract GroupWikiContainer createGroupWikiContainer();

  @OneToOne
  @Owner
  @MappedBy("userwikis")
  public abstract UserWikiContainer getUserWikis();

  @Create
  public abstract UserWikiContainer createUserWikiContainer();

}
