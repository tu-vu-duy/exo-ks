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
package org.exoplatform.wiki.commons;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.servlet.http.HttpServletRequest;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.core.api.MOWService;
import org.exoplatform.wiki.mow.core.api.WikiStoreImpl;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.resolver.PageResolver;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.webui.UIWikiPageEditForm;
import org.exoplatform.wiki.webui.UIWikiRichTextArea;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 22, 2010  
 */
public class Utils {
  
  public static String getCurrentRequestURL() throws Exception {
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    HttpServletRequest request = portalRequestContext.getRequest();
    String requestURL = request.getRequestURL().toString();
    UIPortal uiPortal = Util.getUIPortal();
    String pageNodeSelected = uiPortal.getSelectedNode().getUri();
    if (!requestURL.contains(pageNodeSelected)) {
      // Happens at the first time processRender() called when add wiki portlet manually
      requestURL = portalRequestContext.getPortalURI() + pageNodeSelected;
    }
    return requestURL;
  }

  public static WikiPageParams getCurrentWikiPageParams() throws Exception {
    String requestURL = getCurrentRequestURL();
    PageResolver pageResolver = (PageResolver) PortalContainer.getComponent(PageResolver.class);
    WikiPageParams params = pageResolver.extractWikiPageParams(requestURL);
    HttpServletRequest request = Util.getPortalRequestContext().getRequest();
    Map<String, String[]> paramsMap = request.getParameterMap();
    Set<String> keys = paramsMap.keySet();
    for (String key : keys) {
      params.setParameter(key, paramsMap.get(key));
    }
    return params;
  }

  public static void reparePermissions(AttachmentImpl att) throws Exception {
    MOWService mowService = (MOWService) PortalContainer.getComponent(MOWService.class);
    WikiStoreImpl store = (WikiStoreImpl) mowService.getModel().getWikiStore();
    Node attNode = (Node) store.getSession().getJCRSession().getItem(att.getPath());
    ExtendedNode extNode = (ExtendedNode) attNode;
    if (extNode.canAddMixin("exo:privilegeable"))
      extNode.addMixin("exo:privilegeable");
    String[] arrayPers = { PermissionType.READ };
    extNode.setPermission("any", arrayPers);
    attNode.getSession().save();
  }

  public static Page getCurrentWikiPage() throws Exception {
    String requestURL = Utils.getCurrentRequestURL();
    PageResolver pageResolver = (PageResolver) PortalContainer.getComponent(PageResolver.class);
    Page page = pageResolver.resolve(requestURL);
    return page;
  }
  
  public static String getDownloadLink(String path, String filename, DownloadService dservice){
    if(dservice == null)dservice = (DownloadService)PortalContainer.getComponent(DownloadService.class) ;
    WikiService wservice = (WikiService)PortalContainer.getComponent(WikiService.class) ;
    try {
      InputStream input = wservice.getAttachmentAsStream(path) ;      
      byte[] attBytes = null;
      if (input != null) {
        attBytes = new byte[input.available()];
        input.read(attBytes);
        ByteArrayInputStream bytearray = new ByteArrayInputStream(attBytes);
        MimeTypeResolver mimeTypeResolver = new MimeTypeResolver() ;
        String mimeType = mimeTypeResolver.getMimeType(filename) ;
        InputStreamDownloadResource dresource = new InputStreamDownloadResource(bytearray, mimeType);
        dresource.setDownloadName(filename);
        return dservice.getDownloadLink(dservice.addDownloadResource(dresource));
      }
    } catch (Exception e) {     
    }
    return null;
  }
  
  public static String getExtension(String filename)throws Exception {
    MimeTypeResolver mimeResolver = new MimeTypeResolver() ;
    try{
      return mimeResolver.getExtension(mimeResolver.getMimeType(filename)) ;
    }catch(Exception e) {
      return mimeResolver.getDefaultMimeType() ;
    }    
  }
  
  public static String getCurrentWiki() throws Exception {
    return Utils.getCurrentWikiPageParams().getOwner();
  }
  
  public static void feedDataForWYSIWYGEditor(UIWikiPageEditForm pageEditForm, String xhtmlContent) throws Exception {
    if (xhtmlContent == null) {
      RenderingService renderingService = (RenderingService) PortalContainer.getComponent(RenderingService.class);
      String markupContent = pageEditForm.getUIFormTextAreaInput(UIWikiPageEditForm.FIELD_CONTENT).getValue();
      String markupSyntax = pageEditForm.getUIFormSelectBox(UIWikiPageEditForm.FIELD_SYNTAX).getValue();
      String htmlContent = renderingService.render(markupContent, markupSyntax, Syntax.ANNOTATED_XHTML_1_0.toIdString());
      Util.getPortalRequestContext().getRequest().getSession(false).setAttribute(UIWikiRichTextArea.SESSION_KEY, htmlContent);
    } else {
      Util.getPortalRequestContext().getRequest().getSession(false).setAttribute(UIWikiRichTextArea.SESSION_KEY, xhtmlContent);
    }
  }
}
