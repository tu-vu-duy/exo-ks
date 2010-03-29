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
package org.exoplatform.wiki.rendering.xwiki;

import junit.framework.TestCase;

import org.exoplatform.wiki.rendering.MarkupRenderingService;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Nov
 * 5, 2009
 */
public class TestXWikiRenderer extends TestCase {

  XWikiRenderer renderer;

  protected void setUp() throws Exception {
    super.setUp();
    MarkupRenderingService service = new MarkupRenderingService();
    renderer = (XWikiRenderer) service.getRenderer("xwiki");
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testRender() throws Exception {
    assertEquals("<p>This is <strong>bold</strong></p>", renderer.render("This is **bold**"));
  }

  public void testLinks() throws Exception {
    assertTrue(renderer.render("[[OK]]").contains("wikilink")); // <p><span
    // class="wikilink"><a
    // href="#view"><span
    // class="wikigeneratedlinkcontent">OK</span></a></span></p>
    assertTrue(renderer.render("[[KO]]").contains("wikicreatelink")); // <p><span
    // class="wikicreatelink"><a
    // href="#edit"><span
    // class="wikigeneratedlinkcontent">KO</span></a></span></p>

  }

}
