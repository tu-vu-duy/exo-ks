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
package org.exoplatform.forum.service.impl.mapping;

import org.exoplatform.chrome.api.annotations.NodeMapping;
import org.exoplatform.forum.service.Forum;

@NodeMapping(name = "exo:forum")
public abstract class ForumMapping implements Forum {


  public String getLastTopicPath() {
    String result = null;
    if (getLastTopic() != null) {
      result = getLastTopic() ;
      if(result.trim().length() > 0){
        if(result.lastIndexOf("/") > 0){
          result = getPath() + result.substring(result.lastIndexOf("/"));
        } else {
          result = getPath() + "/" + result;
        }
      }  
    }
    return result;
  }
  
  public void setLastTopicPath(String lastTopicPath) {
    setLastTopic(lastTopicPath);
  }

  public String getCategoryId(){
    if(getPath() != null && getPath().length() > 0) {
      String[] arr = getPath().split("/");
      String result = arr[arr.length - 2];
      return result;
    }
    return null;
  }


}
