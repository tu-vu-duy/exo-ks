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
package org.exoplatform.faq.service;
/**
 * Created by The eXo Platform SARL
 * 
 * This Object is used to set some properties of FAQ.
 *  
 * Author : Truong Nguyen
 *					truong.nguyen@exoplatform.com
 * Apr 10, 2008, 2:07:25 PM
 */
public class FAQSetting {
	private String displayMode ;
	private String orderBy ;
	private String orderType ;
	
	public static String DISPLAY_TYPE_ALPHABET = "alphabet" ;
	public static String DISPLAY_TYPE_POSTDATE = "created" ;
	public static String ORDERBY_TYPE_ASC = "asc" ;
	public static String ORDERBY_TYPE_DESC = "desc" ;
	
	/**
	 * This method get one value is Alphabet or Post Date
	 * 
	 * @return displayType
	 */
	public String getDisplayMode() { return displayMode ;}
	
	/**
	 * All categories/questions can be displayed in some types depending on users.
	 * This is a combobox with two values: Alphabet or Post Date
	 * 
	 * @param dis  the string to display is Alphabet or Post Date
	 */
	public void setDisplayMode(String displayMode) { this.displayMode = displayMode ;}

	public String getOrderBy() {
  	return orderBy;
  }

	public void setOrderBy(String orderBy) {
  	this.orderBy = orderBy;
  }

	public String getOrderType() {
  	return orderType;
  }

	public void setOrderType(String orderType) {
  	this.orderType = orderType;
  } 
	
	
}
