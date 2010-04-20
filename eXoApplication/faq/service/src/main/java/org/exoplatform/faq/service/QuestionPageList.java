/*
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
 */
package org.exoplatform.faq.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.ks.common.jcr.SessionManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * All questions and their properties, methods are getted from database
 * to view in page will be restore in this class. And ratification, questions can be
 * sorted by alphabet or by date time when quetsion is created.
 * 
 * @author Hung Nguyen (hung.nguyen@exoplatform.com)
 * @since Mar 08, 2008
 */
public class QuestionPageList extends JCRPageList {

  /** The iter_. */
  private NodeIterator iter_ = null ;
  
  /** The is query_. */
  private boolean isQuery_ = false ;
  
  /** The value_. */
  private String value_ ;
  
  /** The is not yet answered. */
  private boolean isNotYetAnswered = false;
  
  /** The list questions_. */
  private List<Question> listQuestions_ = null;
  
  /** The list faq form search s_. */
  private List<ObjectSearchResult> listFAQFormSearchS_ = null;
  
  /** The list watchs_. */
  private List<Watch> listWatchs_ = null;
  
  /** The list categories_. */
  private List<Category> listCategories_ = null;
  
  /** The question query_. */
  private String questionQuery_ = new String();
  
  /** The node category_. */
  private Node nodeCategory_ = null;
  
  /** The list object_. */
  private List<Object> listObject_ = new ArrayList<Object>();
  
  /** The faq setting_. */
  private FAQSetting faqSetting_ = null;
  
  final private static String ANSWER_HOME = "faqAnswerHome".intern();
  final private static String COMMENT_HOME = "faqCommentHome".intern();
  
  private SessionManager sessionManager;
  
  private static Log log = ExoLogger.getLogger(QuestionPageList.class);
  /**
   * Sets the not yet answered. Set parameter is <code>true</code> if want get questions are not
   * yet answered opposite set is <code>false</code> or don't do (default value is <code>false</code>)
   * 
   * @param isNotYetAnswered  the new not yet answered, is <code>true</code> if want get
   * questoins not yet answered and is <code>false</code> if opposite
   */
  public void setNotYetAnswered(boolean isNotYetAnswered) {
    this.isNotYetAnswered = isNotYetAnswered;
    setTotalQuestion();
  }
  
  /**
   * get total questions are not yet ansewered. The first, get all questions in faq system
   * then each questions check if property <code>response</code> in default language
   * is <code>null</code> then put this question into the <code>List</code>
   * else get all children nodes of this question (if it have) and check if one of them is
   * not yet ansewred then put this question into the <code>List</code>
   */
  private void setTotalQuestion(){
  	listQuestions_ = new ArrayList<Question>();
  	NodeIterator nodeIterator = iter_;
  	NodeIterator languageIter = null;
  	Node questionNode = null;
  	Node languageNode = null;
  	Node language;
  	String languages = null;
  	while(nodeIterator.hasNext()){
  		languages = new String();
  		questionNode = nodeIterator.nextNode();
  		try {
        if(!questionNode.hasNode(ANSWER_HOME) || questionNode.getNode(ANSWER_HOME).getNodes().getSize() < 1){
        	languages = questionNode.getProperty("exo:language").getValue().getString();
        }
      	if(questionNode.hasNode("languages")){
      		languageNode = questionNode.getNode("languages");
      		languageIter = languageNode.getNodes();
      		while(languageIter.hasNext()){
      			language = languageIter.nextNode();
      			if(!language.hasNode(ANSWER_HOME) || language.getNode(ANSWER_HOME).getNodes().getSize() < 1) {
      				if(languages != null && languages.trim().length() > 0) languages += ",";
      				languages += language.getProperty("exo:language").getString();
      			}
      		}
      	}
      	if(languages != null && languages.trim().length() > 0) listQuestions_.add(getQuestion(questionNode).setLanguagesNotYetAnswered(languages));
      } catch (Exception e) {
        log.error("Fail to set total questions: ", e);
      }
  	}
  	setAvailablePage(listQuestions_.size()) ;
  }

