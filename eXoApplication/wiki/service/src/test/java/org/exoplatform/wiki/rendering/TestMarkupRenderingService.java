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
package org.exoplatform.wiki.rendering;

import junit.framework.TestCase;

import org.exoplatform.wiki.rendering.xwiki.XWikiRenderer;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Nov
 * 5, 2009
 */
public class TestMarkupRenderingService extends TestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testGetRenderer() {
    MarkupRenderingService service = new MarkupRenderingService();
    Renderer renderer = service.getRenderer("xwiki");

    assertTrue((renderer instanceof XWikiRenderer));
  }

  public void testXDOMToMow() throws Exception {
    String markup = "This is a [[Link]] in the middle";
    MarkupRenderingService service = new MarkupRenderingService();
    Renderer renderer = service.getRenderer("xwiki");

  }

}
