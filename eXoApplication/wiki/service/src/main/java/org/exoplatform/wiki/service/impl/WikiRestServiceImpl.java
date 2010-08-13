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
package org.exoplatform.wiki.service.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.impl.EnvironmentContext;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.MOWService;
import org.exoplatform.wiki.mow.core.api.WikiStoreImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.WikiHome;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.rendering.impl.RenderingServiceImpl;
import org.exoplatform.wiki.resolver.TitleResolver;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiRestService;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.tree.PageTreeNode;
import org.exoplatform.wiki.tree.SpaceTreeNode;
import org.exoplatform.wiki.tree.TreeNode;
import org.exoplatform.wiki.tree.WikiHomeTreeNode;
import org.exoplatform.wiki.tree.WikiTreeNode;
import org.exoplatform.wiki.utils.Utils;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jun 20, 2010  
 */
@Path("/wiki")
public class WikiRestServiceImpl implements WikiRestService, ResourceContainer {

  private final WikiService      wikiService;

  private final RenderingService renderingService;

  private static Log             log = ExoLogger.getLogger("wiki:WikiRestService");

  private final CacheControl     cc;
  
  private final MOWService mowService;

  public WikiRestServiceImpl(WikiService wikiService, RenderingService renderingService, MOWService mowService) {
    this.wikiService = wikiService;
    this.renderingService = renderingService;
    this.mowService = mowService;
    cc = new CacheControl();
    cc.setNoCache(true);
    cc.setNoStore(true);
  }