  /**
   * Class constructor specifying iter contain quetion nodes, value,
   * it's query or not, how to sort all question.
   * 
   * @param iter        NodeIterator use to store question nodes
   * @param pageSize    this param is used to set number of question per page
   * @param value       query string is used to get question node
   * @param isQuery     is <code>true</code> is param value is query string
   * and is <code>false</code> if opposite
   * 
   * @throws Exception  if repository occur exception
   */
  public QuestionPageList(NodeIterator iter, long pageSize, String value, boolean isQuery) throws Exception {
    super(pageSize) ;
    this.sessionManager = FAQServiceUtils.getSessionManager();
    iter_ = iter ;
    value_ = value ;
    isQuery_ = isQuery ;
    setAvailablePage(iter.getSize()) ;   
  }
  
  /**
   * Instantiates a new question page list.
   * 
   * @param faqFormSearchs the faq form searchs
   * @param pageSize the page size
   * 
   * @throws Exception the exception
   */
  public QuestionPageList(List<ObjectSearchResult> faqFormSearchs, long pageSize) throws Exception {
  	super(pageSize) ;
    this.sessionManager = FAQServiceUtils.getSessionManager();
  	this.listFAQFormSearchS_ = faqFormSearchs;
  	setAvailablePage(faqFormSearchs.size()) ;    
  }
  
  /**
   * Instantiates a new question page list.
   * 
   * @param listWatch the list watch
   * @param page the page
   * 
   * @throws Exception the exception
   */
  public QuestionPageList(List<Watch> listWatch, double page) throws Exception {
  	super(10) ;
    this.sessionManager = FAQServiceUtils.getSessionManager();
  	this.listWatchs_ = listWatch;
  	setAvailablePage(listWatch.size()) ;    
  }
  
  /**
   * Instantiates a new question page list.
   * 
   * @param listCategories the list categories
   * 
   * @throws Exception the exception
   */
  public QuestionPageList(List<Category> listCategories) throws Exception {
  	super(10) ;
    this.sessionManager = FAQServiceUtils.getSessionManager();
  	this.listCategories_ = listCategories;
  	setAvailablePage(listCategories.size()) ;    
  }
  
  /**
   * Instantiates a new question page list.
   * 
   * @param listQuestions the list questions
   * @param size the size
   * 
   * @throws Exception the exception
   */
  public QuestionPageList(List<Question> listQuestions, int size) throws Exception{
  	super(10) ;
    this.sessionManager = FAQServiceUtils.getSessionManager();
  	this.listQuestions_ = listQuestions;
  	setAvailablePage(size) ;    
  }
  
  /**
   * Instantiates a new question page list.
   * 
   * @param categoryNode the category node
   * @param quesQuerry the ques querry
   * @param listObject the list object
   * @param setting the setting
   * 
   * @throws Exception the exception
   */
  public QuestionPageList(Node categoryNode, String quesQuerry, List<Object> listObject, FAQSetting setting) throws Exception{
  	super(10) ;
    this.sessionManager = FAQServiceUtils.getSessionManager();
  	this.questionQuery_ = quesQuerry;
  	this.nodeCategory_ = categoryNode;
  	this.listObject_.addAll(listObject);
  	this.faqSetting_ = setting;
  	setAvailablePage(listObject.size()) ;    
  }

