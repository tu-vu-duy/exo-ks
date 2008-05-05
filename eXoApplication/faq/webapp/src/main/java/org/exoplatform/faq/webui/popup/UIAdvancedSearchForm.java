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
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQEventQuery;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@SuppressWarnings({ "unused", "unchecked" })
@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =	"app:/templates/faq/webui/popup/UIAdvancedSearchForm.gtmpl",
		events = {
				@EventConfig(listeners = UIAdvancedSearchForm.SearchActionListener.class),
				@EventConfig(listeners = UIAdvancedSearchForm.OnchangeActionListener.class, phase = Phase.DECODE),	
				@EventConfig(listeners = UIAdvancedSearchForm.CancelActionListener.class)
		}
)
public class UIAdvancedSearchForm extends UIForm implements UIPopupComponent	{
	final static	private String FIELD_SEARCHOBJECT_SELECTBOX = "SearchOject" ;
	
	final static private String FIELD_CATEGORY_NAME = "CategoryName" ;
	final static private String FIELD_CATEGORY_DESCRIPTIONS = "CategoryDescriptions" ;
	final static private String FROM_DATE = "FromDate" ;
	final static private String TO_DATE = "ToDate" ;
	
	final static private String AUTHOR = "Author" ;
	final static private String EMAIL_ADDRESS = "EmailAddress" ;
	final static private String LANGUAGE = "Language" ;
	final static private String QUESTION = "Question" ;
	
	public UIAdvancedSearchForm() throws Exception {}
	public void init() throws Exception {
		List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>("Category", "faqCategory")) ;
		list.add(new SelectItemOption<String>("Question", "faqQuestion")) ;
		UIFormSelectBox searchType = new UIFormSelectBox(FIELD_SEARCHOBJECT_SELECTBOX, FIELD_SEARCHOBJECT_SELECTBOX, list) ;
		searchType.setOnChange("Onchange") ;
		UIFormStringInput categoryName = new UIFormStringInput(FIELD_CATEGORY_NAME, FIELD_CATEGORY_NAME, null) ;
		UIFormStringInput descriptions = new UIFormStringInput(FIELD_CATEGORY_DESCRIPTIONS, FIELD_CATEGORY_DESCRIPTIONS, null) ;
		
