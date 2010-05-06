/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.wiki.service;

import java.util.List;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.wiki.mow.api.Page;

/**
 * Created by The eXo Platform SARL.
 * <p>
 * WikiService is interface provide functions for processing database
 * with wikis and pages include: add, edit, remove and searching data
 * 
 * @author  exoplatform
 * @since   Mar 04, 2010
 */
public interface WikiService {
	
	public Page createPage(String wikiType, String wikiOwner, String title, String parentId) throws Exception ;	
	public boolean deletePage(String wikiType, String wikiOwner, String pageId) throws Exception ;
	public boolean movePage(String pageId, String newParentId, String wikiType, String wikiOwner) throws Exception ;
	
	public Page getPageById(String wikiType, String wikiOwner, String pageId) throws Exception ;
	public Page getPageByUUID(String uuid) throws Exception ;	
	
	public PageList<Page> search(String wikiType, String wikiOwner, SearchData data) throws Exception ;
	public List<BreadcumbData> getBreadcumb(String wikiType, String wikiOwner, String pageId) throws Exception ;

}



