package org.exoplatform.wiki.utils;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.wiki.GroupWiki;
import org.exoplatform.wiki.mow.core.api.wiki.PortalWiki;
import org.exoplatform.wiki.mow.core.api.wiki.UserWiki;

public class Utils {
  
  private static final String JCR_WEBDAV_SERVICE_BASE_URI = "/jcr";
  
  //The path should get from NodeHierarchyCreator 
  public static String getPortalWikisPath() {    
    String path = "/exo:applications/" 
    + WikiNodeType.Definition.WIKI_APPLICATION + "/"
    + WikiNodeType.Definition.WIKIS ; 
    return path ;
  }
  
  public static String validateWikiOwner(String wikiType, String wikiOwner){
    if(wikiType.equals(PortalConfig.GROUP_TYPE)) {
      if(wikiOwner == null || wikiOwner.length() == 0){
        return null;
      }
      if(wikiOwner.startsWith("/")){
        wikiOwner = wikiOwner.substring(1,wikiOwner.length());
      }
      if(wikiOwner.endsWith("/")){
        wikiOwner = wikiOwner.substring(0,wikiOwner.length()-1);
      }
    }
    return wikiOwner;
  }
  
  public static String getDefaultRepositoryWebDavUri() {
    StringBuilder sb = new StringBuilder();
    sb.append("/");
    sb.append(PortalContainer.getCurrentPortalContainerName());
    sb.append("/");
    sb.append(PortalContainer.getCurrentRestContextName());
    sb.append(JCR_WEBDAV_SERVICE_BASE_URI);
    sb.append("/");
    RepositoryService repositoryService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
    sb.append(repositoryService.getConfig().getDefaultRepositoryName());
    sb.append("/");
    return sb.toString();
  }
  
  public static String getCurrentUser() {
    try {
      ConversationState conversationState = ConversationState.getCurrent();
      return conversationState.getIdentity().getUserId();
    }catch(Exception e){}
    return "system" ;
  }
  
  public static String getWikiType(Wiki wiki) {
    if (wiki instanceof PortalWiki) {
      return PortalConfig.PORTAL_TYPE;
    } else if (wiki instanceof GroupWiki) {
      return PortalConfig.GROUP_TYPE;
    } else if (wiki instanceof UserWiki) {
      return PortalConfig.USER_TYPE;
    } else {
      return null;
    }
  }
  
}
