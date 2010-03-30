package org.exoplatform.wiki.commons;

import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.resolver.Resolver;
import org.exoplatform.wiki.service.WikiPageParams;

public class URLResolver implements Resolver{

  public WikiPageParams extractPageParams(String requestURI) throws Exception {
    WikiPageParams params = new WikiPageParams() ;
    params.setType(extractWikiType(requestURI)) ;
    params.setOwner(extractOwner(requestURI)) ;
    params.setPageId(extractWikiPageUri(requestURI)) ;
    return params;
  }
  
  private String extractWikiType(String requestURL) throws Exception {
    UIPortal uiPortal = Util.getUIPortal();
    PageNavigation nav = uiPortal.getSelectedNavigation();
    if (PortalConfig.PORTAL_TYPE.equalsIgnoreCase(nav.getOwnerType())) {
      return WikiType.PORTAL.toString();
    } else if (PortalConfig.GROUP_TYPE.equalsIgnoreCase(nav.getOwnerType())) {
      return WikiType.GROUP.toString();
    } else {
      return WikiType.USER.toString();
    }

  }
  
  private String extractOwner(String requestURI) throws Exception {
    UIPortal uiPortal = Util.getUIPortal();
    PageNavigation nav = uiPortal.getSelectedNavigation();
    return nav.getOwnerId();
  }

  private String extractWikiPageUri(String requestURI) throws Exception {
    UIPortal uiPortal = Util.getUIPortal();
    PageNode node = uiPortal.getSelectedNode();
    String nodeuri = node.getUri();
    int beginIndex = requestURI.indexOf(nodeuri) + nodeuri.length();
    int endIndex = requestURI.length();
    String pageID = requestURI.substring(beginIndex, endIndex);
    if (pageID.length() == 0) {
      pageID = "WikiHome"; // TODO use a const
    }
    return pageID;
  }
  
  
  public String getDescription() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setDescription(String s) {
    // TODO Auto-generated method stub
    
  }

  public void setName(String s) {
    // TODO Auto-generated method stub
    
  }

 

}
