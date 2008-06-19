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
import java.util.Locale;

import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
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
		template =	"app:/templates/faq/webui/popup/UILanguageForm.gtmpl",
		events = {
		  @EventConfig(listeners = UILanguageForm.SelectedLanguageActionListener.class),
			@EventConfig(listeners = UILanguageForm.SaveActionListener.class),
			@EventConfig(listeners = UILanguageForm.CancelActionListener.class)
		}
)
public class UILanguageForm extends UIForm implements UIPopupComponent	{
  private List<String> LIST_LANGUAGE = new ArrayList<String>() ;
  private List<String> listLocaldName = new ArrayList<String>() ;
  private List<String> LANGAUGE_SELECT = new ArrayList<String>() ;
  
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
	 
	public UILanguageForm() throws Exception {
    List<String> listLanguage = new ArrayList<String>() ;
    
    LocaleConfigService configService = getApplicationComponent(LocaleConfigService.class) ;
    for(Object object:configService.getLocalConfigs()) {      
      LocaleConfig localeConfig = (LocaleConfig)object ;
      Locale locale = localeConfig.getLocale() ;
      String displayName = locale.getDisplayLanguage() ;
      String localedName = locale.getDisplayName(locale) ;   
      listLanguage.add(displayName) ;
      listLocaldName.add(localedName) ;
    }
    //LIST_LANGUAGE.addAll(Arrays.asList(new String[]{"English", "France", "Vietnamese", "Ukrainnian"})) ;\
    LIST_LANGUAGE.addAll(listLanguage) ;
	}
  
  public void setListSelected(List<String> language) {
    this.LANGAUGE_SELECT.clear();
    this.LANGAUGE_SELECT.addAll(language);
  }
  
  @SuppressWarnings("unused")
  private List<String> getListLanguage(){
    return this.LIST_LANGUAGE ;
  }
  
  @SuppressWarnings("unused")
  private String[] getLocaledLanguage(){
    return this.listLocaldName.toArray(new String[]{}) ;
  }
  
  @SuppressWarnings("unused")
  private List<String> getListSelected(){
    return this.LANGAUGE_SELECT ;
  }
	
	static public class SelectedLanguageActionListener extends EventListener<UILanguageForm> {
	  public void execute(Event<UILanguageForm> event) throws Exception {
      UILanguageForm languageForm = event.getSource() ;
      String languageIsSelect = event.getRequestContext().getRequestParameter(OBJECTID) ;
      if(languageForm.LANGAUGE_SELECT.contains(languageIsSelect)){
        if(languageForm.LANGAUGE_SELECT.indexOf(languageIsSelect) > 0) {
          languageForm.LANGAUGE_SELECT.remove(languageIsSelect) ;
        } else {
          UIApplication uiApplication = languageForm.getAncestorOfType(UIApplication.class) ;
          uiApplication.addMessage(new ApplicationMessage("UILanguageForm.msg.language-is-default", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
          return ;
        }
      } else if(!languageForm.LANGAUGE_SELECT.contains(languageIsSelect)){
        languageForm.LANGAUGE_SELECT.add(languageIsSelect) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(languageForm) ;
	  }
	}
  
	static public class SaveActionListener extends EventListener<UILanguageForm> {
    @SuppressWarnings("static-access")
    public void execute(Event<UILanguageForm> event) throws Exception {
			UILanguageForm languageForm = event.getSource() ;
			UIPopupContainer popupContainer = languageForm.getAncestorOfType(UIPopupContainer.class) ;
      UIQuestionForm questionForm = popupContainer.getChild(UIQuestionForm.class) ;
      if(questionForm == null ) {
        UIFAQPortlet portlet = languageForm.getAncestorOfType(UIFAQPortlet.class) ;
        UIQuestionManagerForm questionManagerForm = portlet.findFirstComponentOfType(UIQuestionManagerForm.class) ;
        questionForm = questionManagerForm.getChildById(questionManagerForm.UI_QUESTION_FORM) ;
      }
      questionForm.setListLanguage(languageForm.LANGAUGE_SELECT) ;
      questionForm.initPage(true) ;
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
		}
	}

	static public class CancelActionListener extends EventListener<UILanguageForm> {
    public void execute(Event<UILanguageForm> event) throws Exception {
			UILanguageForm uiCategory = event.getSource() ;			
			UIPopupContainer popupContainer = uiCategory.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}