		UIFormDateTimeInput fromDate = new UIFormDateTimeInput(FROM_DATE, FROM_DATE, null, false) ;
		UIFormDateTimeInput toDate = new UIFormDateTimeInput(TO_DATE, TO_DATE, null, false) ;
		// search question
		UIFormStringInput author = new UIFormStringInput(AUTHOR, AUTHOR, null) ;
		author.setRendered(false) ;
		UIFormStringInput emailAdress = new UIFormStringInput(EMAIL_ADDRESS, EMAIL_ADDRESS, null) ;
		emailAdress.setRendered(false) ;
		list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>("English", "english")) ;
		list.add(new SelectItemOption<String>("Dutch", "dutch")) ;
		list.add(new SelectItemOption<String>("French", "french")) ;
		list.add(new SelectItemOption<String>("German", "german")) ;
		UIFormSelectBox language = new UIFormSelectBox(LANGUAGE, LANGUAGE, list) ;
		language.setRendered(false) ;
		UIFormStringInput question = new UIFormStringInput(QUESTION, QUESTION, null) ;
		question.setRendered(false) ;
		
		addUIFormInput(searchType) ;
		addUIFormInput(categoryName) ;
		addUIFormInput(descriptions) ;
		addUIFormInput(fromDate) ;
		addUIFormInput(toDate) ;
		
		addUIFormInput(author) ;
		addUIFormInput(emailAdress) ;
		addUIFormInput(language) ;
		addUIFormInput(question) ;
		
	}
	
	public void setSelectType(String type) {
	  this.getUIFormSelectBox(FIELD_SEARCHOBJECT_SELECTBOX).setValue(type) ;
  }
	
	public void activate() throws Exception {
	  // TODO Auto-generated method stub
	  
  }
	public void deActivate() throws Exception {
	  // TODO Auto-generated method stub
	  
  }
	
	public void setValue(boolean isCategoryName, boolean isDescriptions, boolean isFormDate, boolean isToDate,
			boolean isAuthor, boolean isEmailAddress, boolean isLanguage, boolean isQuestion) {
		UIFormStringInput categoryName = getUIStringInput(FIELD_CATEGORY_NAME).setRendered(isCategoryName) ;
		UIFormStringInput descriptions = getUIStringInput(FIELD_CATEGORY_DESCRIPTIONS).setRendered(isDescriptions) ;
	
		UIFormStringInput author = getUIStringInput(AUTHOR).setRendered(isAuthor) ;
		UIFormStringInput emailAddress = getUIStringInput(EMAIL_ADDRESS).setRendered(isEmailAddress) ;
		UIFormSelectBox language = getUIFormSelectBox(LANGUAGE).setRendered(isLanguage) ;
		UIFormStringInput question = getUIStringInput(QUESTION).setRendered(isQuestion) ;
		getUIStringInput(FIELD_CATEGORY_NAME).setValue("") ;
		getUIStringInput(FIELD_CATEGORY_DESCRIPTIONS).setValue("") ;

		categoryName.setValue("") ;
		descriptions.setValue("") ;
	
		author.setValue("") ;
		emailAddress.setValue("") ;
		language.setValue("English") ;
		question.setValue("") ;
	}
	public String getLabel(ResourceBundle res, String id) throws Exception {
    String label = getId() + ".label." + id;    
    try {
    	return res.getString(label);
    } catch (Exception e) {
			return id ;
		}
  }
  
  public String[] getActions() {
    return new String[]{"Search", "Cancel"} ;
  }
	
	static public class OnchangeActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
			UIAdvancedSearchForm uiAdvancedSearchForm = event.getSource() ;			
			String type = uiAdvancedSearchForm.getUIFormSelectBox(FIELD_SEARCHOBJECT_SELECTBOX).getValue() ;
			if(type.equals("category")) {
				uiAdvancedSearchForm.setValue(true, true, true, true, false, false, false, false) ;
			} else {
				uiAdvancedSearchForm.setValue(false, false, true, true, true, true, true, true) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiAdvancedSearchForm) ;
		}
	}
	
	static public class SearchActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
			UIAdvancedSearchForm uiAdvancedSearch = event.getSource() ;			
			String type = uiAdvancedSearch.getUIFormSelectBox(FIELD_SEARCHOBJECT_SELECTBOX).getValue() ;
			System.out.println("====>>>>:::" + type );
			String categoryName = uiAdvancedSearch.getUIStringInput(FIELD_CATEGORY_NAME).getValue() ;
			String descriptions = uiAdvancedSearch.getUIStringInput(FIELD_CATEGORY_DESCRIPTIONS).getValue() ;
			Calendar fromDate = uiAdvancedSearch.getUIFormDateTimeInput(FROM_DATE).getCalendar() ;
			Calendar toDate= uiAdvancedSearch.getUIFormDateTimeInput(TO_DATE).getCalendar() ;
			
			String author = uiAdvancedSearch.getUIStringInput(AUTHOR).getValue() ;
			String emailAddress = uiAdvancedSearch.getUIStringInput(EMAIL_ADDRESS).getValue() ;
			String language = uiAdvancedSearch.getUIFormSelectBox(LANGUAGE).getValue() ;
			String question = uiAdvancedSearch.getUIStringInput(QUESTION).getValue() ;
			FAQEventQuery eventQuery = new FAQEventQuery() ;
			eventQuery.setType(type) ;
			eventQuery.setName(categoryName) ;
			eventQuery.setDescription(descriptions) ;
			eventQuery.setFromDate(fromDate) ;
			eventQuery.setToDate(toDate) ;
			eventQuery.setAuthor(author) ;
			eventQuery.setLanguage(language) ;
			eventQuery.setEmail(emailAddress) ;
			eventQuery.setQuestion(question) ;
			if(!type.equals("faqQuestion")) {
				UIFAQPortlet uiPortlet = uiAdvancedSearch.getAncestorOfType(UIFAQPortlet.class);
				UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
				UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;	
				ResultSearchCategory result = popupAction.activate(ResultSearchCategory.class, 600) ;
				FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
				List<Category> list = faqService.getAdvancedSeach(FAQUtils.getSystemProvider(),eventQuery);
				popupContainer.setId("ResultSearchCategory") ;
				result.setListCategory(list) ;
				result.init() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			}
		}
	}
	
	static public class CancelActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
			UIAdvancedSearchForm uiCategory = event.getSource() ;			
			UIPopupAction uiPopupAction = uiCategory.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
		}
	}

	
	
}