  /**
   * Get questions to view in page. Base on total questions, number of question per page,
   * current page, this function get right questions. For example: have 100 questions,
   * view 10 questions per page, and current page is 1, this function will get questions
   * form first question to tenth question and put them into a list which is viewed.
   * 
   * @param username  the name of current user
   * @param page      number of current page
   * 
   * @throws Exception the exception
   * 
   * @see             org.exoplatform.faq.service.JCRPageList#populateCurrentPage(long, java.lang.String)
   */
  protected void populateCurrentPage(long page, String username) throws Exception  {
    if(iter_ == null || !iter_.hasNext()) {
      Session session = getJCRSession() ;
      if(isQuery_) {
        QueryManager qm = session.getWorkspace().getQueryManager() ;
        Query query = qm.createQuery(value_, Query.XPATH);
        QueryResult result = query.execute();
        iter_ = result.getNodes();
      } else {
        Node node = (Node)session.getItem(value_) ;
        iter_ = node.getNodes() ;
      }
      if(isNotYetAnswered)	setTotalQuestion();
      closeSession();
    }
    if(isNotYetAnswered){
    	setAvailablePage(listQuestions_.size()) ;
    } else {
    	setAvailablePage(iter_.getSize()) ;
    }
    long pageSize = getPageSize() ;
    long position = 0 ;
    if(page == 1) position = 0;
    else {
      position = (page-1) * pageSize ;
    }
    currentListPage_ = new ArrayList<Question>() ;
    if(!isNotYetAnswered) {
    	iter_.skip(position) ;
    	Question question = new Question();
      for(int i = 0; i < pageSize; i ++) {
        // add != null to fix bug 514
        if(iter_ != null && iter_.hasNext()){
        	question = getQuestion(iter_.nextNode());
          currentListPage_.add(question);
        }else {
          break ;
        }
      }
    } else {
      pageSize += position;
      for(int i = (int)position; i < pageSize; i ++) {
        // add != null to fix bug 514
        if(i < listQuestions_.size()){
          currentListPage_.add(listQuestions_.get(i));
        }else {
          break ;
        }
      }
    }    
    iter_ = null ;
  }
// Created by Vu Duy Tu 
  @SuppressWarnings("unchecked")
  protected void populateCurrentPageItem(long page) throws Exception  {
  	if(iter_ == null || !iter_.hasNext()) {
  		Session session = getJCRSession() ;
  		if(isQuery_) {
  			QueryManager qm = session.getWorkspace().getQueryManager() ;
  			Query query = qm.createQuery(value_, Query.XPATH);
  			QueryResult result = query.execute();
  			iter_ = result.getNodes();
  		} else {
  			Node node = (Node)session.getItem(value_) ;
  			iter_ = node.getNodes() ;
  		}
  		closeSession();
  		setAvailablePage(iter_.getSize()) ;
  		checkAndSetPage(page) ;
  		page = currentPage_;
  	}
  	Node currentNode;
  	long pageSize = getPageSize() ;
  	long position = 0 ;
  	if(page == 1) position = 0;
  	else if(page > 1) {
  		position = (page-1) * pageSize ;
  		iter_.skip(position) ;
  	} else pageSize = iter_.getSize();
  	currentListObject_ = new ArrayList<Question>() ;
  	for(int i = 0; i < pageSize; i ++) {
			if(iter_.hasNext()){
				currentNode = iter_.nextNode() ;
				if(currentNode.isNodeType("exo:answer")) {
					currentListObject_.add(getAnswerByNode(currentNode)) ;
				}else if(currentNode.isNodeType("exo:comment")) {
					currentListObject_.add(getCommentByNode(currentNode)) ;
				}
			}else {
				break ;
			}
  	}
  	iter_ = null ;    
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.JCRPageList#populateCurrentPageWatch(long, java.lang.String)
   */
  @Override
  protected void populateCurrentPageWatch(long page, String username) throws Exception {
  	if(iter_ == null || !iter_.hasNext()) {
      Session session = getJCRSession() ;
      QueryManager qm = session.getWorkspace().getQueryManager() ;
      Query query = qm.createQuery(value_, Query.XPATH);
      QueryResult result = query.execute();
      iter_ = result.getNodes();
      closeSession();
    }
    long pageSize = getPageSize() ;
    long position = 0 ;
    if(page <= 1) position = 0;
    else {
      position = (page - 1) * pageSize ;
    }
    currentListWatch_ = new ArrayList<Watch>();
    Node watchNode = null;
    int count = 0;
    while (iter_.hasNext()) {
    	count ++;
      watchNode = iter_.nextNode();
      currentListWatch_.addAll(getWatchs(watchNode, false, (int)position, (int)pageSize));
    }
    iter_ = null ;  
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.JCRPageList#populateCurrentPageResultSearch(long, java.lang.String)
   */
  @Override
  protected void populateCurrentPageResultSearch(long page, String username) throws Exception {
	  long pageSize = getPageSize();
	  long position = 0;
	  if(page == 1) position = 0;
	  else {
	  	position = (page - 1) * pageSize;
	  }
	  pageSize *= page ;
	  currentListResultSearch_ = new ArrayList<ObjectSearchResult>();
	  for(int i = (int)position; i < pageSize && i < this.listFAQFormSearchS_.size(); i ++){
	  	currentListResultSearch_.add(listFAQFormSearchS_.get(i));
	  }
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.JCRPageList#populateCurrentPageCategoriesSearch(long, java.lang.String)
   */
  @Override
  protected void populateCurrentPageCategoriesSearch(long page, String username) throws Exception {
  	long pageSize = getPageSize();
  	long position = 0;
  	if(!isQuery_){
		  if(page == 1) position = 0;
		  else {
		  	position = (page - 1) * pageSize;
		  }
		  pageSize *= page ;
		  currentListCategory_ = new ArrayList<Category>();
		  for(int i = (int)position; i < pageSize && i < this.listCategories_.size(); i ++){
		  	currentListCategory_.add(listCategories_.get(i));
		  }
  	} else {
		  // get category for watches
		  if(iter_ == null || !iter_.hasNext()) {
	      Session session = getJCRSession() ;
        QueryManager qm = session.getWorkspace().getQueryManager() ;
        Query query = qm.createQuery(value_, Query.XPATH);
        QueryResult result = query.execute();
        iter_ = result.getNodes();
	      closeSession();
	    }
    	setAvailablePage(iter_.getSize()) ;
	    if(page == 1) position = 0;
	    else {
	      position = (page-1) * pageSize ;
	    }
	    currentListCategory_ = new ArrayList<Category>() ;
    	iter_.skip(position) ;
    	Category category = null;
      for(int i = 0; i < pageSize; i ++) {
        // add != null to fix bug 514
        if(iter_ != null && iter_.hasNext()){
        	category = getCategory(iter_.nextNode());
        	currentListCategory_.add(category);
        }else {
          break ;
        }
      }
	    iter_ = null ;    
  	}
  }

	/* (non-Javadoc)
	 * @see org.exoplatform.faq.service.JCRPageList#populateCurrentPageQuestionsSearch(long, java.lang.String)
	 */
	@Override
  protected void populateCurrentPageQuestionsSearch(long page, String username) throws Exception {
		long pageSize = getPageSize();
	  long position = 0;
	  if(page == 1) position = 0;
	  else {
	  	position = (page - 1) * pageSize;
	  }
	  pageSize *= page ;
	  currentListPage_ = new ArrayList<Question>();
	  for(int i = (int)position; i < pageSize && i < this.listQuestions_.size(); i ++){
	  	currentListPage_.add(listQuestions_.get(i));
	  }
  }
	
	/* (non-Javadoc)
	 * @see org.exoplatform.faq.service.JCRPageList#populateCurrentPageCategoriesQuestionsSearch(long, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
  @Override
	protected void populateCurrentPageCategoriesQuestionsSearch(long page, String username) throws Exception {
		String idSearch = getObjectId();
		int posSearch = 0;
		if(listObject_ == null || listObject_.isEmpty()){
			listObject_ = new ArrayList<Object>();
			int size = 0;
      
      //================== get list questions ===================================================
      Session session = getJCRSession() ;
      QueryManager qm = session.getWorkspace().getQueryManager() ;
      Query query = qm.createQuery(questionQuery_, Query.XPATH);
      QueryResult result = query.execute();
      iter_ = result.getNodes();
      Question question = null;
      while(iter_.hasNext()){
      	question = getQuestion(iter_.nextNode());
      	if(!question.getId().equals(idSearch)) size ++;
      	else posSearch = size + 1;
      	listObject_.add(question);
      }
      
      closeSession(); // was missing ?
			//================== get list catetgories ===================================================
      iter_ = null;
      iter_ = nodeCategory_.getNodes();
      List<Category> listCategory = new ArrayList<Category>();
      while(iter_.hasNext()){
      	listCategory.add(getCategory(iter_.nextNode()));
      }
      if(faqSetting_.getOrderBy().equals("created")) {
	    	if(faqSetting_.getOrderType().equals("asc")) Collections.sort(listCategory, new Utils.DatetimeComparatorASC()) ;
	    	else Collections.sort(listCategory, new Utils.DatetimeComparatorDESC()) ;
	    } else {
				if(faqSetting_.getOrderType().equals("asc")) Collections.sort(listCategory, new Utils.NameComparatorASC()) ;
				else Collections.sort(listCategory, new Utils.NameComparatorDESC()) ;
			}
      listObject_.addAll(listCategory);
      
      if(getAvailablePage() < listObject_.size())
      	setAvailablePage(listObject_.size());
      iter_ = null;
		}
		
		long pageSize = getPageSize();
	  long position = 0;
	  if(posSearch > 0){
	  	posSearch =  posSearch/(int)pageSize;
	  	int t = posSearch%(int)pageSize;
	  	if(t > 0 || posSearch == 0){
	  		posSearch ++;
	  	}
	  	page = posSearch;
	  	setObjectId(null);
	  }
	  setPageJump(page);
	  if(page == 1) position = 0;
	  else {
	  	position = (page - 1) * pageSize;
	  }
	  pageSize *= page ;
	  currentListObject_ = new ArrayList<Object>();
	  for(int i = (int)position; i < pageSize && i < this.listObject_.size(); i ++){
	  	currentListObject_.add(listObject_.get(i));
	  }
	  listObject_ = null;
  }
/*	
	private Boolean[] ValuesToBoolean(Value[] Val) throws Exception {
  	if(Val.length < 1) return new Boolean[]{} ;
  	Boolean[] bools = new Boolean[Val.length] ;
  	for(int i = 0; i < Val.length; ++i) {
  		bools[i] = Val[i].getBoolean();
  	}
  	return bools;
  }
*/  
  /**
   * Set values for all question's properties from question node which is got
   * form NodeIterator.
   * 
   * @param questionNode  the question node is got form NodeIterator
   * 
   * @return              Question with all properties are set from question node
   * 
   * @throws Exception    if repository ,value format or path of node occur exception
   */
	private Question getQuestion(Node questionNode) throws Exception {
  	Question question = new Question() ;
  	question.setId(questionNode.getName()) ;
  	if(questionNode.hasProperty("exo:language")) question.setLanguage(questionNode.getProperty("exo:language").getString()) ;
  	if(questionNode.hasProperty("exo:name")) question.setDetail(questionNode.getProperty("exo:name").getString()) ;
    if(questionNode.hasProperty("exo:author")) question.setAuthor(questionNode.getProperty("exo:author").getString()) ;
    if(questionNode.hasProperty("exo:email")) question.setEmail(questionNode.getProperty("exo:email").getString()) ;
    if(questionNode.hasProperty("exo:title")) question.setQuestion(questionNode.getProperty("exo:title").getString()) ;
    if(questionNode.hasProperty("exo:createdDate")) question.setCreatedDate(questionNode.getProperty("exo:createdDate").getDate().getTime()) ;
    if(questionNode.hasProperty("exo:categoryId")) question.setCategoryId(questionNode.getProperty("exo:categoryId").getString()) ;
    /*String catePath = questionNode.getParent().getParent().getPath() ;
		question.setCategoryId(catePath.substring(catePath.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1)) ;*/
    if(questionNode.hasProperty("exo:isActivated")) question.setActivated(questionNode.getProperty("exo:isActivated").getBoolean()) ;
    if(questionNode.hasProperty("exo:isApproved")) question.setApproved(questionNode.getProperty("exo:isApproved").getBoolean()) ;
    if(questionNode.hasProperty("exo:relatives")) question.setRelations(ValuesToStrings(questionNode.getProperty("exo:relatives").getValues())) ;  	
    if(questionNode.hasProperty("exo:nameAttachs")) question.setNameAttachs(ValuesToStrings(questionNode.getProperty("exo:nameAttachs").getValues())) ;  	
    if(questionNode.hasProperty("exo:usersVote")) question.setUsersVote(ValuesToStrings(questionNode.getProperty("exo:usersVote").getValues())) ;  	
    if(questionNode.hasProperty("exo:markVote")) question.setMarkVote(questionNode.getProperty("exo:markVote").getValue().getDouble()) ;
    if(questionNode.hasProperty("exo:emailWatching")) question.setEmailsWatch(ValuesToStrings(questionNode.getProperty("exo:emailWatching").getValues())) ;
    if(questionNode.hasProperty("exo:userWatching")) question.setUsersWatch(ValuesToStrings(questionNode.getProperty("exo:userWatching").getValues())) ;
    if(questionNode.hasProperty("exo:topicIdDiscuss")) question.setTopicIdDiscuss(questionNode.getProperty("exo:topicIdDiscuss").getString()) ;
    String path = questionNode.getPath() ;
		question.setPath(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1)) ;
		
    List<FileAttachment> listFile = new ArrayList<FileAttachment>() ;
  	NodeIterator nodeIterator = questionNode.getNodes() ;
    Node nodeFile ;
    Node node ;
    FileAttachment attachment =  null;
    String workspace = questionNode.getSession().getWorkspace().getName() ;
    while(nodeIterator.hasNext()){
      node = nodeIterator.nextNode() ;
      if(node.isNodeType("exo:faqAttachment")) {
        attachment = new FileAttachment() ;
        nodeFile = node.getNode("jcr:content") ;
        attachment.setId(node.getPath());
        attachment.setMimeType(nodeFile.getProperty("jcr:mimeType").getString());
        attachment.setNodeName(node.getName());
        attachment.setName(nodeFile.getProperty("exo:fileName").getValue().getString());
        attachment.setWorkspace(workspace) ;
        attachment.setPath("/" + workspace + node.getPath()) ;
        try{
          if(nodeFile.hasProperty("jcr:data")) attachment.setSize(nodeFile.getProperty("jcr:data").getStream().available());
          else attachment.setSize(0) ;
        } catch (Exception e) {
          attachment.setSize(0) ;
        }
        listFile.add(attachment);
      }
    }
    question.setAttachMent(listFile) ;
    question.setAnswers(getAnswers(questionNode));
    question.setComments(getComment(questionNode));
    return question ;
  }
	
	public Answer[] getAnswers(Node questionNode) throws Exception{
		try{
			if(!questionNode.hasNode(ANSWER_HOME)) return new Answer[]{};
			NodeIterator nodeIterator = questionNode.getNode(ANSWER_HOME).getNodes();
			Answer[] answers = new Answer[(int) nodeIterator.getSize()];
			Node answerNode = null;
			int i = 0;
			while(nodeIterator.hasNext()){
				answerNode = nodeIterator.nextNode();
				answers[i] = getAnswerByNode(answerNode);
				i ++;
			}
			return answers;
		} catch (Exception e){
			return new Answer[]{};
		}
	}
	
	public Answer getAnswerByNode(Node answerNode) throws Exception {
		Answer answer = new Answer();
		answer.setId(answerNode.getName()) ;
  	if(answerNode.hasProperty("exo:responses")) answer.setResponses((answerNode.getProperty("exo:responses").getValue().getString())) ;
    if(answerNode.hasProperty("exo:responseBy")) answer.setResponseBy((answerNode.getProperty("exo:responseBy").getValue().getString())) ;
    if(answerNode.hasProperty("exo:fullName")) answer.setFullName((answerNode.getProperty("exo:fullName").getValue().getString())) ;
    if(answerNode.hasProperty("exo:dateResponse")) answer.setDateResponse((answerNode.getProperty("exo:dateResponse").getValue().getDate().getTime())) ;
    if(answerNode.hasProperty("exo:usersVoteAnswer")) answer.setUsersVoteAnswer(ValuesToStrings(answerNode.getProperty("exo:usersVoteAnswer").getValues())) ;
    if(answerNode.hasProperty("exo:MarkVotes")) answer.setMarkVotes(answerNode.getProperty("exo:MarkVotes").getValue().getLong()) ;
    if(answerNode.hasProperty("exo:approveResponses")) answer.setApprovedAnswers((answerNode.getProperty("exo:approveResponses").getValue().getBoolean())) ;
    if(answerNode.hasProperty("exo:activateResponses")) answer.setActivateAnswers((answerNode.getProperty("exo:activateResponses").getValue().getBoolean())) ;
    if(answerNode.hasProperty("exo:postId")) answer.setPostId(answerNode.getProperty("exo:postId").getString()) ;
    String path = answerNode.getPath() ;
		answer.setPath(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1)) ;
    return answer;
  }
  
	public Comment[] getComment(Node questionNode) throws Exception{
		try{
			if(!questionNode.hasNode(COMMENT_HOME)) return new Comment[]{};
			NodeIterator nodeIterator = questionNode.getNode(COMMENT_HOME).getNodes();
			Comment[] comments = new Comment[(int) nodeIterator.getSize()];
			Node commentNode = null;
			int i = 0;
			while(nodeIterator.hasNext()){
				commentNode = nodeIterator.nextNode();
				comments[i] = getCommentByNode(commentNode);
				i ++;
			}
			return comments;
		} catch (Exception e){
		  log.error("Fail to get comments: ", e);
		  return new Comment[]{};
		}
	}
	
  public Comment getCommentByNode(Node commentNode) throws Exception {
  	Comment comment = new Comment();
		comment.setId(commentNode.getName()) ;
		if(commentNode.hasProperty("exo:comments")) comment.setComments((commentNode.getProperty("exo:comments").getString())) ;
		if(commentNode.hasProperty("exo:commentBy")) comment.setCommentBy((commentNode.getProperty("exo:commentBy").getString())) ;  
		if(commentNode.hasProperty("exo:fullName")) comment.setFullName((commentNode.getProperty("exo:fullName").getValue().getString())) ;
		if(commentNode.hasProperty("exo:dateComment")) comment.setDateComment((commentNode.getProperty("exo:dateComment").getDate().getTime())) ;
		if(commentNode.hasProperty("exo:postId")) comment.setPostId(commentNode.getProperty("exo:postId").getString()) ;
  	return comment;
  }
  
  /**
   * Gets the category.
   * 
   * @param categoryNode the category node
   * 
   * @return the category
   * 
   * @throws Exception the exception
   */
  private Category getCategory(Node categoryNode) throws Exception {
  	Category cat = new Category() ;
  	cat.setId(categoryNode.getName()) ;
  	if(categoryNode.hasProperty("exo:name")) cat.setName(categoryNode.getProperty("exo:name").getString()) ;
  	if(categoryNode.hasProperty("exo:description")) cat.setDescription(categoryNode.getProperty("exo:description").getString()) ;
  	if(categoryNode.hasProperty("exo:createdDate")) cat.setCreatedDate(categoryNode.getProperty("exo:createdDate").getDate().getTime()) ;
  	if(categoryNode.hasProperty("exo:moderators")) cat.setModerators(ValuesToStrings(categoryNode.getProperty("exo:moderators").getValues())) ;
  	if(categoryNode.hasProperty("exo:userPrivate")) cat.setUserPrivate(ValuesToStrings(categoryNode.getProperty("exo:userPrivate").getValues())) ;
  	if(categoryNode.hasProperty("exo:isModerateQuestions")) cat.setModerateQuestions(categoryNode.getProperty("exo:isModerateQuestions").getBoolean()) ;
  	String path = categoryNode.getPath() ;
		cat.setPath(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1)) ;
  	return cat;
  }
  