  /**
   * {@inheritDoc}
   */
  @GET
  @Path("/{wikiType}/{wikiOwner:.+}/{pageId}/content/")
  @Produces(MediaType.TEXT_HTML)
  public Response getWikiPageContent(@PathParam("wikiType") String wikiType,
                                     @PathParam("wikiOwner") String wikiOwner,
                                     @PathParam("pageId") String pageId,
                                     @QueryParam("portalURI") String portalURI,
                                     @QueryParam("sessionKey") String sessionKey,
                                     @QueryParam("markup") boolean isMarkup) {
    String pageContent = "";
    String syntaxId = "";
    if (sessionKey != null && sessionKey.length() > 0) {
      EnvironmentContext env = EnvironmentContext.getCurrent();
      HttpServletRequest request = (HttpServletRequest) env.get(HttpServletRequest.class);
      pageContent = (String) request.getSession().getAttribute(sessionKey);
      if (pageContent != null) {
        return Response.ok(pageContent, MediaType.TEXT_HTML).cacheControl(cc).build();
      }
    }
    try {
      Page page = wikiService.getPageById(wikiType, wikiOwner, pageId);
      if (page != null) {
        pageContent = page.getContent().getText();
        syntaxId = page.getContent().getSyntax();
        syntaxId = (syntaxId != null) ? syntaxId : Syntax.XWIKI_2_0.toIdString();
      }
      if (!isMarkup) {
        Execution ec = ((RenderingServiceImpl) renderingService).getExecutionContext();
        ec.setContext(new ExecutionContext());
        WikiContext wikiContext = new WikiContext();
        wikiContext.setPortalURI(portalURI);
        wikiContext.setPortletURI("wiki");
        wikiContext.setType(wikiType);
        wikiContext.setOwner(wikiOwner);
        wikiContext.setPageId(pageId);
        ec.getContext().setProperty(WikiContext.WIKICONTEXT, wikiContext);
        pageContent = renderingService.render(pageContent, syntaxId, Syntax.ANNOTATED_XHTML_1_0.toIdString());
        ec.removeContext();
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return Response.serverError().entity(e.getMessage()).cacheControl(cc).build();
    }
    return Response.ok(pageContent, MediaType.TEXT_HTML).cacheControl(cc).build();
  }

  @GET
  @Path("/{path}")
  @Produces(MediaType.TEXT_HTML)
  public Response getWikiTreeData(@PathParam("path") String absNodePath) {
    try {
      WikiStoreImpl store = (WikiStoreImpl) mowService.getModel().getWikiStore();

      absNodePath = absNodePath.replace(".", "/");
      absNodePath= URLDecoder.decode(absNodePath,"utf-8");
      StringBuilder responseData = new StringBuilder();

      String parentPath = "";
      if (absNodePath.indexOf("/") > 0) {
        parentPath = absNodePath.substring(0, absNodePath.lastIndexOf("/"));
      }
      WikiPageParams nodeParams = Utils.getPageParamsFromPath(absNodePath);
      if (nodeParams != null) {
        responseData.append("<div class=\"NodeGroup\">");
        String nodeType = nodeParams.getType();
        String nodeOwner = nodeParams.getOwner();
        String nodeName = nodeParams.getPageId();
        nodeName= TitleResolver.getPageId(nodeName, false);
        if ((nodeType != null) && (nodeOwner != null) && (nodeName != null)) {
          if (!nodeName.equals(WikiNodeType.Definition.WIKI_HOME_NAME)) {
            PageImpl expandPage = (PageImpl) wikiService.getPageById(nodeType, nodeOwner, nodeName);
            PageTreeNode expandPageNode = new PageTreeNode(expandPage, parentPath);
            expandPageNode.setChildren();
            responseData.append(expandNode(expandPageNode).toString());
          } else {
            Wiki expandWiki = store.getWikiContainer(WikiType.valueOf(nodeType.toUpperCase()))
                                   .getWiki(nodeOwner);
            WikiHomeTreeNode expandWikiHome = new WikiHomeTreeNode((WikiHome) expandWiki.getWikiHome(),
                                                                   parentPath);
            expandWikiHome.setChildren();
            responseData.append(expandNode(expandWikiHome).toString());
          }
        } else if (nodeType != null && nodeOwner != null) {

          Wiki dataWiki = store.getWikiContainer(WikiType.valueOf(nodeType.toUpperCase()))
                               .getWiki(nodeOwner);
          WikiTreeNode expandWikiNode = new WikiTreeNode(dataWiki, parentPath);
          expandWikiNode.setChildren();
          responseData.append(expandNode(expandWikiNode).toString());
        } else {
          SpaceTreeNode expandSpaceNode = new SpaceTreeNode(nodeType);
          expandSpaceNode.setChildren();
          responseData.append(expandNode(expandSpaceNode).toString());
        }
      }
      responseData.append("</div>");
      return Response.ok(responseData.toString(), MediaType.TEXT_HTML).cacheControl(cc).build();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      log.error(e.getMessage(), e);
      return Response.serverError().entity(e.getMessage()).cacheControl(cc).build();
    }
  }

  public StringBuilder expandNode(TreeNode treeNode) throws UnsupportedEncodingException {
    StringBuilder responseData = new StringBuilder();
    int counter = 1;
    for (TreeNode child : treeNode.getChildren()) {
      boolean isLastNode = false;
      if (counter >= treeNode.getChildren().size()) {
        isLastNode = true;
      }
      responseData.append(renderNode(child, isLastNode));
      counter++;
    }
    return responseData;
  }

  public String renderNode(TreeNode treeNode, boolean isLastNode) throws UnsupportedEncodingException {
    StringBuffer sb = new StringBuffer();
    String nodeName = treeNode.getName();
    //Change Type for CSS
    String nodeType = treeNode.getNodeType().toString();
    String nodeTypeCSS= nodeType.substring(0,1).toUpperCase() + nodeType.substring(1).toLowerCase();
    String iconType = "Expand";
    String lastNodeClass = "";
    String absPath = treeNode.getAbsPath().replace("/", ".");
    if (isLastNode) {
      lastNodeClass = "LastNode";
    }
    if (!treeNode.isHasChild()) {
      iconType = "Empty";
    }
    sb.append("<div  class=\""+lastNodeClass+" Node\" >") ;
    sb.append("  <div class=\""+iconType+"Icon\" id=\"" + absPath + "\" onclick=\"event.cancelBubble=true;  if(eXo.wiki.UITreeExplorer.collapseExpand(this)) return;  eXo.wiki.UITreeExplorer.expandNode('" + URLEncoder.encode(absPath,"utf-8") + "', this)\">") ;
    sb.append( "    <div id=\"iconTreeExplorer\" onclick=\"event.cancelBubble=true;\"" + "class=\""+nodeTypeCSS+" NodeType Node \""  + ">");
    sb.append( "      <div class='NodeLabel'>") ;
    sb.append( "        <a  onclick=\"event.cancelBubble=true; eXo.wiki.UITreeExplorer.selectNode('"+absPath.replace(".", "/")+ "')\" style='cursor: pointer;' title=\""+nodeName+"\">"+nodeName+"</a>") ;
    sb.append( "      </div>") ; 
    sb.append( "    </div>") ; 
    sb.append( "  </div>") ; 
    sb.append( "</div>") ; 
    return sb.toString();
  }
}