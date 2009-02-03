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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.service.impl.MultiLanguages;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQContainer;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.faq.webui.ValidatorDataInput;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormWYSIWYGInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;

/**
 * Created by The eXo Platform SAS
 * Author : Mai Van Ha
 *          ha_mai_van@exoplatform.com
 * Apr 17, 2008 ,3:19:00 PM 
 */

@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =  "app:/templates/faq/webui/popup/UIResponseForm.gtmpl",
		events = {
			@EventConfig(listeners = UIResponseForm.AddNewAnswerActionListener.class),
			@EventConfig(listeners = UIResponseForm.SaveActionListener.class),
			@EventConfig(listeners = UIResponseForm.CancelActionListener.class),
			@EventConfig(listeners = UIResponseForm.AddRelationActionListener.class),
			@EventConfig(listeners = UIResponseForm.AttachmentActionListener.class),
			@EventConfig(listeners = UIResponseForm.RemoveAttachmentActionListener.class),
			@EventConfig(listeners = UIResponseForm.RemoveRelationActionListener.class),
			@EventConfig(listeners = UIResponseForm.ViewEditQuestionActionListener.class),
			@EventConfig(listeners = UIResponseForm.ChangeQuestionActionListener.class)
		}
)

public class UIResponseForm extends UIForm implements UIPopupComponent {
	private static final String QUESTION_CONTENT = "QuestionTitle" ;
	private static final String QUESTION_DETAIL = "QuestionContent" ;
	private static final String QUESTION_LANGUAGE = "Language" ;
	private static final String RESPONSE_CONTENT = "QuestionRespone" ;
	private static final String ATTATCH_MENTS = "QuestionAttach" ;
	private static final String REMOVE_FILE_ATTACH = "RemoveFile" ;
	private static final String FILE_ATTACHMENTS = "FileAttach" ;
	private static final String SHOW_ANSWER = "QuestionShowAnswer" ;
	private static final String IS_APPROVED = "IsApproved" ;
	private static Question question_ = null ;
	private static FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;

	@SuppressWarnings("unused")
	private boolean isViewEditQuestion_ = false;
	@SuppressWarnings("unused")
	private String questionDetail = new String();
	private String questionContent = new String();

	// form input :
	private UIFormStringInput inputQuestionContent_ ;
	private UIFormTextAreaInput inputQuestionDetail_ ;
	private UIFormSelectBox questionLanguages_ ;
	private UIFormWYSIWYGInput inputResponseQuestion_ ; 
	private UIFormInputWithActions inputAttachment_ ; 
	@SuppressWarnings("unchecked")
	private UIFormCheckBoxInput checkShowAnswer_ ;
	private UIFormCheckBoxInput<Boolean> isApproved_ ;

	// question infor :
	private String questionId_ = new String() ;
	private List<String> listRelationQuestion =  new ArrayList<String>() ;
	private List<String> listQuestIdRela = new ArrayList<String>() ;
	private List<FileAttachment> listFileAttach_ = new ArrayList<FileAttachment>() ;

	// form variable:
	private List<QuestionLanguage> listQuestionLanguage = new ArrayList<QuestionLanguage>() ;
	private List<SelectItemOption<String>> listLanguageToReponse = new ArrayList<SelectItemOption<String>>() ;
	@SuppressWarnings("unused")
	private String questionChanged_ = new String() ;
	@SuppressWarnings("unused")
	private String responseContent_ = new String () ;
	private String languageIsResponsed = "" ;
	private String link_ = "" ;
	private boolean isChildren_ = false ;
	private FAQSetting faqSetting_;
	private List<Answer> listAnswers = new ArrayList<Answer>();
	private int posOfResponse = 0;
	private boolean cateIsApprovedAnswer_ = true;
	
	private long currentDate = new Date().getTime();

	public void activate() throws Exception { }
	public void deActivate() throws Exception { }

