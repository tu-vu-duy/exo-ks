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
package org.exoplatform.chrome;


import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.chrome.api.Chrome;
import org.exoplatform.chrome.api.ChromeBuilder;
import org.exoplatform.chrome.core.DomainSession;
import org.exoplatform.forum.service.impl.mapping.CategoryMapping;
import org.exoplatform.forum.service.impl.mapping.ForumMapping;
import org.exoplatform.services.log.ExoLogger;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jul 18, 2009  
 */
public class ChromeService implements Startable {
  
  Log log = ExoLogger.getLogger(ChromeService.class);
  /** . */
  private ChromeBuilder builder;

  /** . */
  private Chrome chrome;



  public Chrome getChrome() {
    return chrome;
  }

  public DomainSession login() throws RepositoryException {
    return (DomainSession)chrome.openSession();
  }

  protected final void addClass(Class<?> clazz) {
    builder.add(clazz);
  }

  public ChromeService() {
    
  }
  
  public void start() {
    builder = ChromeBuilder.create();
    builder.setOption(ChromeBuilder.INSTRUMENTOR_CLASSNAME, "org.exoplatform.chrome.cglib.CGLibInstrumentor");
    builder.setOption(ChromeBuilder.SESSION_PROVIDER_CLASSNAME, "org.exoplatform.chrome.ExoSystemSessionProvider");
    createDomain();
    try {
      chrome = builder.build();
    } catch (Exception e) {
      log.error("Failed to instantiate jcr mapping", e);
    }
    
  }

  private void createDomain() {
    addClass(CategoryMapping.class);
    addClass(ForumMapping.class);
  }

  public void stop() {
    // TODO Auto-generated method stub
    
  }

}
