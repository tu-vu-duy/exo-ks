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
package org.exoplatform.ks.discussion.api;

/**
 * Used when an object od the discussion api was fetched by ID but not found. 
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class ObjectNotFoundException extends DiscussionException {


  /**
   * 
   */
  private static final long serialVersionUID = -1482325973754501628L;
  
  /**
   * Identifier of the object that was not found
   */
  private String objectId;
  
  public ObjectNotFoundException(String objectId) {
    this.objectId = objectId;
  }

  public String getObjectId() {
    return objectId;
  }

  public String getMessage() {
    return "Could not find an object with the identifier " + objectId;
  }

}
