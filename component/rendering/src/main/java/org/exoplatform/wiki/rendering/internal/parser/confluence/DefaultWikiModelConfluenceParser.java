/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wiki.rendering.internal.parser.confluence;

import org.exoplatform.wiki.rendering.internal.parser.DefaultXWikiConfluenceGeneratorListener;
import org.wikimodel.wem.IWikiParser;
import org.wikimodel.wem.confluence.ConfluenceExtendedWikiParser;
import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.internal.parser.WikiModelConfluenceParser;
import org.xwiki.rendering.internal.parser.wikimodel.XWikiGeneratorListener;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.util.IdGenerator;

/**
 * Created by The eXo Platform SAS
 * Author : haidd 
 *          haidd@exoplatform.com
 * Jan 14, 2012  
 */
@Component("confluence/1.0")
public class DefaultWikiModelConfluenceParser extends WikiModelConfluenceParser {
  public IWikiParser createWikiModelParser() {
    return new ConfluenceExtendedWikiParser();
  }

  @Override
  public XWikiGeneratorListener createXWikiGeneratorListener(Listener listener,
                                                             IdGenerator idGenerator) {
    return new DefaultXWikiConfluenceGeneratorListener(getLinkLabelParser(),
                                                       listener,
                                                       getLinkReferenceParser(),
                                                       getImageReferenceParser(),
                                                       this.plainRendererFactory,
                                                       idGenerator);
  }
}
