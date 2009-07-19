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

import javax.jcr.Credentials;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.chrome.spi.jcr.SessionProvider;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.ExoLogger;

/**
 * SessionProvider that will authenticate with a System session from the RepositoryService.
 * Can be used inside eXo Portal.
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice lamarque</a>
 * @version $Revision$
 */
public class ExoSystemSessionProvider implements SessionProvider {
  
  Log log = ExoLogger.getLogger(ExoSystemSessionProvider.class);
  

  public ExoSystemSessionProvider() throws Exception {
  }

  public Session login() throws RepositoryException {
    ManageableRepository repo = getRepository();
    String defaultWS = repo.getConfiguration().getDefaultWorkspaceName() ;
    return repo.getSystemSession(defaultWS);
  }

  public Session login(String workspace) throws RepositoryException {
    ManageableRepository repo = getRepository();
    return repo.getSystemSession(workspace);
  }

  public Session login(Credentials credentials, String workspace) throws RepositoryException {
    return getRepository().login(credentials, workspace);
  }

  public Session login(Credentials credentials) throws RepositoryException {
    ManageableRepository repo = getRepository();
    String defaultWS = repo.getConfiguration().getDefaultWorkspaceName() ;
    return getRepository().login(credentials, defaultWS);
  }

  
  private ManageableRepository getRepository() throws RepositoryException {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    RepositoryService repositoryService = (RepositoryService)container.getComponentInstanceOfType(RepositoryService.class);
    
    try {
      return repositoryService.getDefaultRepository();
    } catch (RepositoryConfigurationException e) {
      log.error("Could not retreive default repository ", e);
      throw new RepositoryException("ExoSystemSessionProvider could not retreive default repository ", e);
    }

  }
  

}
