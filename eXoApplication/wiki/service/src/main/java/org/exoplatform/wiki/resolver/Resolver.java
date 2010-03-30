package org.exoplatform.wiki.resolver;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.wiki.service.WikiPageParams;

public interface Resolver extends ComponentPlugin{
  
  public WikiPageParams extractPageParams(String requestURI) throws Exception ;
  
}
