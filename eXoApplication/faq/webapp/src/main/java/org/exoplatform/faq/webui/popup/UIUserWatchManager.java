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
package org.exoplatform.faq.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIBreadcumbs;
import org.exoplatform.faq.webui.UIFAQContainer;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
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
 * Author : Truong Nguyen
 *					truong.nguyen@exoplatform.com
 * Oct 13, 2008, 11:30:44 AM
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =	"app:/templates/faq/webui/popup/UIUserWatchManager.gtmpl",
		events = {
				@EventConfig(listeners = UIUserWatchManager.LinkActionListener.class),
				@EventConfig(listeners = UIUserWatchManager.UnWatchActionListener.class),
				@EventConfig(listeners = UIUserWatchManager.CancelActionListener.class)
		}
)
public class UIUserWatchManager  extends UIForm	implements UIPopupComponent{
	private List<Category> listCategory_ = new ArrayList<Category>() ;
	private static FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
	public UIUserWatchManager() throws Exception {
		this.setActions(new String[]{"Cancel"}) ;
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
  
	public void setListCategory(List<Category> listCategory){this.listCategory_ = listCategory;}
  public List<Category> getListCategory() throws Exception {return listCategory_ ;}
	
  public String getPathService(String categoryId) throws Exception {
  	String oldPath = "";
		List<String> listPath = FAQUtils.getFAQService().getCategoryPath(FAQUtils.getSystemProvider(), categoryId) ;
    for(int i = listPath.size() -1 ; i >= 0; i --) {
    	oldPath = oldPath + " > " + listPath.get(i);
    }
    String path = this.getLabel("eXoFAQ")+ oldPath ;
    oldPath = path.substring(0, path.lastIndexOf(" > ")) ;
    Category category = FAQUtils.getFAQService().getCategoryById(categoryId, FAQUtils.getSystemProvider());
    return oldPath + " > " + category.getName();
  }  
  
  public static String getSubString(String str, int max) {
		if(!FAQUtils.isFieldEmpty(str)) {
			int l = str.length() ;
			if(l > max) {
				str = str.substring(0, (max-3)) ;
				int comma = str.lastIndexOf(",");
				if(comma > 0)
					str = str.substring(0, comma) + "...";
				else str = str + "..." ;
			}
		}
	  return str ;
  }
  
  
	static	public class LinkActionListener extends EventListener<UIUserWatchManager> {
		public void execute(Event<UIUserWatchManager> event) throws Exception {
			UIUserWatchManager watchManager = event.getSource() ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet uiPortlet = watchManager.getAncestorOfType(UIFAQPortlet.class) ;
			UIQuestions uiQuestions = uiPortlet.findFirstComponentOfType(UIQuestions.class) ;
			try {
				faqService_.getCategoryById(categoryId, FAQUtils.getSystemProvider()) ;
      } catch (Exception e) {
        UIApplication uiApplication = watchManager.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        uiQuestions.setIsNotChangeLanguage();
        UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class) ;
        popupAction.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
        return ;
      }
			uiQuestions.setCategories(categoryId) ;
			uiQuestions.setIsNotChangeLanguage() ;
	    UIBreadcumbs breadcumbs = uiPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;
	    breadcumbs.setUpdataPath(null) ;
      String oldPath = "" ;
	    List<String> listPath = faqService_.getCategoryPath(FAQUtils.getSystemProvider(), categoryId) ;
	    for(int i = listPath.size() -1 ; i >= 0; i --) {
	    	oldPath = oldPath + "/" + listPath.get(i);
	    }
	    String newPath = "FAQService"+oldPath ;
	    uiQuestions.setPath(newPath) ;
	    breadcumbs.setUpdataPath(newPath) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(breadcumbs) ;
	    UIFAQContainer fAQContainer = uiQuestions.getAncestorOfType(UIFAQContainer.class) ;
	    event.getRequestContext().addUIComponentToUpdateByAjax(fAQContainer) ;
	    uiPortlet.cancelAction() ;
		}
	}
	
	static	public class UnWatchActionListener extends EventListener<UIUserWatchManager> {
		public void execute(Event<UIUserWatchManager> event) throws Exception {
			UIUserWatchManager watchManager = event.getSource() ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			System.out.println("==>categoryId:" + categoryId);
			UIFAQPortlet uiPortlet = watchManager.getAncestorOfType(UIFAQPortlet.class);
			try {
				faqService_.getCategoryById(categoryId, FAQUtils.getSystemProvider()) ;
      } catch (Exception e) {
        UIApplication uiApplication = watchManager.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        UIQuestions uiQuestions =  uiPortlet.findFirstComponentOfType(UIQuestions.class) ;
        uiQuestions.setIsNotChangeLanguage();
        UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class) ;
        popupAction.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
        return ;
      }
//			faqService_.UnWatch(categoryId, FAQUtils.getSystemProvider(),FAQUtils.getCurrentUser()) ;
//			UISettingForm settingForm = uiPortlet.findFirstComponentOfType(UISettingForm.class) ;
//			if(settingForm.getCategoryAddWatch().size()>0) watchManager.setListCategory(settingForm.getCategoryAddWatch()) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(watchManager) ;
		}
	}
	
	static	public class CancelActionListener extends EventListener<UIUserWatchManager> {
		public void execute(Event<UIUserWatchManager> event) throws Exception {
			UIUserWatchManager watchManager = event.getSource() ;
      UIPopupAction uiPopupAction = watchManager.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
		}
	}
}
