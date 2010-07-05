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
package org.exoplatform.wiki.rendering.render.confluence;

import java.util.Map;

import org.xwiki.rendering.internal.renderer.ParametersPrinter;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jul 2, 2010  
 */

/**
 * Generates Confluence Syntax for a Macro Block.
 */
public class ConfluenceSyntaxMacroRenderer {
  
  private ParametersPrinter parametersPrinter = new ParametersPrinter();

  public String renderMacro(String id, Map<String, String> parameters, String content, boolean isInline) {
    StringBuffer buffer = new StringBuffer();

    // Print begin macro
    buffer.append("{");
    buffer.append(id);

    // Print parameters
    if (!parameters.isEmpty()) {
      buffer.append(':');
      buffer.append(renderMacroParameters(parameters));
    }

    // Print content and end macro
    if (content == null) {
      buffer.append("}");
    } else {
      buffer.append("}");
      if (content.length() > 0) {
        if (!isInline) {
          buffer.append("\n");
        }
        buffer.append(content);
        if (!isInline) {
          buffer.append("\n");
        }
      }
      buffer.append("{").append(id).append("}");
    }

    return buffer.toString();
  }

  public String renderMacroParameters(Map<String, String> parameters) {
    return this.parametersPrinter.print(parameters, '~').replace("}", "~}");
  }
}