	public String getLink() {return link_;}
	public void setLink(String link) { this.link_ = link;}
	public void setFAQSetting(FAQSetting faqSetting) {this.faqSetting_= faqSetting;}
	public UIResponseForm() throws Exception {
		isChildren_ = false ;
		inputQuestionContent_ = new UIFormStringInput(QUESTION_CONTENT, QUESTION_CONTENT, null) ;
		inputQuestionDetail_ = new UIFormTextAreaInput(QUESTION_DETAIL, QUESTION_DETAIL, null) ;
		inputResponseQuestion_ = new UIFormWYSIWYGInput(RESPONSE_CONTENT, null, null , true) ;

		checkShowAnswer_ = new UIFormCheckBoxInput<Boolean>(SHOW_ANSWER, SHOW_ANSWER, false) ;
		isApproved_ = new UIFormCheckBoxInput<Boolean>(IS_APPROVED, IS_APPROVED, false) ;
		inputAttachment_ = new UIFormInputWithActions(ATTATCH_MENTS) ;
		inputAttachment_.addUIFormInput( new UIFormInputInfo(FILE_ATTACHMENTS, FILE_ATTACHMENTS, null) ) ;
		this.setActions(new String[]{"Attachment", "AddRelation", "Save", "Cancel"}) ;
	}

	@SuppressWarnings("unused")
	private int numberOfAnswer(){
		return listAnswers.size();
	}
	
	@SuppressWarnings("unused")
	private void setListRelation() throws Exception {
    String[] relations = question_.getRelations() ;
    this.setListIdQuesRela(Arrays.asList(relations)) ;
    if(relations != null && relations.length > 0){
      SessionProvider sessionProvider = FAQUtils.getSystemProvider();
      for(String relation : relations) {
        listRelationQuestion.add(faqService.getQuestionById(relation, sessionProvider).getQuestion()) ;
      }
      sessionProvider.close();
    }
  }

	@SuppressWarnings("unchecked")
  public void setQuestionId(Question question, String languageViewed, boolean cateIsApprovedAnswer){
		this.cateIsApprovedAnswer_ = cateIsApprovedAnswer;
		listAnswers = new ArrayList<Answer>();
		SessionProvider sessionProvider = FAQUtils.getSystemProvider();
		try{
			if(listQuestIdRela!= null && !listQuestIdRela.isEmpty()) {
				listRelationQuestion.clear() ;
				listQuestIdRela.clear() ;
			}
			question_ = question ;
			
			posOfResponse = 0;
			if(languageViewed != null && languageViewed.trim().length() > 0) {
				languageIsResponsed = languageViewed ;
			} else {
				languageIsResponsed = question.getLanguage();
			}
			QuestionLanguage questionLanguage = new QuestionLanguage() ;
			questionLanguage.setLanguage(question.getLanguage()) ;
			questionLanguage.setDetail(question.getDetail()) ;
			questionLanguage.setQuestion(question.getQuestion());
			questionLanguage.setAnswers(question.getAnswers()) ;
			
			listQuestionLanguage.add(questionLanguage) ;
			listQuestionLanguage.addAll(faqService.getQuestionLanguages(question_.getId(), sessionProvider)) ;
			for(QuestionLanguage language : listQuestionLanguage) {
				listLanguageToReponse.add(new SelectItemOption<String>(language.getLanguage(), language.getLanguage())) ;
				if(language.getLanguage().equals(languageIsResponsed)) {
					questionChanged_ = language.getDetail() ;
					inputQuestionContent_.setValue(language.getQuestion());
					inputQuestionDetail_.setValue(language.getDetail()) ;
					questionDetail = language.getDetail();
					questionContent = language.getQuestion();
//					TODO
//					if(language.getAnswers() != null && language.getAnswers().length > 0) {
//					}
//					listAnswers.addAll(Arrays.asList(language.getAnswers()));
					String questionId = question.getId();
					if(!question.getLanguage().equals(languageIsResponsed)) {
						questionId = questionId + "/" + Utils.LANGUAGE_HOME+"/"+languageIsResponsed;
					}
					try{
						listAnswers.addAll((List<Answer>)faqService.getPageListAnswer(sessionProvider, questionId).getPageItem(0));
					} catch(NullPointerException npe){}
					if(listAnswers.size() > 0) {
						inputResponseQuestion_.setValue(listAnswers.get(0).getResponses()) ;
					}
					if(listAnswers.isEmpty()){
						listAnswers.add(new Answer(FAQUtils.getCurrentUser(), cateIsApprovedAnswer_));
					} 
				}
			}
			this.setListRelation(sessionProvider);
			setListFileAttach(question.getAttachMent()) ;
		} catch (Exception e) {
			e.printStackTrace() ;
		}
		this.questionId_ = question.getId() ;
		checkShowAnswer_.setChecked(question_.isActivated()) ;
		isApproved_.setChecked(question_.isApproved()) ;
		try{
			inputAttachment_.setActionField(FILE_ATTACHMENTS, getUploadFileList()) ;
		} catch (Exception e) {
			e.printStackTrace() ;
		}

		questionLanguages_ = new UIFormSelectBox(QUESTION_LANGUAGE, QUESTION_LANGUAGE, getListLanguageToReponse()) ;
		questionLanguages_.setSelectedValues(new String[]{languageIsResponsed}) ;
		questionLanguages_.setOnChange("ChangeQuestion") ;

		addChild(inputQuestionContent_) ;
		addChild(inputQuestionDetail_) ;
		addChild(questionLanguages_) ;
		addChild(inputResponseQuestion_) ;
		addChild(isApproved_) ;
		addChild(checkShowAnswer_) ;
		addChild(inputAttachment_) ;
		
		sessionProvider.close();
	}
	
