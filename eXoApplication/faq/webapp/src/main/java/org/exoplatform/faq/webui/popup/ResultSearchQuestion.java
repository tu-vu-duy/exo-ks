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

import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionPageList;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIAnswersContainer;
import org.exoplatform.faq.webui.UIAnswersPageIterator;
import org.exoplatform.faq.webui.UIAnswersPortlet;
import org.exoplatform.faq.webui.UIBreadcumbs;
import org.exoplatform.faq.webui.UICategories;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.faq.webui.UIResultContainer;
import org.exoplatform.ks.common.webui.BaseUIForm;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Truong Nguyen
 *					truong.nguyen@exoplatform.com
 * May 6, 2008, 1:55:48 PM
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/faq/webui/popup/ResultSearchQuestion.gtmpl",
		events = {
			@EventConfig(listeners = ResultSearchQuestion.ViewActionListener.class),
			@EventConfig(listeners = ResultSearchQuestion.LinkActionListener.class),
			@EventConfig(listeners = ResultSearchQuestion.CloseActionListener.class)
		}
)
public class ResultSearchQuestion extends BaseUIForm implements UIPopupComponent{
	private List<Question> listQuestion_ = null ;
	private String language_ = "" ;
	public ResultSearchQuestion() throws Exception {
		addChild(UIAnswersPageIterator.class, null, LIST_RESULT_SEARCH) ;
	}

	private String LIST_RESULT_SEARCH = "listResultQuestionsSearch";
	private UIAnswersPageIterator pageIterator ;
	private JCRPageList pageList ;

	@SuppressWarnings("unused")
	private List<Question> getListQuestion() throws Exception {
		long pageSelected = pageIterator.getPageSelected();
		listQuestion_ = new ArrayList<Question>() ;
		try {
			listQuestion_.addAll(pageList.getPageResultQuestionsSearch(pageSelected, FAQUtils.getCurrentUser()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return listQuestion_ ;
	}

	public void setListQuestion(List<Question> listQuestion) {
		this.listQuestion_ = listQuestion ;
		try {
			pageList = new QuestionPageList(listQuestion, listQuestion.size());
			pageList.setPageSize(5);
			pageIterator = this.getChildById(LIST_RESULT_SEARCH);
			pageIterator.updatePageList(pageList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private long getTotalpages(String pageInteratorId) {
		UIAnswersPageIterator pageIterator = this.getChildById(LIST_RESULT_SEARCH) ;
		try {
			return pageIterator.getInfoPage().get(3) ;
		} catch (Exception e) {
			e.printStackTrace();
			return 1 ;
		}
	}

	public void setLanguage(String language) {this.language_ = language ;}
	public String getLanguage() { return language_ ;}

	public void activate() throws Exception {}
	public void deActivate() throws Exception {}

	static	public class ViewActionListener extends EventListener<ResultSearchQuestion> {
		public void execute(Event<ResultSearchQuestion> event) throws Exception {
			ResultSearchQuestion resultSearch = event.getSource() ;
			FAQService faqService = FAQUtils.getFAQService() ;
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			//SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try {
				faqService.isExisting(questionId) ;
			} catch (Exception e) {
				resultSearch.warning("UIQuestions.msg.question-id-deleted") ;
				return ;
			}
			UIResultContainer uiResultContainer = resultSearch.getParent() ;
			UIPopupAction popupAction = uiResultContainer.getChild(UIPopupAction.class) ;
			UIPopupViewQuestion viewQuestion = popupAction.activate(UIPopupViewQuestion.class, 700) ;
			viewQuestion.setQuestion(questionId) ;
			viewQuestion.setLanguage(resultSearch.getLanguage()) ;
			viewQuestion.setId("UIPopupViewQuestion") ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static	public class LinkActionListener extends EventListener<ResultSearchQuestion> {
		public void execute(Event<ResultSearchQuestion> event) throws Exception {
			ResultSearchQuestion resultSearch = event.getSource() ;
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			FAQService faqService = FAQUtils.getFAQService() ;
			try {
				Question question = faqService.getQuestionById(questionId) ;
				String categoryId = faqService.getCategoryPathOf(questionId) ;
				UIAnswersPortlet answerPortlet = resultSearch.getAncestorOfType(UIAnswersPortlet.class) ;
				UIQuestions uiQuestions = answerPortlet.findFirstComponentOfType(UIQuestions.class) ;
				//uiQuestions.pageList.setObjectId(questionId);
				uiQuestions.setCategoryId(categoryId) ;
				uiQuestions.updateCurrentQuestionList();
				//uiQuestions.viewingQuestionId_ = questionId ;
				uiQuestions.viewQuestion(question) ;
				//int pos = 0 ;
				/*for(Question question2 : uiQuestions.listQuestion_) {
					if(question2.getId().equals(questionId)) {
						pos = uiQuestions.listQuestion_.indexOf(question2) ;
						break ;
					}
				}*/
				/*uiQuestions.listQuestionLanguage.clear() ;
				uiQuestions.listLanguage.clear() ;
				QuestionLanguage questionLanguage = new QuestionLanguage() ;
				questionLanguage.setId(question.getId());
				questionLanguage.setQuestion(question.getQuestion()) ;
//				questionLanguage.setAnswers(question.getAnswers()) ;
				questionLanguage.setLanguage(question.getLanguage()) ;
				uiQuestions.listQuestionLanguage.add(questionLanguage) ;
				uiQuestions.listQuestionLanguage.addAll(faqService.getQuestionLanguages(questionId)) ;
				for(QuestionLanguage language : uiQuestions.listQuestionLanguage) {
					uiQuestions.listLanguage.add(language.getLanguage()) ;
					if(language.getLanguage().equals(language_)) {
						uiQuestions.listQuestion_.get(pos).setQuestion(language.getQuestion()) ;
						uiQuestions.listQuestion_.get(pos).setLanguage(language.getLanguage()) ;
//						uiQuestions.listQuestion_.get(pos).setAnswers(language.getAnswers()) ;
					}
				}*/
				//uiQuestions.isChangeLanguage = true ;
				uiQuestions.setLanguageView(FAQUtils.getDefaultLanguage());
				event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions) ;
				UIBreadcumbs breadcumbs = answerPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;
				//breadcumbs.setUpdataPath(null) ;
				/*String oldPath = "" ;
				if(!categoryId.equals("null")){
					List<String> listPath = faqService.getCategoryPath(sessionProvider, categoryId) ;
					for(int i = listPath.size() -1 ; i >= 0; i --) {
						oldPath = oldPath + "/" + listPath.get(i);
					}
				}*/
				//String newPath = "FAQService" + "/" + categoryId ;
				uiQuestions.setPath(categoryId) ;
				breadcumbs.setUpdataPath(categoryId);
				UICategories categories = answerPortlet.findFirstComponentOfType(UICategories.class);
				categories.setPathCategory(categoryId);
				event.getRequestContext().addUIComponentToUpdateByAjax(answerPortlet.getChild(UIAnswersContainer.class));
				answerPortlet.cancelAction() ;
			} catch (Exception e) {
				e.printStackTrace();
				resultSearch.warning("UIQuestions.msg.question-id-deleted") ;
			}
		}
	}

	static	public class CloseActionListener extends EventListener<ResultSearchQuestion> {
		public void execute(Event<ResultSearchQuestion> event) throws Exception {
			ResultSearchQuestion resultSearchQuestion = event.getSource() ;
			UIAnswersPortlet portlet = resultSearchQuestion.getAncestorOfType(UIAnswersPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}
