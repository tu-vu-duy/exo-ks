package org.exoplatform.wiki.resolver;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;

public class PageResolver {
  
  private Log LOG = ExoLogger.getLogger(PageResolver.class);
  private WikiService wService ;
  private Resolver resolver ;
  
  public PageResolver (WikiService wService) {
    this.wService = wService ; 
  }
  
  public void setResolverPlugin(ComponentPlugin plugin) throws Exception {
    resolver = (Resolver)plugin ;
  }
  
  //If for some reason (such as difference class loaders), we couldn't set ResolverPlugin, resolver parameter must be provided.
  public Page resolve(String requestURI, Resolver resolver) throws Exception {
    
    WikiPageParams params;
    if(this.resolver != null){
      params = this.resolver.extractPageParams(requestURI) ;
    }
    else if(resolver != null){
      params = resolver.extractPageParams(requestURI) ;
    }else{
      LOG.error("Couldn't resolve URI: " + requestURI+ ". ResolverPlugin is not set!");
      return null;
    }

    Page page = wService.getPageById(params.getType(), params.getOwner(), params.getPageId());
    
    return page;

  }
}