	@SuppressWarnings("unused")
	private String getValue(String id){
		if(id.equals("QuestionTitle")) return questionContent;
		else return questionDetail;
	}

	public String getQuestionId(){ 
		return questionId_ ; 
	}

	public List<ActionData> getUploadFileList() { 
		List<ActionData> uploadedFiles = new ArrayList<ActionData>() ;
		for(FileAttachment attachdata : listFileAttach_) {
			ActionData fileUpload = new ActionData() ;
			fileUpload.setActionListener("Download") ;
			fileUpload.setActionParameter(attachdata.getPath());
			fileUpload.setActionType(ActionData.TYPE_ICON) ;
			fileUpload.setCssIconClass("AttachmentIcon") ; // "AttachmentIcon ZipFileIcon"
			fileUpload.setActionName(attachdata.getName() + " ("+attachdata.getSize()+" B)" ) ;
			fileUpload.setShowLabel(true) ;
			uploadedFiles.add(fileUpload) ;
			ActionData removeAction = new ActionData() ;
			removeAction.setActionListener("RemoveAttachment") ;
			removeAction.setActionName(REMOVE_FILE_ATTACH);
			removeAction.setActionParameter(attachdata.getPath());
			removeAction.setCssIconClass("LabelLink");
			removeAction.setActionType(ActionData.TYPE_LINK) ;
			uploadedFiles.add(removeAction) ;
		}
		return uploadedFiles ;
	}


	public void setListFileAttach(List<FileAttachment> listFileAttachment){
		listFileAttach_.addAll(listFileAttachment) ;
	}

	public void setListFileAttach(FileAttachment fileAttachment){
		listFileAttach_.add(fileAttachment) ;
	}

	@SuppressWarnings("unused")
	private List<FileAttachment> getListFile() {
		return listFileAttach_ ;
	}

	@SuppressWarnings("unused")
	private String getLanguageIsResponse() {
		return this.languageIsResponsed ;
	}

	public void refreshUploadFileList() throws Exception {
		((UIFormInputWithActions)this.getChildById(ATTATCH_MENTS)).setActionField(FILE_ATTACHMENTS, getUploadFileList()) ;
	}

	private void setListRelation(SessionProvider sessionProvider) throws Exception {
		String[] relations = question_.getRelations() ;
		this.setListIdQuesRela(Arrays.asList(relations)) ;
		if(relations != null && relations.length > 0)
			for(String relation : relations) {
				listRelationQuestion.add(faqService.getQuestionById(relation, sessionProvider).getDetail()) ;
			}
	}
	public List<String> getListRelation() {
		return listRelationQuestion ; 
	}

	@SuppressWarnings("unused")
	private List<SelectItemOption<String>> getListLanguageToReponse() {
		return listLanguageToReponse ;
	}

	public List<String> getListIdQuesRela() {
		return this.listQuestIdRela ;
	}

	public void setListIdQuesRela(List<String> listId) {
		if(!listQuestIdRela.isEmpty()) {
			listQuestIdRela.clear() ;
		}
		listQuestIdRela.addAll(listId) ;
	}

	public void setListRelationQuestion(List<String> listQuestionContent) {
		this.listRelationQuestion.clear() ;
		this.listRelationQuestion.addAll(listQuestionContent) ;
	}

