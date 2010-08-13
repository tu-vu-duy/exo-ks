/**
 * Copyright (C) 2010 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

function UIPageToolBarMore(){
};

UIPageToolBarMore.prototype.init = function(){
  var UIPageToolBarMoreObj= eXo.wiki.UIPageToolBarMore;
  var moreLink = document.getElementById("MoreLink");
  moreLink.onmouseover= function(e) {return UIPageToolBarMoreObj.hover(true);}
  moreLink.onmouseout= function(e) {return UIPageToolBarMoreObj.hover(false);}
  var moreMenu = document.getElementById("MoreMenu");
  moreMenu.onmouseover= function(e) {return UIPageToolBarMoreObj.hover(true);}
  moreMenu.onmouseout= function(e) {return UIPageToolBarMoreObj.hover(false);}
};

UIPageToolBarMore.prototype.hover = function(state){
  var moreMenu= document.getElementById("MoreMenu");

  if (state==true){
    moreMenu.style.display="block";
  }
  else{
    moreMenu.style.display="none";
  }  
};
eXo.wiki.UIPageToolBarMore = new UIPageToolBarMore();