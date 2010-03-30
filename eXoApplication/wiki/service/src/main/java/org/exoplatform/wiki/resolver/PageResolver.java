package org.exoplatform.wiki.resolver;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;

public class PageResolver {
  private WikiService wService ;
  private Resolver resolver ;
  
  public PageResolver (WikiService wService) {
    this.wService = wService ; 
  }
  
  public void setResolverPlugin(ComponentPlugin plugin) throws Exception {
    resolver = (Resolver)plugin ;
  }
  
  public Page resolve(String requestURI) throws Exception {

    WikiPageParams params = resolver.extractPageParams(requestURI) ;    

    Page page = wService.getPageById(params.getType(), params.getOwner(), params.getPageId());
    
    return page;

  }
}
