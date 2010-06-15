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
package org.exoplatform.wiki.rendering.impl;

import org.xwiki.rendering.syntax.Syntax;


/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jun 15, 2010  
 */
public class TestMacroRendering extends AbstractRenderingTestCase {
  
  public void testRenderInfoMacro() throws Exception {
    String expectedHtml = "<div class=\"box infomessage\">This is an info.</div>";
    assertEquals(expectedHtml, renderingService.render("{{info}}This is an info.{{/info}}", Syntax.XWIKI_2_0.toIdString()));
  }

  public void testRenderWarningMacro() throws Exception {
    String expectedHtml = "<div class=\"box warningmessage\">This is an warning.</div>";
    assertEquals(expectedHtml, renderingService.render("{{warning}}This is an warning.{{/warning}}", Syntax.XWIKI_2_0.toIdString()));
  }

  public void testRenderErrorMacro() throws Exception {
    String expectedHtml = "<div class=\"box errormessage\">This is an error.</div>";
    assertEquals(expectedHtml, renderingService.render("{{error}}This is an error.{{/error}}", Syntax.XWIKI_2_0.toIdString()));
  }

  public void testRenderCodeMacro() throws Exception {
    String expectedHtml = "<div class=\"box code\"><span style=\"font-weight: bold; color: #008000; \">&lt;html&gt;&lt;head&gt;</span>Cool!<span style=\"font-weight: bold; color: #008000; \">&lt;/head&gt;&lt;/html&gt;</span></div>";
    assertEquals(expectedHtml, renderingService.render("{{code language=\"html\"}}<html><head>Cool!</head></html>{{/code}}", Syntax.XWIKI_2_0.toIdString()));
  }
  
}
