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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.io.FilenameUtils;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.impl.EnvironmentContext;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.MOWService;
import org.exoplatform.wiki.mow.core.api.WikiStoreImpl;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.WikiHome;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.rendering.impl.RenderingServiceImpl;
import org.exoplatform.wiki.resolver.TitleResolver;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiResource;
import org.exoplatform.wiki.service.WikiRestService;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.tree.PageTreeNode;
import org.exoplatform.wiki.tree.SpaceTreeNode;
import org.exoplatform.wiki.tree.TreeNode;
import org.exoplatform.wiki.tree.TreeNodeType;
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

  @POST
  @Path("/upload/{wikiType}/{wikiOwner:.+}/{pageId}/")
  public Response upload(@PathParam("wikiType") String wikiType,
                         @PathParam("wikiOwner") String wikiOwner,
                         @PathParam("pageId") String pageId) {
    EnvironmentContext env = EnvironmentContext.getCurrent();
    HttpServletRequest req = (HttpServletRequest) env.get(HttpServletRequest.class);
    boolean isMultipart = FileUploadBase.isMultipartContent(req);
    if (isMultipart) {
      DiskFileUpload upload = new DiskFileUpload();
      // Parse the request
      try {
        List<FileItem> items = upload.parseRequest(req);
        for (FileItem fileItem : items) {
          InputStream inputStream = fileItem.getInputStream();
          byte[] imageBytes;
          if (inputStream != null) {
            imageBytes = new byte[inputStream.available()];
            inputStream.read(imageBytes);
          } else {
            imageBytes = null;
          }
          String fileName = fileItem.getName();
          String fileType = fileItem.getContentType();
          if (fileName != null) {
            // It's necessary because IE posts full path of uploaded files
            fileName = FilenameUtils.getName(fileName);
            fileType = FilenameUtils.getExtension(fileName);
          }
          String mimeType = new MimeTypeResolver().getMimeType(fileName);
          WikiResource attachfile = new WikiResource(mimeType, "UTF-8", imageBytes);
          attachfile.setName(TitleResolver.getPageId(fileName, false));
          if (attachfile != null) {
            WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class);
            Page page = wikiService.getExsitedOrNewDraftPageById(wikiType, wikiOwner, pageId);
            if (page != null) {
              AttachmentImpl att = ((PageImpl) page).createAttachment(attachfile.getName(), attachfile);
              ConversationState conversationState = ConversationState.getCurrent();
              String creator = null;
              if (conversationState != null && conversationState.getIdentity() != null) {
                creator = conversationState.getIdentity().getUserId();
              }
              att.setCreator(creator);
              Utils.reparePermissions(att);
            }
          }
        }
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        return Response.status(HTTPStatus.BAD_REQUEST).entity(e.getMessage()).build();
      }
    }
    return Response.ok().build();
  }

  @GET
  @Path("/tree/{currentPath}/{expandPath}")
  @Produces(MediaType.TEXT_HTML)
  public Response getWikiTreeData(@PathParam("currentPath") String currentPath,@PathParam("expandPath") String expandPath) {
    try {
      currentPath= URLDecoder.decode(currentPath, "utf-8").replace(".", "/");
      expandPath= URLDecoder.decode(expandPath, "utf-8").replace(".", "/");
      WikiStoreImpl store = (WikiStoreImpl) mowService.getModel().getWikiStore();
     
      WikiPageParams currentPageParams= Utils.getPageParamsFromPath(currentPath);
      WikiPageParams expandPageParams= Utils.getPageParamsFromPath(expandPath);     
      StringBuilder responseData = new StringBuilder();
   
      String expandWikiType= expandPageParams.getType();
      String expandWikiOwner= expandPageParams.getOwner();
      String expandWikiPageId= expandPageParams.getPageId();      
    
      responseData.append("<div class=\"NodeGroup\">");
      if (expandWikiOwner!=null && expandWikiPageId!=null) {      
        if (!expandWikiPageId.equals(WikiNodeType.Definition.WIKI_HOME_NAME)) {
          //Expand a Page Node
          PageImpl expandPage = (PageImpl) wikiService.getPageById(expandWikiType, expandWikiOwner, expandWikiPageId);
          PageTreeNode expandPageNode = new PageTreeNode(expandPage);
          expandPageNode.setChildren();                  
          responseData.append(expandNode(expandPageNode,currentPageParams).toString());
        } else {
          //Expand a WikiHome Node
          Wiki expandWiki = store.getWikiContainer(WikiType.valueOf(expandWikiType.toUpperCase()))
                                 .getWiki(expandWikiOwner);
          WikiHome wikiHome= (WikiHome)expandWiki.getWikiHome();
          WikiHomeTreeNode expandWikiHome = new WikiHomeTreeNode((WikiHome) expandWiki.getWikiHome());
          expandWikiHome.setChildren();                 
          responseData.append(expandNode(expandWikiHome,currentPageParams).toString());
        }
      } else if (expandWikiOwner!=null) {
        //Expand a Wiki Node
        Wiki dataWiki = store.getWikiContainer(WikiType.valueOf(expandWikiType.toUpperCase()))
                             .getWiki(expandWikiOwner);
        WikiTreeNode expandWikiNode = new WikiTreeNode(dataWiki);
        expandWikiNode.setChildren();
        responseData.append(expandNode(expandWikiNode, currentPageParams).toString());
      } else {
        //Expand a Wiki Space Node
        SpaceTreeNode expandSpaceNode = new SpaceTreeNode(expandWikiType);
        expandSpaceNode.setChildren();
        responseData.append(expandNode(expandSpaceNode, currentPageParams).toString());
      }

      responseData.append("</div>");
      return Response.ok(responseData.toString(), MediaType.TEXT_HTML).cacheControl(cc).build();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      log.error(e.getMessage(), e);
      return Response.serverError().entity(e.getMessage()).cacheControl(cc).build();
    }
  }

  public StringBuilder expandNode(TreeNode treeNode, WikiPageParams currentPageParams) throws Exception {
    String currentPagePath = Utils.getPathFromPageParams(currentPageParams);
   
    StringBuilder responseData = new StringBuilder();
    int counter = 1;
    for (TreeNode child : treeNode.getChildren()) {
      boolean isSelectable = true;
      boolean isLastNode = false;
      if (counter >= treeNode.getChildren().size()) {
        isLastNode = true;
      }
      // if (child.getNodeType().equals(TreeNodeType.WIKIHOME)) {   isSelectable = true;}
      if (child.getNodeType().equals(TreeNodeType.WIKI)) {
        isSelectable = false;    
      } else if (child.getNodeType().equals(TreeNodeType.PAGE)) {
        PageImpl page = ((PageTreeNode) child).getPage();
        PageImpl currentPage = (PageImpl) wikiService.getPageById(currentPageParams.getType(),
                                                                  currentPageParams.getOwner(),
                                                                  currentPageParams.getPageId());      
        if (currentPage.equals(page) || Utils.isDescendantPage(page, currentPage)) isSelectable = false;
      }
      responseData.append(renderNode(child, isLastNode, isSelectable, currentPagePath));
      counter++;
    }
    return responseData;
  }

  public String renderNode(TreeNode treeNode, boolean isLastNode, boolean isSelectable, String currentPagePath) throws Exception {
    currentPagePath= currentPagePath.replace("/",".");   
    StringBuffer sb = new StringBuffer();
    String nodeName = treeNode.getName();
    // Change Type for CSS
    String nodeType = treeNode.getNodeType().toString();
    String nodeTypeCSS = nodeType.substring(0, 1).toUpperCase()
        + nodeType.substring(1).toLowerCase();
    String iconType = "Expand";
    String lastNodeClass = "";
    String absPath = treeNode.getAbsPath();
    String relPath= treeNode.getRelPath().replace("/",".");
    if (isLastNode) {
      lastNodeClass = "LastNode";
    }
    if (!treeNode.isHasChild()) {
      iconType = "Empty";
    }
    sb.append("<div  class=\""+lastNodeClass+" Node\" >") ;
    sb.append("  <div class=\""+iconType+"Icon\" id=\"" + relPath + "\" onclick=\"event.cancelBubble=true;  if(eXo.wiki.UITreeExplorer.collapseExpand(this)) return;  eXo.wiki.UITreeExplorer.expandNode('" +URLEncoder.encode( currentPagePath, "utf-8") + "/"+  URLEncoder.encode( relPath, "utf-8") + "', this)\">") ;
    sb.append( "    <div id=\"iconTreeExplorer\" onclick=\"event.cancelBubble=true;\"" + "class=\""+nodeTypeCSS+" NodeType Node \""  + ">");
    sb.append( "      <div class='NodeLabel'>") ;
    if (isSelectable){
    sb.append( "        <a  onclick=\"event.cancelBubble=true; eXo.wiki.UITreeExplorer.selectNode('"+absPath+ "')\" style='cursor: pointer;' title=\""+nodeName+"\">"+nodeName+"</a>") ;
    }
    else{
      sb.append( "    <span style=\"cursor:auto\" title=\""+nodeName+"\">"+nodeName+"</span>") ;  
    }      
    sb.append( "      </div>") ; 
    sb.append( "    </div>") ; 
    sb.append( "  </div>") ; 
    sb.append( "</div>") ; 
    return sb.toString();
  }
}