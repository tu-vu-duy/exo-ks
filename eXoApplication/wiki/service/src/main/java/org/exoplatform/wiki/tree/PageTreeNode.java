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
package org.exoplatform.wiki.tree;

import java.util.Iterator;

import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Aug 6, 2010  
 */
public class PageTreeNode extends TreeNode {
  private PageImpl           page; 

  public PageTreeNode(PageImpl page, String parentPath) throws Exception {
    this.page = page;
    this.name = page.getContent().getTitle();
    this.absPath = parentPath + "/" + page.getName();    
    this.hasChild=   this.page.getChildPages().size()>0;
    this.nodeType= TreeNodeType.PAGE;
  }
  
  public void setChildren() throws Exception
  {
    Iterator<PageImpl> childPageIterator = page.getChildPages().values().iterator();
    while (childPageIterator.hasNext()) {
      PageTreeNode child = new PageTreeNode(childPageIterator.next(), this.absPath);
      this.children.add(child);
    }
  }  

  public PageTreeNode getChildByName(String name) throws Exception {
    for (TreeNode child : children) {
      if (child.getName().equals(name))
        return (PageTreeNode)child;
    }
    return null;
  }

}