  private List<Watch> getWatchs(Node watchNode, boolean isGetAll, int start, int max) throws Exception {
  	Watch watch = new Watch();
  	List<Watch> listWatches = new ArrayList<Watch>();
  	String[] userWatch = null;
		String[] emails = null;
		String[] RSS = null;
  	if(watchNode.hasProperty("exo:emailWatching")) emails = ValuesToStrings(watchNode.getProperty("exo:emailWatching").getValues());
  	if(watchNode.hasProperty("exo:userWatching")) userWatch = ValuesToStrings(watchNode.getProperty("exo:userWatching").getValues());
  	if(watchNode.hasProperty("exo:rssWatching")) RSS = ValuesToStrings(watchNode.getProperty("exo:rssWatching").getValues());
  	if(userWatch != null)
  		if(isGetAll) {
  			start = 0;
  			max = userWatch.length;
  		} else {
  			max += start;
  			setAvailablePage(userWatch.length) ;
  		}
	  	for(int i = start; i < max && i < userWatch.length; i ++){
	  		watch = new Watch();
	  		watch.setEmails(emails[i]);
	  		watch.setUser(userWatch[i]);
	  		listWatches.add(watch);
	  	}
  	return listWatches;
  }
  
  /**
   * Convert all values of a node's property from type Property to String and
   * return them in to string array. These values will be setted for a question object
   * 
   * @param Val         list values of node's property
   * 
   * @return            values of node's property after convert form type Property to String
   * 
   * @throws Exception  if an valueFormat or Repository exception occur
   */
  private String [] ValuesToStrings(Value[] Val) throws Exception {
  	if(Val.length == 1)
  		return new String[]{Val[0].getString()};
		String[] Str = new String[Val.length];
		for(int i = 0; i < Val.length; ++i) {
		  Str[i] = Val[i].getString();
		}
		return Str;
  }
  