	@SuppressWarnings("unused")
	private List<String> getListRelationQuestion() {
		return this.listRelationQuestion ;
	}

	public void setIsChildren(boolean isChildren) {
		this.isChildren_ = isChildren ;
		this.removeChildById(QUESTION_CONTENT) ; 
		this.removeChildById(QUESTION_DETAIL) ; 
		this.removeChildById(QUESTION_LANGUAGE) ;
		this.removeChildById(RESPONSE_CONTENT) ; 
		this.removeChildById(ATTATCH_MENTS) ; 
		this.removeChildById(IS_APPROVED) ;
		this.removeChildById(SHOW_ANSWER) ;
		listFileAttach_.clear() ;
		listLanguageToReponse.clear() ;
		listQuestIdRela.clear() ;
		listQuestionLanguage.clear() ;
		listRelationQuestion.clear() ;
	}

	private boolean compareTowArraies(String[] array1, String[] array2){
		List<String> list1 = new ArrayList<String>();
		list1.addAll(Arrays.asList(array1));
		int count = 0;
		for(String str : array2){
			if(list1.contains(str)) count ++;
		}
		if(count == array1.length && count == array2.length) return true;
		return false;
	}
	
	private double[] getMarkVoteAnswer(List<Double> listMarkResponse){
		double[] markVoteResponse = new double[listMarkResponse.size()];
		int i = 0;
		for(Double d : listMarkResponse){
			markVoteResponse[i++] = d;
		}
		return markVoteResponse;
	}

