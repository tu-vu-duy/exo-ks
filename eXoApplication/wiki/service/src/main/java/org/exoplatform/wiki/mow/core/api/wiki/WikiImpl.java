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

import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.Name;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Owner;
import org.chromattic.api.annotations.Path;
import org.chromattic.api.annotations.Property;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.content.ContentImpl;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public abstract class WikiImpl implements Wiki {

  @OneToOne
  @Owner
  @MappedBy(WikiNodeType.Definition.WIKI_HOME_NAME)
  protected abstract WikiHome getHome();
  protected abstract void setHome(WikiHome homePage);

  @OneToOne
  @Owner
  @MappedBy(WikiNodeType.Definition.TRASH_NAME)
  public abstract Trash getTrash();
  public abstract void setTrash(Trash trash);
  
  @Create
  protected abstract WikiHome createWikiHome();

  @Create
  public abstract Trash createTrash();
  
  @Create
  public abstract PageImpl createWikiPage();
  
  public WikiHome getWikiHome() {
    WikiHome home = getHome();
    if (home == null) {
      home = createWikiHome();
      setHome(home);
      home.makeVersionable();
      home.setOwner(getOwner());
      ContentImpl content = home.getContent() ;
      content.setTitle(WikiNodeType.Definition.WIKI_HOME_TITLE) ;
      content.setSyntax("xwiki/2.0") ;
      content.setText("This is a [[**wiki home page of " + getOwner()+"**>>" + WikiNodeType.Definition.WIKI_HOME_TITLE +"]]") ;
    }
    return home;
  }

  @Name
  public abstract String getName();

  @Property(name = WikiNodeType.Definition.OWNER )
  public abstract String getOwner();
  
  public abstract void setOwner(String wikiOwner);
  
  @Path
  public abstract String getPath();
  
  public PageImpl getPageByID(String id) {
    throw new UnsupportedOperationException();
  }

  public PageImpl getPageByURI(String uri) {
    throw new UnsupportedOperationException();
  }

}
