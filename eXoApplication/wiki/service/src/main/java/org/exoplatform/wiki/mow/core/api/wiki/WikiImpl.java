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
package org.exoplatform.wiki.mow.core.api.wiki;

import org.chromattic.api.RelationshipType;
import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Property;
import org.exoplatform.wiki.mow.api.Wiki;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public abstract class WikiImpl implements Wiki {

  @OneToOne(type = RelationshipType.HIERARCHIC)
  @MappedBy("WikiHome")
  public abstract PageImpl getHome();

  public abstract void setHome(PageImpl homePage);

  @Create
  public abstract PageImpl createWikiPage();

  @Create
  public abstract PageImpl createWikiPage(String pageName);

  public PageImpl getWikiHome() {
    PageImpl home = getHome();
    if (home == null) {
      home = createWikiPage("WikiHome");
      setHome(home);
    }
    return home;
  }

  @Property(name = "name")
  public abstract String getName();

  @Property(name = "owner")
  public abstract String getOwner();

  public PageImpl getPageByID(String id) {
    throw new UnsupportedOperationException();
  }

  public PageImpl getPageByURI(String uri) {
    throw new UnsupportedOperationException();
  }

}
