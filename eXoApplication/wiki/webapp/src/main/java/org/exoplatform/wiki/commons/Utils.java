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

import javax.jcr.Node;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.core.api.MOWService;
import org.exoplatform.wiki.mow.core.api.WikiStoreImpl;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.resolver.PageResolver;
import org.exoplatform.wiki.service.WikiPageParams;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 22, 2010  
 */
public class Utils {

  public static final String WIKIURI = "wiki";
  
  public static String getCurrentRequestURL(){
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    HttpServletRequest request = portalRequestContext.getRequest();
    HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(request);
    String requestURL = requestWrapper.getRequestURL().toString();
    if(!requestURL.contains(WIKIURI)){
      //Happens at the first time processRender() called when add wiki portlet manually
      requestURL = portalRequestContext.getPortalURI()+WIKIURI;
    }
    return requestURL;
  }
  
  public static WikiPageParams getCurrentWikiPageParams(){
    String requestURL = getCurrentRequestURL();
    PageResolver pageResolver = (PageResolver) PortalContainer.getComponent(PageResolver.class);
    WikiPageParams params = pageResolver.extractWikiPageParams(requestURL);
    return params;
  }
  
  public static void reparePermissions(AttachmentImpl att) throws Exception {
    MOWService mowService = (MOWService)PortalContainer.getComponent(MOWService.class) ;
    WikiStoreImpl store = (WikiStoreImpl)mowService.getModel().getWikiStore() ;
    Node attNode = (Node)store.getSession().getJCRSession().getItem(att.getPath()) ;
    ExtendedNode extNode = (ExtendedNode)attNode ;
    if (extNode.canAddMixin("exo:privilegeable")) extNode.addMixin("exo:privilegeable");
    String[] arrayPers = {PermissionType.READ};
    extNode.setPermission("any", arrayPers) ;    
    attNode.getSession().save() ;
  }
  
  public static Page getCurrentWikiPage() throws Exception{
    String requestURL = Utils.getCurrentRequestURL();
    PageResolver pageResolver = (PageResolver) PortalContainer.getComponent(PageResolver.class);
    Page page = pageResolver.resolve(requestURL);
    return page;
  }
}