  private long [] ValuesToLong(Value[] Val) throws Exception {
  	if(Val.length < 1) return new long[]{0} ;
  	long[] d = new long[Val.length] ;
  	for(int i = 0; i < Val.length; ++i) {
  		d[i] = Val[i].getLong() ;
  	}
  	return d;
  }
  
  /**
   * Get all nodes is stored in a NodeIterator and sort them by FAQSetting,
   * with each node, system get all values of this node and set them into
   * a question object, an this question object is push into a list questions.
   * 
   * @return              list questions
   * 
   * @throws  Exception   if query or repository or path of node is occur Exception
   * @throws Exception the exception
   * 
   * @see                 Question
   * @see                 List
   */
	public List<Question> getAll() throws Exception { 
    if(iter_ == null || !iter_.hasNext()) {
      Session session = getJCRSession() ;
      if(isQuery_) {
        QueryManager qm = session.getWorkspace().getQueryManager() ;
        Query query = qm.createQuery(value_, Query.XPATH);
        QueryResult result = query.execute();
        iter_ = result.getNodes();
      } else {
        Node node = (Node)session.getItem(value_) ;
        iter_ = node.getNodes() ;
      }
      closeSession();
    }
    
    List<Question> questions = new ArrayList<Question>();
    while (iter_.hasNext()) {
      Node questionNode = iter_.nextNode();
      questions.add(getQuestion(questionNode));
    }
    iter_ = null;
    return questions; 
  }
	
