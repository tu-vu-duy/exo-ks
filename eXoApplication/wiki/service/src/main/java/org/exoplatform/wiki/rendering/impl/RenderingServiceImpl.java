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
package org.exoplatform.wiki.rendering.impl;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.rendering.RenderingService;
import org.picocontainer.Startable;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.manager.ComponentLookupException;
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
public class RenderingServiceImpl implements RenderingService, Startable {

  private Log LOG = ExoLogger.getExoLogger(RenderingServiceImpl.class);
  EmbeddableComponentManager componentManager = null;

  public Execution getExecutionContext() throws ComponentLookupException, ComponentRepositoryException{
    return componentManager.lookup(Execution.class);
  }
  
  /*
   * (non-Javadoc)
   * @see poc.wiki.rendering.Renderer#render(java.lang.String)
   */
  public String render(String markup, String syntaxId) throws Exception {

    Syntax syntax = Syntax.XWIKI_2_0;
    if (Syntax.XWIKI_1_0.toIdString().equals(syntaxId)) {
      syntax = Syntax.XWIKI_1_0;
    } else if (Syntax.XWIKI_2_0.toIdString().equals(syntaxId)) {
      syntax = Syntax.XWIKI_2_0;
    } else if (Syntax.CREOLE_1_0.toIdString().equals(syntaxId)) {
      syntax = Syntax.CREOLE_1_0;
    } else if (Syntax.CONFLUENCE_1_0.toIdString().equals(syntaxId)) {
      syntax = Syntax.CONFLUENCE_1_0;
    } else if (Syntax.MEDIAWIKI_1_0.toIdString().equals(syntaxId)) {
      syntax = Syntax.MEDIAWIKI_1_0;
    } else if (Syntax.JSPWIKI_1_0.toIdString().equals(syntaxId)) {
      syntax = Syntax.JSPWIKI_1_0;
    } else if (Syntax.TWIKI_1_0.toIdString().equals(syntaxId)) {
      syntax = Syntax.TWIKI_1_0;
    }
    
    // Step 1: Find the parser and generate a XDOM
    XDOM xdom = parse(new StringReader(markup), syntax);
    outputTree(xdom, 0);
    WikiPrinter printer = convert(xdom, syntax, Syntax.XHTML_1_0);
    return printer.toString();
  }

  @Override
  public void start() {
    componentManager = new EmbeddableComponentManager();
    componentManager.initialize(this.getClass().getClassLoader());
  }

  @Override
  public void stop() {
  }
  
  private Converter getConverter() throws Exception {
    Converter converter = componentManager.lookup(Converter.class);
    return converter;
  }

  private void outputTree(Block parent, int level) {
    StringBuffer buf = new StringBuffer();
    int i = 0;
    while (i++ < level) {
      buf.append("  ");
    }
    buf.append(parent.getClass().getSimpleName());
    if(LOG.isDebugEnabled()){
      LOG.debug(buf.toString());
    }
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
      TransformationManager transformationManager = componentManager.lookup(TransformationManager.class);
      transformationManager.performTransformations(xdom, sourceSyntax);
    } catch (TransformationException e) {
      throw new ConversionException("Failed to execute some transformations", e);
    }

    // Step 3: Locate the Renderer and render the content in the passed printer
    WikiPrinter printer = new DefaultWikiPrinter();
    BlockRenderer renderer;
    try {
      renderer = componentManager.lookup(BlockRenderer.class, targetSyntax.toIdString());
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
      Parser parser = componentManager.lookup(Parser.class, sourceSyntax.toIdString());
      xdom = parser.parse(source);
    } catch (ComponentLookupException e) {
      throw new ConversionException("Failed to locate Parser for syntax [" + sourceSyntax + "]", e);
    } catch (ParseException e) {
      throw new ConversionException("Failed to parse input source", e);
    }
    return xdom;
  }

}