	// action :
		static public class SaveActionListener extends EventListener<UIResponseForm> {
			@SuppressWarnings("unchecked")
			public void execute(Event<UIResponseForm> event) throws Exception {
				ValidatorDataInput validatorDataInput = new ValidatorDataInput() ;
				UIResponseForm responseForm = event.getSource() ;

				String questionContent = responseForm.inputQuestionContent_.getValue() ;
				if(questionContent == null || questionContent.trim().length() < 1) {
					UIApplication uiApplication = responseForm.getAncestorOfType(UIApplication.class) ;
					uiApplication.addMessage(new ApplicationMessage("UIResponseForm.msg.question-null", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					return ; 
				}
				questionContent = questionContent.replaceAll("<", "&lt;").replaceAll(">", "&gt;") ;
				
				String questionDetail = responseForm.inputQuestionDetail_.getValue();
				if(!validatorDataInput.fckContentIsNotEmpty(questionDetail)) questionDetail = " ";

				String responseQuestionContent = responseForm.inputResponseQuestion_.getValue() ;
				java.util.Date date = new java.util.Date();
				if(responseQuestionContent != null && responseQuestionContent.trim().length() >0 && validatorDataInput.fckContentIsNotEmpty(responseQuestionContent)) {
						if(!responseForm.listAnswers.isEmpty() && responseForm.listAnswers.size() > 0){
							responseForm.listAnswers.get(responseForm.posOfResponse).setResponses(responseQuestionContent);
						} else {
							Answer answer = new Answer(FAQUtils.getCurrentUser(), responseForm.cateIsApprovedAnswer_);
							answer.setResponses(responseQuestionContent);
							responseForm.listAnswers.add(answer);
						}
				} else if(!responseForm.listAnswers.isEmpty() && responseForm.listAnswers.size() > 0){
					responseForm.listAnswers.remove(responseForm.posOfResponse);
				}

				if(responseForm.listAnswers.isEmpty()){
					UIApplication uiApplication = responseForm.getAncestorOfType(UIApplication.class) ;
					uiApplication.addMessage(new ApplicationMessage("UIResponseForm.msg.response-null", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					return ;
				}
				
				if(question_.getLanguage().equals(responseForm.languageIsResponsed)) {
					question_.setQuestion(questionContent);
					question_.setDetail(questionDetail) ;
					question_.setAnswers(responseForm.listAnswers.toArray(new Answer[]{}));
				} else {
					question_.setQuestion(responseForm.listQuestionLanguage.get(0).getQuestion().replaceAll("<", "&lt;").replaceAll(">", "&gt;")) ;
					question_.setDetail(responseForm.listQuestionLanguage.get(0).getDetail().replaceAll("<", "&lt;").replaceAll(">", "&gt;")) ;
					question_.setAnswers(responseForm.listQuestionLanguage.get(0).getAnswers());
				}

				for(QuestionLanguage questionLanguage : responseForm.listQuestionLanguage) {
					if(questionLanguage.getLanguage().equals(responseForm.languageIsResponsed) && !question_.getLanguage().equals(responseForm.languageIsResponsed)) {
						questionLanguage.setQuestion(questionContent) ;
						questionLanguage.setDetail(questionDetail) ;
						questionLanguage.setAnswers(responseForm.listAnswers.toArray(new Answer[]{}));
						break;
					}
				}
				
				// set relateion of question:
				question_.setRelations(responseForm.getListIdQuesRela().toArray(new String[]{})) ;

				// set show question:
				question_.setApproved(((UIFormCheckBoxInput<Boolean>)responseForm.getChildById(IS_APPROVED)).isChecked()) ;
				question_.setActivated(((UIFormCheckBoxInput<Boolean>)responseForm.getChildById(SHOW_ANSWER)).isChecked()) ;

				question_.setAttachMent(responseForm.listFileAttach_) ;

				Node questionNode = null ;

				//link
				UIFAQPortlet portlet = responseForm.getAncestorOfType(UIFAQPortlet.class) ;
				UIQuestions questions = portlet.getChild(UIFAQContainer.class).getChild(UIQuestions.class) ;
				String link = responseForm.getLink().replaceFirst("UIResponseForm", "UIQuestions").replaceFirst("Attachment", "ViewQuestion").replaceAll("&amp;", "&");
				String selectedNode = Util.getUIPortal().getSelectedNode().getUri() ;
				String portalName = "/" + Util.getUIPortal().getName() ;
				if(link.indexOf(portalName) > 0) {
					if(link.indexOf(portalName + "/" + selectedNode) < 0){
						link = link.replaceFirst(portalName, portalName + "/" + selectedNode) ;
					}									
				}	
				PortalRequestContext portalContext = Util.getPortalRequestContext();
				String url = portalContext.getRequest().getRequestURL().toString();
				url = url.replaceFirst("http://", "") ;
				url = url.substring(0, url.indexOf("/")) ;
				url = "http://" + url;
				String path = questions.getPathService(question_.getCategoryId())+"/"+question_.getCategoryId() ;
				link = link.replaceFirst("OBJECTID", path);
				link = url + link;
				question_.setLink(link) ;

				SessionProvider sessionProvider = FAQUtils.getSystemProvider();
				
				try{
					FAQUtils.getEmailSetting(responseForm.faqSetting_, false, false);
					questionNode = faqService.saveQuestion(question_, false, sessionProvider,responseForm.faqSetting_) ;
				
					FAQSetting faqSetting = new FAQSetting();
					FAQUtils.getPorletPreference(faqSetting);
					if(faqSetting.getIsDiscussForum()) {
						String pathTopic = question_.getPathTopicDiscuss();
						if(pathTopic != null && pathTopic.length() > 0) {
							ForumService forumService = (ForumService) PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
							String []ids = pathTopic.split("/");
							Post post;
							int l = question_.getAnswers().length;
							for (int i = 0; i < l; ++i) {
								String postId = question_.getAnswers()[i].getPostId();
								try {
									if(postId != null && postId.length() > 0){
										post = forumService.getPost(sessionProvider, ids[0], ids[1], ids[2], postId);
										if(post == null) {
											post = new Post();
											post.setOwner(question_.getAnswers()[i].getResponseBy());
											post.setName("Re: " + question_.getQuestion());
											post.setIcon("ViewIcon");
											question_.getAnswers()[i].setPostId(post.getId());
											post.setMessage(question_.getAnswers()[i].getResponses());
											forumService.savePost(sessionProvider, ids[0], ids[1], ids[2], post, true, "");
										}else {
											post.setMessage(question_.getAnswers()[i].getResponses());
											forumService.savePost(sessionProvider, ids[0], ids[1], ids[2], post, false, "");
										}
									} else {
										post = new Post();
										post.setOwner(question_.getAnswers()[i].getResponseBy());
										post.setName("Re: " + question_.getQuestion());
										post.setIcon("ViewIcon");
										post.setMessage(question_.getAnswers()[i].getResponses());
										forumService.savePost(sessionProvider, ids[0], ids[1], ids[2], post, true, "");
										question_.getAnswers()[i].setPostId(post.getId());
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
	            }
						}
					}
					
					faqService.saveAnswer(question_.getId(), question_.getAnswers(), sessionProvider);
					MultiLanguages multiLanguages = new MultiLanguages() ;
					for(int i = 1; i < responseForm.listQuestionLanguage.size(); i ++) {
						multiLanguages.addLanguage(questionNode, responseForm.listQuestionLanguage.get(i)) ;
						multiLanguages.saveAnswer(questionNode, responseForm.listQuestionLanguage.get(i));
					}
				} catch (PathNotFoundException e) {
					e.printStackTrace();
					UIApplication uiApplication = responseForm.getAncestorOfType(UIApplication.class) ;
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				} catch (Exception e) {
					e.printStackTrace() ;
				}

				if(question_.getAnswers() == null || question_.getAnswers().length < 1) {
					UIApplication uiApplication = responseForm.getAncestorOfType(UIApplication.class) ;
					uiApplication.addMessage(new ApplicationMessage("UIResponseForm.msg.response-invalid", new String[]{question_.getLanguage()}, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				}
				
				//cancel
				if(!responseForm.isChildren_) {
					questions.setIsNotChangeLanguage() ;
					UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
					popupAction.deActivate() ;
					event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(questions.getAncestorOfType(UIFAQContainer.class)) ; 
					if(questionNode!= null && !("" + questions.getCategoryId()).equals(question_.getCategoryId())) {
						UIApplication uiApplication = responseForm.getAncestorOfType(UIApplication.class) ;
						Category category = faqService.getCategoryById(question_.getCategoryId(), sessionProvider) ;
						uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-moved", new Object[]{category.getName()}, ApplicationMessage.WARNING)) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					}
				} else {
					UIQuestionManagerForm questionManagerForm = responseForm.getParent() ;
					UIQuestionForm questionForm = questionManagerForm.getChild(UIQuestionForm.class) ;
					if(questionManagerForm.isEditQuestion && responseForm.getQuestionId().equals(questionForm.getQuestionId())) {
						questionForm.setIsChildOfManager(true) ;
						questionForm.setQuestionId(question_) ;
					}
					questionManagerForm.isResponseQuestion = false ;
					UIPopupContainer popupContainer = questionManagerForm.getParent() ;
					event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
				}
				sessionProvider.close();
			}
		}

		static public class CancelActionListener extends EventListener<UIResponseForm> {
			public void execute(Event<UIResponseForm> event) throws Exception {
				UIResponseForm response = event.getSource() ;
				UIFAQPortlet portlet = response.getAncestorOfType(UIFAQPortlet.class) ;
				if(!response.isChildren_) {
					UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
					popupAction.deActivate() ;
					event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				} else {
					UIQuestionManagerForm questionManagerForm = portlet.findFirstComponentOfType(UIQuestionManagerForm.class) ;
					questionManagerForm.isResponseQuestion = false ;

					UIPopupContainer popupContainer = questionManagerForm.getAncestorOfType(UIPopupContainer.class) ;
					UIAttachMentForm attachMentForm = popupContainer.findFirstComponentOfType(UIAttachMentForm.class) ;
					if(attachMentForm != null) {
						UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
						popupAction.deActivate() ;
					} else {
						UIAddRelationForm addRelationForm = popupContainer.findFirstComponentOfType(UIAddRelationForm.class) ;
						if(addRelationForm != null) {
							UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
							popupAction.deActivate() ;
						}
					}
					event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
				}
			}
		}

		static public class AddRelationActionListener extends EventListener<UIResponseForm> {
			public void execute(Event<UIResponseForm> event) throws Exception {
				UIResponseForm response = event.getSource() ;
				UIPopupContainer popupContainer = response.getAncestorOfType(UIPopupContainer.class);
				UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
				UIAddRelationForm addRelationForm = popupAction.activate(UIAddRelationForm.class, 500) ;
				addRelationForm.setQuestionId(response.questionId_) ;
				addRelationForm.setRelationed(response.getListIdQuesRela()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			}
		}

		static public class AttachmentActionListener extends EventListener<UIResponseForm> {
			public void execute(Event<UIResponseForm> event) throws Exception {
				UIResponseForm response = event.getSource() ;
				UIPopupContainer popupContainer = response.getAncestorOfType(UIPopupContainer.class) ;
				UIPopupAction uiChildPopup = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
				UIAttachMentForm attachMentForm = uiChildPopup.activate(UIAttachMentForm.class, 550) ;
				attachMentForm.setResponse(true) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
			}
		}

		static public class RemoveAttachmentActionListener extends EventListener<UIResponseForm> {
			@SuppressWarnings("static-access")
			public void execute(Event<UIResponseForm> event) throws Exception {
				UIResponseForm questionForm = event.getSource() ;
				String attFileId = event.getRequestContext().getRequestParameter(OBJECTID);
				for (FileAttachment att : questionForm.listFileAttach_) {
					if (att.getPath()!= null && att.getPath().equals(attFileId)) {
						questionForm.listFileAttach_.remove(att) ;
						break;
					} else if(att.getId() != null && att.getId().equals(attFileId)) {
						questionForm.listFileAttach_.remove(att) ;
						break;
					}
				}
				questionForm.refreshUploadFileList() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(questionForm) ;
			}
		}

		static public class RemoveRelationActionListener extends EventListener<UIResponseForm> {
			public void execute(Event<UIResponseForm> event) throws Exception {
				UIResponseForm questionForm = event.getSource() ;
				String quesId = event.getRequestContext().getRequestParameter(OBJECTID);
				for(int i = 0 ; i < questionForm.listQuestIdRela.size(); i ++) {
					if(questionForm.listQuestIdRela.get(i).equals(quesId)) {
						questionForm.listRelationQuestion.remove(i) ;
						break ;
					}
				}
				questionForm.listQuestIdRela.remove(quesId) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(questionForm) ;
			}
		}

		static public class AddNewAnswerActionListener extends EventListener<UIResponseForm> {
			@SuppressWarnings("unchecked")
			public void execute(Event<UIResponseForm> event) throws Exception {
				UIResponseForm responseForm = event.getSource();
				String pos = event.getRequestContext().getRequestParameter(OBJECTID);
				UIFormWYSIWYGInput formWYSIWYGInput = responseForm.getChildById(RESPONSE_CONTENT);
				String responseContent = formWYSIWYGInput.getValue();
				java.util.Date date = new java.util.Date();
				String user = FAQUtils.getCurrentUser();
				if(pos.equals("New")){
					ValidatorDataInput validatorDataInput = new ValidatorDataInput();
					if(responseContent != null && validatorDataInput.fckContentIsNotEmpty(responseContent)){
						if(responseForm.listAnswers.isEmpty()){
							Answer answer = new Answer(user, responseForm.cateIsApprovedAnswer_);
							answer.setResponses(responseContent);
							responseForm.listAnswers.add(answer);
						} else {
							responseForm.listAnswers.get(responseForm.posOfResponse).setResponses(responseContent);
						}
						responseForm.posOfResponse = responseForm.listAnswers.size();
						responseForm.listAnswers.add(new Answer(user, responseForm.cateIsApprovedAnswer_));
						formWYSIWYGInput.setValue("");
					} else if(!responseForm.listAnswers.isEmpty() && responseForm.listAnswers.size() != responseForm.posOfResponse + 1){
						responseForm.listAnswers.remove(responseForm.posOfResponse);
						responseForm.posOfResponse = responseForm.listAnswers.size();
						responseForm.listAnswers.add(new Answer(user, responseForm.cateIsApprovedAnswer_));
						formWYSIWYGInput.setValue("");
					}
				} else {
					int newPosResponse = Integer.parseInt(pos);
					if(newPosResponse == responseForm.posOfResponse) return;
					ValidatorDataInput validatorDataInput = new ValidatorDataInput();
					if(responseContent == null || !validatorDataInput.fckContentIsNotEmpty(responseContent)){
						responseForm.listAnswers.remove(responseForm.posOfResponse);
						if(responseForm.posOfResponse < newPosResponse) newPosResponse--;
					} else if(!responseContent.equals(responseForm.listAnswers.get(responseForm.posOfResponse))){
						responseForm.listAnswers.get(responseForm.posOfResponse).setResponses(responseContent);
					}
					formWYSIWYGInput.setValue(responseForm.listAnswers.get(newPosResponse).getResponses());
					responseForm.posOfResponse = newPosResponse;
				}
				event.getRequestContext().addUIComponentToUpdateByAjax(responseForm);
			}
		}

		static public class ViewEditQuestionActionListener extends EventListener<UIResponseForm> {
			public void execute(Event<UIResponseForm> event) throws Exception {
				UIResponseForm responseForm = event.getSource();
				responseForm.isViewEditQuestion_ = true;
				event.getRequestContext().addUIComponentToUpdateByAjax(responseForm);
			}
		}

		static public class ChangeQuestionActionListener extends EventListener<UIResponseForm> {
			@SuppressWarnings("static-access")
			public void execute(Event<UIResponseForm> event) throws Exception {
				UIResponseForm responseForm = event.getSource() ;
				String language = responseForm.questionLanguages_.getValue() ;
				if(responseForm.languageIsResponsed != null && language.equals(responseForm.languageIsResponsed)) return ;
				String responseContent = responseForm.inputResponseQuestion_.getValue() ;
				String questionDetail = responseForm.inputQuestionDetail_.getValue() ;
				String questionContent = responseForm.inputQuestionContent_.getValue();
				if(questionContent == null || questionContent.trim().length() < 1) {
					UIApplication uiApplication = responseForm.getAncestorOfType(UIApplication.class) ;
					uiApplication.addMessage(new ApplicationMessage("UIResponseForm.msg.question-null", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					return ;
				}
				ValidatorDataInput validatorDataInput = new ValidatorDataInput();
				if(!validatorDataInput.fckContentIsNotEmpty(questionDetail)) questionDetail = " ";
				java.util.Date date = new java.util.Date();
				String user = FAQUtils.getCurrentUser();
				for(QuestionLanguage questionLanguage : responseForm.listQuestionLanguage) {
					if(questionLanguage.getLanguage().equals(responseForm.languageIsResponsed)) {
						if(responseContent!= null && validatorDataInput.fckContentIsNotEmpty(responseContent)) {
							if(responseForm.listAnswers.isEmpty()){
								Answer answer = new Answer(user, responseForm.cateIsApprovedAnswer_);
								answer.setResponses(responseContent);
								responseForm.listAnswers.add(answer);
							} else {
								responseForm.listAnswers.get(responseForm.posOfResponse).setResponses(responseContent);
							}
							questionLanguage.setAnswers(responseForm.listAnswers.toArray(new Answer[]{})) ;
						} else {
							if(!responseForm.listAnswers.isEmpty() && responseForm.listAnswers.size() > responseForm.posOfResponse){
								responseForm.listAnswers.remove(responseForm.posOfResponse);
							}
							if(responseForm.listAnswers.isEmpty()){
								questionLanguage.setAnswers(new Answer[]{});
							} else {
								questionLanguage.setAnswers(responseForm.listAnswers.toArray(new Answer[]{})) ;
							}
						}
						questionLanguage.setDetail(questionDetail.replaceAll("<", "&lt;").replaceAll(">", "&gt;")) ;
						questionLanguage.setQuestion(questionContent.replaceAll("<", "&lt;").replaceAll(">", "&gt;")) ;
						break ;
					}
				}
				for(QuestionLanguage questionLanguage : responseForm.listQuestionLanguage) {
					if(questionLanguage.getLanguage().equals(language)) {
						responseForm.languageIsResponsed = language ;
						responseForm.inputQuestionDetail_.setValue(questionLanguage.getDetail()) ;
						responseForm.inputQuestionContent_.setValue(questionLanguage.getQuestion()) ;
						responseForm.questionDetail = questionLanguage.getDetail();
						responseForm.questionContent = questionLanguage.getQuestion();
						if(questionLanguage.getAnswers() != null && questionLanguage.getAnswers().length > 0)
							responseForm.inputResponseQuestion_.setValue(questionLanguage.getAnswers()[0].getResponses()) ;
						else 
							responseForm.inputResponseQuestion_.setValue("") ;
						responseForm.posOfResponse = 0;

						responseForm.listAnswers.clear();

						responseForm.listAnswers.addAll(Arrays.asList(questionLanguage.getAnswers()));
						if(responseForm.listAnswers.isEmpty()){
							Answer answer = new Answer(user, responseForm.cateIsApprovedAnswer_);
							responseForm.listAnswers.add(answer);
						}
						
						break ;
					}
				}
				responseForm.isViewEditQuestion_ = false;
				event.getRequestContext().addUIComponentToUpdateByAjax(responseForm) ;
			}
		}
}