	public List<Watch> getAllWatch() throws Exception {
		List<Watch> listWatches = new ArrayList<Watch>();
		if(iter_ == null) {
      Session session = getJCRSession() ;
      if(isQuery_) {
        QueryManager qm = session.getWorkspace().getQueryManager() ;
        Query query = qm.createQuery(value_, Query.XPATH);
        QueryResult result = query.execute();
        iter_ = result.getNodes();
      } else {
        Node node = (Node)session.getItem(value_) ;
        iter_ = node.getNodes() ;
      }
      iter_ = null;
      closeSession();
    }
		Node watchNode = null;
    while (iter_.hasNext()) {
      watchNode = iter_.nextNode();
      listWatches.addAll(getWatchs(watchNode, true, 0, 0));
    }
		return listWatches;
	}
	
  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.JCRPageList#setList(java.util.List)
   */
  public void setList(List<Question> contacts) { }
  
  @SuppressWarnings("deprecation")
  /**
   * Get a Session from Portal container, and this session is used to set a query
   * 
   * @return          an session object
   * 
   * @throw Exception if repository config occur exception
   *                  or if repository occur exception
   *                  or if login or workspace occur exception
   *                  
   * @see             Session
   */
  private Session getJCRSession() throws Exception {
    return sessionManager.openSession();
  }

  private void closeSession() throws Exception {
    sessionManager.closeSession();
  }
  
}
