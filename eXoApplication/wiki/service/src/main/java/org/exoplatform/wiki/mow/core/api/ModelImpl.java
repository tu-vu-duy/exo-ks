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

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.chromattic.api.ChromatticSession;
import org.chromattic.api.UndeclaredRepositoryException;
import org.exoplatform.wiki.mow.api.Model;
import org.exoplatform.wiki.mow.api.WikiStore;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class ModelImpl implements Model {

  /** . */
  private final ChromatticSession session;

  /** . */
  private WikiStoreImpl           store;

  public ModelImpl(ChromatticSession chromeSession) {
    this.session = chromeSession;
  }

  public WikiStore getMultiWiki() {
    if (store == null) {
      store = session.findByPath(WikiStoreImpl.class, "exo:applications/eXoWiki/wikistore");
      if (store == null) {
        try {
          Node rootNode = session.getJCRSession().getRootNode();
          Node publicApplicationNode = rootNode.getNode("exo:applications");
          Node eXoWiki = null;
          try {
            eXoWiki = publicApplicationNode.getNode("eXoWiki");
          } catch (PathNotFoundException e) {
            eXoWiki = publicApplicationNode.addNode("eXoWiki");
            publicApplicationNode.save();
          }
          Node wikiMetadata = eXoWiki.addNode("wikimetadata", "wiki:store");
          Node wikis = eXoWiki.addNode("wikis", "nt:unstructured");
          eXoWiki.save();
          store = session.findByNode(WikiStoreImpl.class, wikiMetadata);
        } catch (RepositoryException e) {
          throw new UndeclaredRepositoryException(e);
        }
      }
    }
    return store;
  }

  public void save() {
    session.save();
  }

  public void close() {
    session.close();
  }

}
