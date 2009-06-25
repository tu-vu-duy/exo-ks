/***************************************************************************
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
 ***************************************************************************/
package org.exoplatform.faq.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Cate;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =	"app:/templates/faq/webui/popup/UIMoveCategoryForm.gtmpl",
		events = {
			@EventConfig(listeners = UIMoveCategoryForm.SaveActionListener.class),
			@EventConfig(listeners = UIMoveCategoryForm.CancelActionListener.class)
		}
)

public class UIMoveCategoryForm extends UIForm	implements UIPopupComponent{
	private String categoryId_ ;
	private String homeCategoryName;
	private FAQSetting faqSetting_ ;
	private List<Cate> listCate = new ArrayList<Cate>() ;
	private static FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
	public UIMoveCategoryForm() throws Exception {
		homeCategoryName = faqService_.getCategoryNameOf(Utils.CATEGORY_HOME) ;
	}

	public String getCategoryID() { return categoryId_; }
	public void setCategoryID(String s) { categoryId_ = s ; }

	public void activate() throws Exception {}
	public void deActivate() throws Exception {}

	public List<Cate> getListCate(){
		return this.listCate ;
	}

	public void setFAQSetting(FAQSetting faqSetting){
		this.faqSetting_ = faqSetting;
		String orderType = faqSetting.getOrderType() ;
		if(orderType.equals("asc")) faqSetting.setOrderType("desc") ;
		else faqSetting.setOrderType("asc") ;
	}

	public void setListCate() throws Exception {
		/*List<Cate> listCate = new ArrayList<Cate>() ;
		Cate cate ;
		for(Category category : faqService_.getSubCategories(null, faqSetting_, false, null)) {
			cate = new Cate() ;
			cate.setCategory(category) ;
			cate.setDeft(0) ;
			listCate.add(cate) ;
		}
		Cate parentCate;
		Cate childCate;
		while (!listCate.isEmpty()) {
			parentCate = new Cate() ;
			parentCate = listCate.get(listCate.size() - 1) ;
			listCate.remove(parentCate) ;
			this.listCate.add(parentCate) ;
			for(Category category : faqService_.getSubCategories(parentCate.getCategory().getPath(), faqSetting_, false, null)){
				childCate = new Cate() ;
				childCate.setCategory(category) ;
				childCate.setDeft(parentCate.getDeft() + 1) ;
				listCate.add(childCate) ;
			}
		}*/
		this.listCate.addAll(faqService_.listingCategoryTree()) ;
		String orderType = faqSetting_.getOrderType() ;
		if(orderType.equals("asc")) faqSetting_.setOrderType("desc") ;
		else faqSetting_.setOrderType("asc") ;
	}

	static public class SaveActionListener extends EventListener<UIMoveCategoryForm> {
		public void execute(Event<UIMoveCategoryForm> event) throws Exception {
			UIMoveCategoryForm moveCategory = event.getSource() ;	
			UIFAQPortlet faqPortlet = event.getSource().getAncestorOfType(UIFAQPortlet.class) ;
			String destCategoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			String categoryId = moveCategory.getCategoryID() ;
			try {
				boolean canMove = moveCategory.faqSetting_.isAdmin();
				if(!canMove) canMove = faqService_.isCategoryModerator(destCategoryId, FAQUtils.getCurrentUser()) ;
				if(canMove){
					faqService_.moveCategory(categoryId, destCategoryId) ;
				}else {
					UIApplication uiApplication = moveCategory.getAncestorOfType(UIApplication.class) ;
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.can-not-move-category", 
																		new Object[]{"destination"}, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					return;
				}
			}catch (Exception e) {
				UIApplication uiApplication = moveCategory.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
			faqPortlet.cancelAction() ;
		}
	}

	static public class CancelActionListener extends EventListener<UIMoveCategoryForm> {
		public void execute(Event<UIMoveCategoryForm> event) throws Exception {
			UIFAQPortlet faqPortlet = event.getSource().getAncestorOfType(UIFAQPortlet.class) ;
			faqPortlet.cancelAction() ;
		}
	}
}