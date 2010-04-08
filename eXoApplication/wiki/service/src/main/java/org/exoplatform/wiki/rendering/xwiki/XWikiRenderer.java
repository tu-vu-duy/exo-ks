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

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.exoplatform.wiki.rendering.Renderer;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.context.Execution;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.converter.ConversionException;
import org.xwiki.rendering.converter.Converter;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.TransformationException;
import org.xwiki.rendering.transformation.TransformationManager;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Nov
 * 5, 2009
 */
public class XWikiRenderer implements Renderer {

  EmbeddableComponentManager componentManager = null;

  public Execution getExecutionContext() throws ComponentLookupException, ComponentRepositoryException{
    return getComponentManager().lookup(Execution.class);
  }
  
  /*
   * (non-Javadoc)
   * @see poc.wiki.rendering.Renderer#render(java.lang.String)
   */
  public String render(String markup) throws Exception {

    // Step 1: Find the parser and generate a XDOM
    XDOM xdom = parse(new StringReader(markup), Syntax.XWIKI_2_0);
    outputTree(xdom, 0);
    WikiPrinter printer = convert(xdom, Syntax.XWIKI_2_0, Syntax.XHTML_1_0);
    return printer.toString();
  }

  private Converter getConverter() throws Exception {
    Converter converter = getComponentManager().lookup(Converter.class);
    return converter;
  }

  private ComponentManager getComponentManager() throws ComponentRepositoryException {
    if (this.componentManager == null) {
      // Initialize Rendering components and allow getting instances
      componentManager = new EmbeddableComponentManager();
      componentManager.initialize(this.getClass().getClassLoader());
    }
    return componentManager;
  }

  private void outputTree(Block parent, int level) {
    StringBuffer buf = new StringBuffer();
    int i = 0;
    while (i++ < level) {
      buf.append("  ");
    }
    buf.append(parent.getClass().getSimpleName());
    System.out.println(buf.toString());
    List<Block> children = parent.getChildren();
    for (Block block : children) {
      outputTree(block, level + 1);
    }
  }

  /*
   * private XDOM traverseTo(Block parent, int level) { StringBuffer buf = new
   * StringBuffer(); int i = 0; while(i++<level) { buf.append("  "); }
   * buf.append(parent.getClass().getSimpleName());
   * System.out.println(buf.toString()); List<Block> children =
   * parent.getChildren(); for (Block block : children) { outputTree(block,
   * level+1); } }
   */
  private WikiPrinter convert(XDOM xdom, Syntax sourceSyntax, Syntax targetSyntax) throws Exception {

    // Step 2: Run transformations
    try {
      TransformationManager transformationManager = getComponentManager().lookup(TransformationManager.class);
      transformationManager.performTransformations(xdom, sourceSyntax);
    } catch (TransformationException e) {
      throw new ConversionException("Failed to execute some transformations", e);
    }

    // Step 3: Locate the Renderer and render the content in the passed printer
    WikiPrinter printer = new DefaultWikiPrinter();
    BlockRenderer renderer;
    try {
      renderer = getComponentManager().lookup(BlockRenderer.class, targetSyntax.toIdString());
    } catch (ComponentLookupException e) {
      throw new ConversionException("Failed to locate Renderer for syntax [" + targetSyntax + "]",
                                    e);
    }
    renderer.render(xdom, printer);
    return printer;

  }

  private XDOM parse(Reader source, Syntax sourceSyntax) throws Exception {
    XDOM xdom;
    try {
      Parser parser = getComponentManager().lookup(Parser.class, sourceSyntax.toIdString());
      xdom = parser.parse(source);
    } catch (ComponentLookupException e) {
      throw new ConversionException("Failed to locate Parser for syntax [" + sourceSyntax + "]", e);
    } catch (ParseException e) {
      throw new ConversionException("Failed to parse input source", e);
    }
    return xdom;
  }

}
