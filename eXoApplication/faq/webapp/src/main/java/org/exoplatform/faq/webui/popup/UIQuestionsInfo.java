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
import java.util.ResourceBundle;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Cate;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQServiceUtils;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIAnswersPageIterator;
import org.exoplatform.faq.webui.UIAnswersPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SAS
 * Author : Mai Van Ha
 *          ha_mai_van@exoplatform.com
 * May 15, 2008 ,4:09:44 AM 
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class ,
    template =  "app:/templates/faq/webui/popup/UIQuestionsInfo.gtmpl",
    events = {
    	@EventConfig(listeners = UIQuestionsInfo.ChangeCategoryActionListener.class),
      @EventConfig(listeners = UIQuestionsInfo.CloseActionListener.class),
      @EventConfig(listeners = UIQuestionsInfo.EditQuestionActionListener.class),
      @EventConfig(listeners = UIQuestionsInfo.DeleteQuestionActionListener.class),
      @EventConfig(listeners = UIQuestionsInfo.ChangeTabActionListener.class),
      @EventConfig(listeners = UIQuestionsInfo.ChangeQuestionStatusActionListener.class),
      @EventConfig(listeners = UIQuestionsInfo.ResponseQuestionActionListener.class)
    }
)

@SuppressWarnings("unused")
public class UIQuestionsInfo extends UIForm implements UIPopupComponent {
  private static final String LIST_QUESTION_INTERATOR = "FAQUserPageIteratorTab1" ;
  private static final String LIST_QUESTION_NOT_ANSWERED_INTERATOR = "FAQUserPageIteratorTab2" ;
  private static final String LIST_CATEGORIES = "ListCategories";
  private FAQSetting faqSetting_ = new FAQSetting();
  private static FAQService faqService_ =(FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ; 
  private JCRPageList pageList ;
  private JCRPageList pageListNotAnswer ;
  private UIAnswersPageIterator pageIterator ;
  private UIAnswersPageIterator pageQuesNotAnswerIterator ;
  private List<Question> listQuestion_ = new ArrayList<Question>() ;
  private List<Question> listQuestionNotYetAnswered_ = new ArrayList<Question>() ;
  private List<SelectItemOption<String>> listCategories = new ArrayList<SelectItemOption<String>>() ;
  private long pageSelect = 1 ;
  private long pageSelectNotAnswer = 1 ;
  private List<String> moderateCates  = new ArrayList<String>() ;
  private boolean isEditTab_ = true ;
  private boolean isResponseTab_ = false ;
  private boolean isChangeTab_ = false;
  private String cateId_ = Utils.ALL;
  
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  public UIQuestionsInfo() throws Exception {
    isEditTab_ = true ;
    isResponseTab_ = false ;
    addChild(UIAnswersPageIterator.class, null, LIST_QUESTION_INTERATOR) ;
    addChild(UIAnswersPageIterator.class, null, LIST_QUESTION_NOT_ANSWERED_INTERATOR) ;
    setActions(new String[]{""}) ;
  }
  
  public void setFAQSetting(FAQSetting setting) throws Exception{
	  this.faqSetting_ = setting;
	  FAQUtils.getEmailSetting(faqSetting_, false, false);
	  setListCate();
    UIFormSelectBox selectCategory = new UIFormSelectBox(LIST_CATEGORIES, LIST_CATEGORIES, listCategories);
    selectCategory.setOnChange("ChangeCategory");
    this.addUIFormInput(selectCategory);
    setListQuestion() ;
  }
  
  private boolean hasInGroup(List<String> listGroup, String[] listPermission){
  	for(String per : listPermission){
  		if(per!= null && per.trim().length() > 0 && listGroup.contains(per)) return true;
  	}
  	return false;
  }
  
	private void setListCate() throws Exception {
  	WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    ResourceBundle res = context.getApplicationResourceBundle() ;
  	this.listCategories.add(new SelectItemOption<String>(res.getString("UIQuestionsInfo.label.All"), Utils.ALL)) ;
  	FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
  	if(faqSetting_.isAdmin()) {
  		List<Cate> listCate = faqService.listingCategoryTree() ;
  		this.listCategories.add(new SelectItemOption<String>(faqService_.getCategoryNameOf(Utils.CATEGORY_HOME), Utils.CATEGORY_HOME)) ;
      for(Cate cat : listCate) {
      	this.listCategories.add(new SelectItemOption<String>(cat.getCategory().getName(), cat.getCategory().getPath())) ;
      }
  	}else {
  		List<String> listCate = faqService_.getListCateIdByModerator(FAQUtils.getCurrentUser()) ;  		
  		moderateCates.clear() ;
  		for(String str : listCate) {
  			try{
  				this.listCategories.add(new SelectItemOption<String>(str.substring(40), str.substring(0, 40))) ;
    			moderateCates.add(str.substring(0,40)) ;
  			}catch(StringIndexOutOfBoundsException e) {
  				if(str.indexOf(Utils.CATEGORY_HOME) == 0) {
  					this.listCategories.add(new SelectItemOption<String>(str.substring(Utils.CATEGORY_HOME.length()), Utils.CATEGORY_HOME)) ;
      			moderateCates.add(Utils.CATEGORY_HOME) ;
  				}
  			}catch(Exception e) {
  				e.printStackTrace() ;
  			}
  			
  		}
  	}    
  }
  
  private String[] getQuestionActions(){
    return new String[]{"AddLanguage", "Attachment", "Save", "Close"} ;
  }
  
  private String[] getQuestionNotAnsweredActions() {
    return new String[]{"QuestionRelation", "Attachment", "Save", "Close"} ;
  }
  
  private String[] getTab() {
    return new String[]{"Question managerment", "Question not yet answered"} ;
  }
  
  private boolean getIsEdit(){
    return isEditTab_;
  }
  
  private boolean getIsResponse() {
    return isResponseTab_ ;
  }
  
  private long getTotalpages(String pageInteratorId) {
    UIAnswersPageIterator pageIterator = this.getChildById(pageInteratorId) ;
    try {
      return pageIterator.getInfoPage().get(3) ;
    } catch (Exception e) {
      e.printStackTrace();
      return 1 ;
    }
  }
  
  public void setListQuestion() throws Exception {
    listQuestion_.clear() ;
    listQuestionNotYetAnswered_.clear() ;
    String user = FAQUtils.getCurrentUser() ;
    pageIterator = this.getChildById(LIST_QUESTION_INTERATOR) ;
    pageQuesNotAnswerIterator = this.getChildById(LIST_QUESTION_NOT_ANSWERED_INTERATOR) ;
    List<String>userPrivates = FAQServiceUtils.getAllGroupAndMembershipOfUser(FAQUtils.getCurrentUser());
    if(faqSetting_.isAdmin()) {
    	if(this.cateId_.equals(Utils.ALL)){
    		this.pageList = faqService_.getAllQuestions() ;
    		pageListNotAnswer = faqService_.getQuestionsNotYetAnswer(Utils.ALL, false) ;
    	} else {
    		String cateId = cateId_;
    		if(cateId.indexOf("/") > 0) cateId = cateId.substring(cateId.lastIndexOf("/")+1);
    		this.pageList = faqService_.getAllQuestionsByCatetory(cateId, this.faqSetting_);
    		pageListNotAnswer = faqService_.getQuestionsNotYetAnswer(cateId, false) ;
    	}
      this.pageList.setPageSize(5);
      pageIterator.updatePageList(this.pageList) ;
      
      pageListNotAnswer.setPageSize(5);
      pageQuesNotAnswerIterator.updatePageList(pageListNotAnswer) ;
    } else {
    	List<String> listCateId = new ArrayList<String>() ;
      if(cateId_.equals(Utils.ALL)){
      	listCateId.addAll(moderateCates) ;      	
      } else {
      	if(cateId_.indexOf("/") > 0) listCateId.add(cateId_.substring(cateId_.lastIndexOf("/") + 1));
      	else listCateId.add(cateId_);
      }
      
      if(listCateId.size() > 0) {
        this.pageList = faqService_.getQuestionsByListCatetory(listCateId, false) ;
        this.pageList.setPageSize(5);
        pageIterator.updatePageList(this.pageList) ;
        
        this.pageListNotAnswer = faqService_.getQuestionsByListCatetory(listCateId, true) ;
        this.pageListNotAnswer.setPageSize(5);
        pageQuesNotAnswerIterator.updatePageList(this.pageListNotAnswer) ;
      }
    }
  }
  
	private String getCategoryPath(String questionPath){
  	try{
  		return faqService_.getParentCategoriesName(questionPath.substring(0, questionPath.indexOf("/"+Utils.QUESTION_HOME)));
  	}catch(Exception e){
  		e.printStackTrace();
  		return questionPath;
  	}
  }
  
  private List<Question> getListQuestion() {
    if(!isChangeTab_){
      pageSelect = pageIterator.getPageSelected() ;
      listQuestion_ = new ArrayList<Question>();
      try {
        listQuestion_.addAll(this.pageList.getPage(pageSelect, null)) ;
        if(listQuestion_.isEmpty()){
	        UIAnswersPageIterator pageIterator = null ;
	        while(listQuestion_.isEmpty() && pageSelect > 1) {
	          pageIterator = this.getChildById(LIST_QUESTION_INTERATOR) ;
	          listQuestion_.addAll(this.pageList.getPage(--pageSelect, null)) ;
	          pageIterator.setSelectPage(pageSelect) ;
	        }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    isChangeTab_ = false;
    return listQuestion_ ;
  }
  
  /**
   * Gets the list question not answered.
   * 
   * @return the list question not answered
   */
  private List<Question> getListQuestionNotAnswered() {
    if(!isChangeTab_){
      pageSelectNotAnswer = pageQuesNotAnswerIterator.getPageSelected() ;
      listQuestionNotYetAnswered_.clear() ;
      try {
        listQuestionNotYetAnswered_.addAll(this.pageListNotAnswer.getPage(pageSelectNotAnswer, null)) ;
        UIAnswersPageIterator pageIterator = null ;
        while(listQuestionNotYetAnswered_.isEmpty() && pageSelectNotAnswer > 1) {
          pageIterator = this.getChildById(LIST_QUESTION_NOT_ANSWERED_INTERATOR) ;
          listQuestionNotYetAnswered_.addAll(this.pageListNotAnswer.getPage(--pageSelectNotAnswer, null)) ;
          pageIterator.setSelectPage(pageSelectNotAnswer) ;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    isChangeTab_ = false;
    return listQuestionNotYetAnswered_ ;
  }
  
  static public class EditQuestionActionListener extends EventListener<UIQuestionsInfo> {
    @SuppressWarnings("static-access")
    public void execute(Event<UIQuestionsInfo> event) throws Exception {
      UIQuestionsInfo questionsInfo = event.getSource() ;
      String quesId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIQuestionManagerForm questionManagerForm = questionsInfo.getAncestorOfType(UIQuestionManagerForm.class) ;
      try{
        Question question = faqService_.getQuestionById(quesId) ;
        UIQuestionForm questionForm = questionManagerForm.getChildById(questionManagerForm.UI_QUESTION_FORM) ;
        questionForm.setFAQSetting(questionsInfo.faqSetting_);
        questionForm.setIsChildOfManager(true) ;
        questionForm.setQuestion(question) ;
        questionManagerForm.isViewEditQuestion = true ;
        questionManagerForm.isViewResponseQuestion = false ;
        questionManagerForm.isEditQuestion = true ;
      } catch(Exception e) {
        UIApplication uiApplication = questionsInfo.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        for(int i = 0; i < questionsInfo.listQuestion_.size() ; i ++) {
          if(questionsInfo.listQuestion_.get(i).getId().equals(quesId)) {
            questionsInfo.listQuestion_.remove(i) ;
            break ;
          }
        }
      }
      UIPopupContainer popupContainer = questionManagerForm.getAncestorOfType(UIPopupContainer.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
    }
  }
  
  static public class ResponseQuestionActionListener extends EventListener<UIQuestionsInfo> {
    @SuppressWarnings("static-access")
    public void execute(Event<UIQuestionsInfo> event) throws Exception {
      UIQuestionsInfo questionsInfo = event.getSource() ;
      String ids = event.getRequestContext().getRequestParameter(OBJECTID);
      String questionId = ids.substring(0, ids.lastIndexOf("/")) ;
      String language = ids.substring(ids.lastIndexOf("/") + 1) ;
      UIQuestionManagerForm questionManagerForm = questionsInfo.getAncestorOfType(UIQuestionManagerForm.class) ;
      try{
        Question question = faqService_.getQuestionById(questionId) ;
        boolean isModerateAnswer = faqService_.isModerateAnswer(question.getPath());
        UIResponseForm responseForm = questionManagerForm.getChildById(questionManagerForm.UI_RESPONSE_FORM) ;
        responseForm.setFAQSetting(questionsInfo.faqSetting_);
        responseForm.updateChildOfQuestionManager(true) ;        
        /*if(param.length == 1) responseForm.setQuestionId(question, null, isModerateAnswer) ;
        else*/ 
        responseForm.setQuestionId(question, language, isModerateAnswer) ;
        questionManagerForm.isViewEditQuestion = false ;
        questionManagerForm.isViewResponseQuestion = true ;
        questionManagerForm.isResponseQuestion = true ;
      } catch(Exception e) {
        UIApplication uiApplication = questionsInfo.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        for(int i = 0; i < questionsInfo.listQuestion_.size() ; i ++) {
          if(questionsInfo.listQuestion_.get(i).getPath().equals(questionId)) {
            questionsInfo.listQuestion_.remove(i) ;
            break ;
          }
        }
      }
      UIPopupContainer popupContainer = questionManagerForm.getAncestorOfType(UIPopupContainer.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
    }
  }
  
  static public class DeleteQuestionActionListener extends EventListener<UIQuestionsInfo> {
    public void execute(Event<UIQuestionsInfo> event) throws Exception {
      UIQuestionsInfo questionsInfo = event.getSource() ;
      String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIPopupContainer popupContainer = questionsInfo.getAncestorOfType(UIPopupContainer.class);
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
      try {
        Question question = faqService_.getQuestionById(questionId) ;
        UIDeleteQuestion deleteQuestion = popupAction.activate(UIDeleteQuestion.class, 500) ;
        deleteQuestion.setQuestionId(question) ;
        deleteQuestion.setIsManagement(true) ;
        UIQuestionManagerForm questionManagerForm = questionsInfo.getParent() ;
        if(questionManagerForm.isEditQuestion) {
          UIQuestionForm questionForm = questionManagerForm.getChild(UIQuestionForm.class) ;
          questionForm.setIsMode(true);
          if(questionForm.getQuestionId().equals(questionId)) {
            questionManagerForm.isEditQuestion = false ;
          }
        }
        if(questionManagerForm.isResponseQuestion) {
          UIResponseForm responseForm = questionManagerForm.getChild(UIResponseForm.class) ;
          if(responseForm.questionId_.equals(questionId)) {
            questionManagerForm.isResponseQuestion = false ;
          }
        }
        //event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      } catch(Exception e) {
        UIApplication uiApplication = questionsInfo.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        for(int i = 0; i < questionsInfo.listQuestion_.size() ; i ++) {
          if(questionsInfo.listQuestion_.get(i).getId().equals(questionId)) {
            questionsInfo.listQuestion_.remove(i) ;
            break ;
          }
        }
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
    }
  }
  
  static public class ChangeCategoryActionListener extends EventListener<UIQuestionsInfo> {
		public void execute(Event<UIQuestionsInfo> event) throws Exception {
  		UIQuestionsInfo questionsInfo = event.getSource() ;
      String cateId = ((UIFormSelectBox)questionsInfo.getChildById(LIST_CATEGORIES)).getValue();
      questionsInfo.cateId_ = cateId;
      questionsInfo.setListQuestion();
      UIQuestionManagerForm questionManagerForm = questionsInfo.getAncestorOfType(UIQuestionManagerForm.class) ;
      questionManagerForm.isResponseQuestion = false ;
      questionManagerForm.isEditQuestion = false ;
      UIPopupContainer popupContainer = questionsInfo.getAncestorOfType(UIPopupContainer.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
  	}
  }
  
  static public class CloseActionListener extends EventListener<UIQuestionsInfo> {
    public void execute(Event<UIQuestionsInfo> event) throws Exception {
    	UIQuestionsInfo questionManagerForm = event.getSource() ;
  		UIAnswersPortlet portlet = questionManagerForm.getAncestorOfType(UIAnswersPortlet.class) ;
  		UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
  		event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class ChangeTabActionListener extends EventListener<UIQuestionsInfo> {
    public void execute(Event<UIQuestionsInfo> event) throws Exception {
      UIQuestionsInfo questionsInfo = event.getSource() ;
      String idTab = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIQuestionManagerForm questionManagerForm = questionsInfo.getAncestorOfType(UIQuestionManagerForm.class) ;
      if(idTab.equals("0")) {
        questionsInfo.isEditTab_ = true ;
        questionsInfo.isResponseTab_ = false ;
        
        questionManagerForm.isViewEditQuestion = true ;
        questionManagerForm.isViewResponseQuestion = false ;
      } else {
        questionsInfo.isEditTab_ = false;
        questionsInfo.isResponseTab_ = true ;
        
        questionManagerForm.isViewEditQuestion = false ;
        questionManagerForm.isViewResponseQuestion = true ;
      }
      questionsInfo.isChangeTab_ = true;
      UIPopupContainer popupContainer = questionManagerForm.getAncestorOfType(UIPopupContainer.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
    }
  }
  
  static public class ChangeQuestionStatusActionListener extends EventListener<UIQuestionsInfo> {
  	public void execute(Event<UIQuestionsInfo> event) throws Exception {
  		UIQuestionsInfo questionsInfo = event.getSource() ;
  		String ids = event.getRequestContext().getRequestParameter(OBJECTID) ;
  		String action = ids.substring(0, ids.indexOf("/")) ;
  		String questionId = ids.substring(ids.indexOf("/") + 1) ;
  		try{
  			Question question = faqService_.getQuestionById(questionId);
  			if(action.equals("approved")){
  				question.setApproved(!question.isApproved());
  			} else {
  				question.setActivated(!question.isActivated());
  			}
  			FAQUtils.getEmailSetting(questionsInfo.faqSetting_, false, false);
  			faqService_.saveQuestion(question, false,questionsInfo.faqSetting_);
  			UIAnswersPortlet portlet = questionsInfo.getAncestorOfType(UIAnswersPortlet.class) ;
  			UIQuestions questions = portlet.findFirstComponentOfType(UIQuestions.class) ;
  			questions.setDefaultLanguage();
        questions.updateCurrentQuestionList() ;
      	if(question.getPath().equals(questions.viewingQuestionId_)){
      		questions.updateLanguageMap() ;
      	}
      	event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
  		}catch (Exception e){
  			e.printStackTrace() ;
  			UIApplication uiApplication = questionsInfo.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
  		}
      event.getRequestContext().addUIComponentToUpdateByAjax(questionsInfo.getAncestorOfType(UIPopupContainer.class)) ;
  	}
  }  
}