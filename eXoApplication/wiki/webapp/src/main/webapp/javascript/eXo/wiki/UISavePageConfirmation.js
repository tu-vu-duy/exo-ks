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

function UISavePageConfirmation() {	
};

/*ie bug  you cannot have more than one button tag*/
/**
 * Submits a form with the given action and the given parameters
 */

UISavePageConfirmation.prototype.validateSave = function(pageTitleinputId,currentMode) {
	var ConfirmMask= document.getElementById("ConfirmMask");
	var pageTitleInput= document.getElementById(pageTitleinputId);
	if ((currentMode=="NEW")&&(pageTitleInput.value=="Untitle")){
		ConfirmMask.style.display="block";
		var ConfirmMessage= eXo.core.DOMUtil.findFirstDescendantByClass(ConfirmMask,"div", "ConfirmMessage");
		ConfirmMessage.innerHTML="You are about to save an Untitled page.";
		return false;
	}
	else if (currentMode=="EDIT"){
		ConfirmMask.style.display="block";	
		var ConfirmMessage= eXo.core.DOMUtil.findFirstDescendantByClass(ConfirmMask,"div", "ConfirmMessage");
		ConfirmMessage.innerHTML="Your changes will be saved to history.<br/>Are you sure you want to apply this changes?";		
		return false;
	}
	return true;
} ;

UISavePageConfirmation.prototype.closeConfirm =function(){
		var ConfirmMask= document.getElementById("ConfirmMask");
		ConfirmMask.style.display="none";
		return false;
	}
UISavePageConfirmation.prototype.initDragDrop = function(){		
	var ConfirmBox= document.getElementById("ConfirmBox");
	var ConfirmBox = eXo.core.DOMUtil.findFirstDescendantByClass(ConfirmMask,"div", "ConfirmBox");
	var ConfirmTitle = eXo.core.DOMUtil.findFirstDescendantByClass(ConfirmMask,"div", "ConfirmTitle");
	ConfirmTitle.onmousedown = function(e){
		eXo.core.DragDrop.init(null,ConfirmTitle,ConfirmBox,e);			
	}	
}
eXo.wiki.UISavePageConfirmation = new UISavePageConfirmation();