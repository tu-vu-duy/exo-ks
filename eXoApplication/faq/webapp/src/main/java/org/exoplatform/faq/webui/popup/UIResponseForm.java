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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.faq.webui.popup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.PathNotFoundException;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.rendering.RenderHelper;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.webui.BaseUIFAQForm;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIAnswersContainer;
import org.exoplatform.faq.webui.UIAnswersPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.faq.webui.ValidatorDataInput;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.MessageBuilder;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.ks.common.webui.WebUIUtils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.input.UICheckBoxInput;
import org.exoplatform.webui.form.wysiwyg.UIFormWYSIWYGInput;

/**
 * Created by The eXo Platform SARL
 * Author : Ha Mai
 *          ha.mai@exoplatform.com 
 * Apr 17, 2008 ,3:19:00 PM
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "app:/templates/faq/webui/popup/UIResponseForm.gtmpl", 
    events = {
        @EventConfig(listeners = UIResponseForm.SaveActionListener.class), 
        @EventConfig(listeners = UIResponseForm.CancelActionListener.class), 
        @EventConfig(listeners = UIResponseForm.AddRelationActionListener.class), 
        @EventConfig(listeners = UIResponseForm.RemoveRelationActionListener.class), 
        @EventConfig(listeners = UIResponseForm.ChangeLanguageActionListener.class) 
    }
)
public class UIResponseForm extends BaseUIFAQForm implements UIPopupComponent {
  private static final String QUESTION_LANGUAGE = "Language";

  private static final String RESPONSE_CONTENT  = "QuestionRespone";

  private static final String SHOW_ANSWER       = "QuestionShowAnswer";

  private static final String IS_APPROVED       = "IsApproved";

  private Question            question_         = null;

  private String              questionDetail    = "";

  private String              questionContent   = "";

  private boolean             isModerator       = true;

  public void setModertator(boolean isMod) {
    this.isModerator = isMod;
  }

  // form input :
  private UIFormSelectBox                questionLanguages_;

  private UIFormWYSIWYGInput             inputResponseQuestion_;

  private UICheckBoxInput   checkShowAnswer_;

  private UICheckBoxInput   isApproved_;

  // question infor :
  public String                          questionId_           = "";

  private List<String>                   listRelationQuestion  = new ArrayList<String>();

  private List<String>                   listQuestIdRela       = new ArrayList<String>();

  // form variable:
  Map<String, Answer>                    mapAnswers            = new HashMap<String, Answer>();

  Map<String, QuestionLanguage>          languageMap           = new HashMap<String, QuestionLanguage>();

  private List<SelectItemOption<String>> listLanguageToReponse = new ArrayList<SelectItemOption<String>>();

  private String                         currentLanguage       = "";

  private boolean                        isChildOfQuestionManager_;

  private FAQSetting                     faqSetting_;

  private boolean                        isAnswerApproved      = true;

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  private RenderHelper renderHelper = new RenderHelper();


  public void setFAQSetting(FAQSetting faqSetting) {
    this.faqSetting_ = faqSetting;
  }

  public UIResponseForm() throws Exception {
    isChildOfQuestionManager_ = false;
    inputResponseQuestion_ = new UIFormWYSIWYGInput(RESPONSE_CONTENT, RESPONSE_CONTENT, "");
    inputResponseQuestion_.setFCKConfig(WebUIUtils.getFCKConfig());
    inputResponseQuestion_.setToolBarName("Basic");
    checkShowAnswer_ = new UICheckBoxInput(SHOW_ANSWER, SHOW_ANSWER, false);
    isApproved_ = new UICheckBoxInput(IS_APPROVED, IS_APPROVED, false);
    this.setActions(new String[] { "Save", "Cancel" });
  }

  public Question getQuestion() {
    return question_;
  }

  public void setAnswerInfor(Question question, Answer answer, String language) {
    setQuestionId(question, language, answer.getApprovedAnswers());
    mapAnswers.clear();
    mapAnswers.put(answer.getLanguage(), answer);
    inputResponseQuestion_.setValue(answer.getResponses());
    listLanguageToReponse.clear();
    listLanguageToReponse.add(new SelectItemOption<String>(answer.getLanguage() + " (default) ", answer.getLanguage()));
    questionLanguages_ = new UIFormSelectBox(QUESTION_LANGUAGE, QUESTION_LANGUAGE, listLanguageToReponse);
    questionLanguages_.setValue(answer.getLanguage());
    questionLanguages_.setSelectedValues(new String[] { answer.getLanguage() });
    getUICheckBoxInput(SHOW_ANSWER).setChecked(answer.getActivateAnswers());
    getUICheckBoxInput(IS_APPROVED).setChecked(answer.getApprovedAnswers());
  }

  public void setQuestionId(Question question, String languageViewed, boolean isAnswerApp) {
    this.isAnswerApproved = isAnswerApp;
    try {
      questionDetail = question.getDetail();
      questionContent = question.getQuestion();
      listRelationQuestion.clear();
      listQuestIdRela.clear();
      question_ = question;
      if (languageViewed != null && languageViewed.trim().length() > 0) {
        currentLanguage = languageViewed;
      } else {
        currentLanguage = question.getLanguage();
      }
      this.setListRelation();
    } catch (Exception e) {
      log.error("Can not set Question id, exception: " + e.getMessage());
    }
    this.questionId_ = question.getPath();

    listLanguageToReponse.clear();
    listLanguageToReponse.add(new SelectItemOption<String>(question.getLanguage() + " (default) ", question.getLanguage()));
    QuestionLanguage defaultLanguage = new QuestionLanguage();
    defaultLanguage.setLanguage(question.getLanguage());
    defaultLanguage.setQuestion(question.getQuestion());
    defaultLanguage.setDetail(question.getDetail());
    defaultLanguage.setState(QuestionLanguage.VIEW);
    languageMap.put(defaultLanguage.getLanguage(), defaultLanguage);
    try {
      for (QuestionLanguage language : getFAQService().getQuestionLanguages(questionId_)) {
        if (language.getLanguage().equals(currentLanguage)) {
          questionDetail = language.getDetail();
          questionContent = language.getQuestion();
        }
        languageMap.put(language.getLanguage(), language);
        if (!language.getLanguage().equals(question.getLanguage()))
          listLanguageToReponse.add(new SelectItemOption<String>(language.getLanguage(), language.getLanguage()));
      }
    } catch (Exception e) {
      log.error("Can not set Question id, exception: " + e.getMessage());
    }

    checkShowAnswer_.setChecked(question_.isActivated());
    checkShowAnswer_.setRendered(isModerator);
    isApproved_.setChecked(isAnswerApproved);
    isApproved_.setRendered(isModerator);

    questionLanguages_ = new UIFormSelectBox(QUESTION_LANGUAGE, QUESTION_LANGUAGE, listLanguageToReponse);
    questionLanguages_.setValue(currentLanguage);
    questionLanguages_.setSelectedValues(new String[] { currentLanguage });
    questionLanguages_.setOnChange("ChangeLanguage");
    questionLanguages_.setRendered((listLanguageToReponse.size() <= 1) ? false : true);
    addChild(inputResponseQuestion_);
    addChild(questionLanguages_);
    addChild(isApproved_);
    addChild(checkShowAnswer_);
  }

  protected String render(String s) {
    Question question = new Question();
    question.setDetail(s);
    return renderHelper.renderQuestion(question);
  }

  protected String getValue(String id) {
    if (id.equals("QuestionTitle"))
      return questionContent;
    else
      return questionDetail;
  }

  private void setListRelation() throws Exception {
    String[] relations = question_.getRelations();
    if (relations != null && relations.length > 0)
      for (String relation : relations) {
        Question ques = getFAQService().getQuestionById(relation);
        if(ques != null && ques.isActivated() && ques.isApproved()){
          listQuestIdRela.add(relation);
          listRelationQuestion.add(ques.getQuestion());
        }
      }
  }

  public List<String> getListRelation() {
    return listRelationQuestion;
  }

  public List<String> getListIdQuesRela() {
    return this.listQuestIdRela;
  }

  public void setListIdQuesRela(List<String> listId) {
    listQuestIdRela = new ArrayList<String>();
    listQuestIdRela.addAll(listId);
  }

  public void setListRelationQuestion(List<String> listQuestionContent) {
    this.listRelationQuestion.clear();
    this.listRelationQuestion.addAll(listQuestionContent);
  }

  protected List<String> getListRelationQuestion() {
    return this.listRelationQuestion;
  }

  public void updateChildOfQuestionManager(boolean isChild) {
    this.isChildOfQuestionManager_ = isChild;
    this.removeChildById(RESPONSE_CONTENT);
    this.removeChildById(QUESTION_LANGUAGE);
    this.removeChildById(IS_APPROVED);
    this.removeChildById(SHOW_ANSWER);
    this.inputResponseQuestion_.setValue("");
    listLanguageToReponse.clear();
    listQuestIdRela.clear();
    listRelationQuestion.clear();
  }

  private Answer[] updateDiscussForum(Answer[] answers) throws Exception {
    // Vu Duy Tu Save post Discuss Forum. Mai Ha removed to this function
    if (faqSetting_.getIsDiscussForum()) {
      String topicId = question_.getTopicIdDiscuss();
      if (topicId != null && topicId.length() > 0) {
        ForumService forumService = (ForumService) PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
        Topic topic = (Topic) forumService.getObjectNameById(topicId, org.exoplatform.forum.service.Utils.TOPIC);
        if (topic != null) {
          String[] ids = topic.getPath().split("/");
          int t = ids.length;
          String linkForum = FAQUtils.getLinkDiscuss(topicId);
          Post post;
          int l = answers.length;
          String remoteAddr = WebUIUtils.getRemoteIP();
          for (int i = 0; i < l; ++i) {
            String postId = answers[i].getPostId();
            try {
              if (postId != null && postId.length() > 0) {
                post = forumService.getPost(ids[t - 3], ids[t - 2], topicId, postId);
                if (post == null) {
                  post = new Post();
                  post.setOwner(answers[i].getResponseBy());
                  post.setName("Re: " + question_.getQuestion());
                  post.setIcon("ViewIcon");
                  answers[i].setPostId(post.getId());
                  post.setMessage(answers[i].getResponses());
                  post.setLink(linkForum);
                  post.setIsApproved(!topic.getIsModeratePost());
                  post.setRemoteAddr(remoteAddr);
                  forumService.savePost(ids[t - 3], ids[t - 2], topicId, post, true, new MessageBuilder());
                } else {
                  post.setIsApproved(!topic.getIsModeratePost());
                  post.setMessage(answers[i].getResponses());
                  forumService.savePost(ids[t - 3], ids[t - 2], topicId, post, false, new MessageBuilder());
                }
              } else {
                post = new Post();
                post.setOwner(answers[i].getResponseBy());
                post.setName("Re: " + question_.getQuestion());
                post.setIcon("ViewIcon");
                post.setMessage(answers[i].getResponses());
                post.setLink(linkForum);
                post.setIsApproved(!topic.getIsModeratePost());
                post.setRemoteAddr(remoteAddr);
                forumService.savePost(ids[t - 3], ids[t - 2], topicId, post, true, new MessageBuilder());
                answers[i].setPostId(post.getId());
              }
            } catch (Exception e) {
              log.error("Can not discuss question into forum, exception: ", e);
            }
          }
        }
      }
    }
    return answers;
  }

  static public class SaveActionListener extends EventListener<UIResponseForm> {
    public void execute(Event<UIResponseForm> event) throws Exception {
      UIResponseForm responseForm = event.getSource();
      String language = responseForm.questionLanguages_.getValue();
      String responseQuestionContent = responseForm.inputResponseQuestion_.getValue();
      responseQuestionContent = CommonUtils.encodeSpecialCharInContent(responseQuestionContent);
      Answer answer;
      if (ValidatorDataInput.fckContentIsNotEmpty(responseQuestionContent)) {
        if (responseForm.mapAnswers.containsKey(language)) {
          answer = responseForm.mapAnswers.get(language);
          answer.setResponses(responseQuestionContent);
          answer.setNew(true);
        } else {
          answer = new Answer();
          String currentUser = FAQUtils.getCurrentUser() ;
          answer.setResponseBy(currentUser);
          answer.setFullName(FAQUtils.getFullName(null)) ;
          answer.setNew(true);
          answer.setResponses(responseQuestionContent);
          answer.setLanguage(language);
        }
        // author: Vu Duy Tu. set show answer
        answer.setApprovedAnswers(((UICheckBoxInput) responseForm.getChildById(IS_APPROVED)).isChecked());
        answer.setActivateAnswers(((UICheckBoxInput) responseForm.getChildById(SHOW_ANSWER)).isChecked());
        responseForm.mapAnswers.put(language, answer);
      } else {
        if (responseForm.mapAnswers.containsKey(language)) {
          answer = responseForm.mapAnswers.get(language);
          answer.setNew(false);
          responseForm.mapAnswers.put(language, answer);
        }
      }

      if (responseForm.mapAnswers.isEmpty()) {
        responseForm.warning("UIResponseForm.msg.response-null");
        return;
      }

      // set relateion of question:
      Question question = responseForm.getQuestion();
      question.setRelations(responseForm.listQuestIdRela.toArray(new String[responseForm.listQuestIdRela.size()]));

      // link
      UIAnswersPortlet portlet = responseForm.getAncestorOfType(UIAnswersPortlet.class);
      UIQuestions uiQuestions = portlet.getChild(UIAnswersContainer.class).getChild(UIQuestions.class);
      // Link Question to send mail
       if(FAQUtils.isFieldEmpty(question.getLink())) {
         question.setLink(FAQUtils.getQuestionURI(question.getId(), false));
       }

      // set answer to question for discuss forum function
      if (responseForm.mapAnswers.containsKey(question.getLanguage())) {
        question.setAnswers(new Answer[] { responseForm.mapAnswers.get(question.getLanguage()) });
      }
      try {
        FAQUtils.getEmailSetting(responseForm.faqSetting_, false, false);
        // save answers and question
        Answer[] answers = responseForm.mapAnswers.values().toArray(new Answer[] {});
        try {
          // author: Vu Duy Tu. Make discuss forum
          answers = responseForm.updateDiscussForum(answers);
        } catch (Exception e) {
          responseForm.log.error("Can not discuss question into forum, exception: ", e);
        }
        responseForm.getFAQService().saveAnswer(question.getPath(), answers);
        responseForm.getFAQService().updateQuestionRelatives(question.getPath(), responseForm.listQuestIdRela.toArray(new String[responseForm.listQuestIdRela.size()]));
        if (!responseForm.isModerator && !responseForm.isAnswerApproved){
          responseForm.info("UIResponseForm.msg.pending-for-moderation", false);
        }
      } catch (PathNotFoundException e) {
        responseForm.log.error("Can not save Question, this question is deleted, exception: " + e.getMessage());
        responseForm.warning("UIQuestions.msg.question-id-deleted", false);
      } catch (Exception e) {
        responseForm.log.error("Can not save Question, exception: " + e.getMessage());
      }

      // cancel
      if (!responseForm.isChildOfQuestionManager_) {
        uiQuestions.updateCurrentQuestionList();
        // uiQuestions.updateCurrentLanguage() ;
        uiQuestions.updateLanguageMap();
        uiQuestions.setLanguageView(responseForm.currentLanguage);
        UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
        popupAction.deActivate();
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions.getAncestorOfType(UIAnswersContainer.class));
      } else {
        UIQuestionManagerForm questionManagerForm = responseForm.getParent();
        UIQuestionForm questionForm = questionManagerForm.getChild(UIQuestionForm.class);
        if (questionManagerForm.isEditQuestion && responseForm.questionId_.equals(questionForm.getQuestionId())) {
          questionForm.setIsChildOfManager(true);
          questionForm.setQuestion(question);
          questionForm.setIsMode(true);
        }
        questionManagerForm.isResponseQuestion = false;
        UIPopupContainer popupContainer = questionManagerForm.getParent();
        event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer);
      }
    }
  }

  static public class CancelActionListener extends EventListener<UIResponseForm> {
    public void execute(Event<UIResponseForm> event) throws Exception {
      UIResponseForm response = event.getSource();
      UIAnswersPortlet portlet = response.getAncestorOfType(UIAnswersPortlet.class);
      if (!response.isChildOfQuestionManager_) {
        UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
        popupAction.deActivate();
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
      } else {
        UIQuestionManagerForm questionManagerForm = portlet.findFirstComponentOfType(UIQuestionManagerForm.class);
        questionManagerForm.isResponseQuestion = false;
        UIPopupContainer popupContainer = questionManagerForm.getAncestorOfType(UIPopupContainer.class);
        UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class);
        if (popupAction != null) {
          popupAction.deActivate();
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer);
      }
    }
  }

  static public class AddRelationActionListener extends EventListener<UIResponseForm> {
    public void execute(Event<UIResponseForm> event) throws Exception {
      UIResponseForm response = event.getSource();
      UIPopupContainer popupContainer = response.getAncestorOfType(UIPopupContainer.class);
      UIAddRelationForm addRelationForm = response.openPopup(popupContainer, UIAddRelationForm.class, 500, 0);
      addRelationForm.setQuestionId(response.questionId_);
      addRelationForm.setRelationed(response.listQuestIdRela);
    }
  }

  static public class RemoveRelationActionListener extends EventListener<UIResponseForm> {
    public void execute(Event<UIResponseForm> event) throws Exception {
      UIResponseForm questionForm = event.getSource();
      String quesId = event.getRequestContext().getRequestParameter(OBJECTID);
      for (int i = 0; i < questionForm.listQuestIdRela.size(); i++) {
        if (questionForm.listQuestIdRela.get(i).equals(quesId)) {
          questionForm.listRelationQuestion.remove(i);
          break;
        }
      }
      questionForm.listQuestIdRela.remove(quesId);
      event.getRequestContext().addUIComponentToUpdateByAjax(questionForm);
    }
  }

  static public class ChangeLanguageActionListener extends EventListener<UIResponseForm> {
    public void execute(Event<UIResponseForm> event) throws Exception {
      UIResponseForm responseForm = event.getSource();
      String newLanguage = responseForm.questionLanguages_.getValue();
      String responseContent = responseForm.inputResponseQuestion_.getValue();
      String user = FAQUtils.getCurrentUser();
      Answer answer;
      if (ValidatorDataInput.fckContentIsNotEmpty(responseContent)) {
        if (responseForm.mapAnswers.containsKey(responseForm.currentLanguage)) {
          answer = responseForm.mapAnswers.get(responseForm.currentLanguage);
          answer.setResponses(responseContent);
        } else {
          answer = new Answer();
          answer.setNew(true);
          answer.setActivateAnswers(true);
          answer.setApprovedAnswers(responseForm.isAnswerApproved);
          answer.setResponseBy(user);
          answer.setResponses(responseContent);
          answer.setLanguage(responseForm.currentLanguage);
        }
        responseForm.mapAnswers.put(responseForm.currentLanguage, answer);
      } else {
        if (responseForm.mapAnswers.containsKey(responseForm.currentLanguage)) {
          answer = responseForm.mapAnswers.get(responseForm.currentLanguage);
          answer.setNew(false);
          responseForm.mapAnswers.put(responseForm.currentLanguage, answer);
        }
      }

      // get Question by language
      responseForm.currentLanguage = newLanguage;
      if (newLanguage.equals(responseForm.question_.getLanguage())) {
        responseForm.questionDetail = responseForm.question_.getDetail();
        responseForm.questionContent = responseForm.question_.getQuestion();
      } else {
        responseForm.questionDetail = responseForm.languageMap.get(newLanguage).getDetail();
        responseForm.questionContent = responseForm.languageMap.get(newLanguage).getQuestion();
      }

      // get answer by language
      if (responseForm.mapAnswers.containsKey(newLanguage))
        responseForm.inputResponseQuestion_.setValue(responseForm.mapAnswers.get(newLanguage).getResponses());
      else
        responseForm.inputResponseQuestion_.setValue("");
      event.getRequestContext().addUIComponentToUpdateByAjax(responseForm);
    }
  }
}
