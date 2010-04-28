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
package org.exoplatform.wiki.webui;

import java.util.Locale;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 28, 2010  
 */
public class PageMode {
  
  public static final PageMode NEW = new PageMode("new");
  
  public static final PageMode EXISTED = new PageMode("existed");

  private String name;

  public PageMode(String name)
  {
     if (name == null)
     {
        throw new NullPointerException();
     }
     this.name = name.toLowerCase(Locale.ENGLISH);
  }

  public boolean equals(Object o)
  {
     if (o == this)
     {
        return true;
     }
     if (o instanceof PageMode)
     {
       PageMode that = (PageMode)o;
        return name.equals(that.name);
     }
     return false;
  }

  public int hashCode()
  {
     return name.hashCode();
  }

  public String toString()
  {
     return name;
  }
}
