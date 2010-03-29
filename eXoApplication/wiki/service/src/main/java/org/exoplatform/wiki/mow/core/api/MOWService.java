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

import org.chromattic.api.Chromattic;
import org.chromattic.api.ChromatticBuilder;
import org.chromattic.api.ChromatticSession;
import org.exoplatform.wiki.mow.core.api.content.AnnotationImpl;
import org.exoplatform.wiki.mow.core.api.content.ContentImpl;
import org.exoplatform.wiki.mow.core.api.content.MarkupImpl;
import org.exoplatform.wiki.mow.core.api.content.ParagraphImpl;
import org.exoplatform.wiki.mow.core.api.content.WikiLink;
import org.exoplatform.wiki.mow.core.api.wiki.GroupWiki;
import org.exoplatform.wiki.mow.core.api.wiki.GroupWikiContainer;
import org.exoplatform.wiki.mow.core.api.wiki.PortalWiki;
import org.exoplatform.wiki.mow.core.api.wiki.PortalWikiContainer;
import org.exoplatform.wiki.mow.core.api.wiki.UserWiki;
import org.exoplatform.wiki.mow.core.api.wiki.UserWikiContainer;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class MOWService {

  /** . */
  private Chromattic              chromattic;

  /** . */
  private final ChromatticBuilder builder;

  public MOWService() {
    ChromatticBuilder builder = ChromatticBuilder.create();
    // builder.setOption(ChromatticBuilder.INSTRUMENTOR_CLASSNAME,
    // "org.chromattic.cglib.CGLibInstrumentor");
    builder.setOptionValue(ChromatticBuilder.INSTRUMENTOR_CLASSNAME,
                           "org.chromattic.apt.InstrumentorImpl");
    // builder.setOption(ChromatticBuilder.OBJECT_FORMATTER_CLASSNAME,
    // MOPFormatter.class.getName());

    //
    this.builder = builder;
  }

  public <T> void setOption(ChromatticBuilder.Option<T> option, T value) {
    builder.setOptionValue(option, value);
  }

  public void start() throws Exception {

    builder.add(WikiStoreImpl.class);

    builder.add(PortalWiki.class);
    builder.add(PortalWikiContainer.class);
    builder.add(GroupWiki.class);
    builder.add(GroupWikiContainer.class);
    builder.add(UserWiki.class);
    builder.add(UserWikiContainer.class);

    builder.add(ContentImpl.class);
    builder.add(ParagraphImpl.class);
    builder.add(MarkupImpl.class);
    builder.add(WikiLink.class);
    builder.add(AnnotationImpl.class);

    //
    chromattic = builder.build();

    //
    this.chromattic = builder.build();

  }

  public ModelImpl getModel() {
    ChromatticSession chromeSession = chromattic.openSession();
    return new ModelImpl(chromeSession);
  }
}
