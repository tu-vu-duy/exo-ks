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
package org.exoplatform.faq.service.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.CategoryInfo;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQEventQuery;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.ObjectSearchResult;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.service.Watch;
import org.exoplatform.faq.service.impl.JCRDataStorage;
import org.exoplatform.faq.test.FAQServiceTestCase;
import org.exoplatform.ks.common.bbcode.BBCode;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 3, 2008  
 */


@SuppressWarnings("unused")
public class TestFAQService extends FAQServiceTestCase{
	private FAQService faqService_ ;
	private FAQSetting faqSetting_ = new FAQSetting();
	private SessionProvider sProvider_ ;
	private List<FileAttachment> listAttachments = new ArrayList<FileAttachment>() ;

	private static String  USER_ROOT = "root";
  private static String  USER_JOHN = "john";
	private static String  USER_DEMO = "demo";
	private static String  categoryId1;
	private static String  categoryId2;
	private static String  questionId1;
	private static String  questionId2;
	private static String  questionId3;
	private static String  questionId4;
	private static String  questionId5;
	private JCRDataStorage datastorage;

	public TestFAQService() throws Exception {
		super();
	}

	public void setUp() throws Exception {
		super.setUp();
		faqService_ = (FAQService) container.getComponentInstanceOfType(FAQService.class);
		datastorage = (JCRDataStorage) container.getComponentInstanceOfType(JCRDataStorage.class);
		SessionProviderService sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class) ;
		sProvider_ = sessionProviderService.getSystemSessionProvider(null) ;
		faqSetting_.setDisplayMode("both");
		faqSetting_.setOrderBy("created");
		faqSetting_.setOrderType("asc") ;
		faqSetting_.setSortQuestionByVote(true);
		faqSetting_.setIsAdmin("TRUE");
		faqSetting_.setEmailMoveQuestion("content email move question");
	}

	public void testFAQService() throws Exception {
		assertNotNull(faqService_) ;
		assertNotNull(sProvider_) ;
	}

	public Category createCategory(String categoryName) {
		Date date = new Date() ;
		Category category = new Category() ;
		category.setName(categoryName) ;
		category.setDescription("Description") ;
		category.setModerateQuestions(true) ;
		category.setModerateAnswers(true);
		category.setViewAuthorInfor(true);
		category.setModerators(new String[]{"root"}) ;
		category.setCreatedDate(date) ;
		return category ;
	}

	public Question createQuestion(String cateId) throws Exception {
		Question question = new Question() ;
		question.setLanguage("English") ;
		question.setQuestion("What is FAQ?");
		question.setDetail("Add new question 1") ;
		question.setAuthor("root") ;
		question.setEmail("maivanha1610@gmail.com") ;
		question.setActivated(true) ;
		question.setApproved(true) ;
		question.setCreatedDate(new Date()) ;
		question.setCategoryId(cateId) ;
		question.setRelations(new String[]{}) ;
		question.setAttachMent(listAttachments) ;
		question.setAnswers(new Answer[]{});
		question.setComments(new Comment[]{});
		question.setUsersVote(new String[]{});
		question.setMarkVote(0.0);
		question.setUsersWatch(new String[]{});
		question.setEmail(new String());
		question.setEmailsWatch(new String[]{});
		question.setTopicIdDiscuss(null);
		return question ;
	}

	private QuestionLanguage createQuestionLanguage(String language){
		QuestionLanguage questionLanguage = new QuestionLanguage();
		questionLanguage.setAnswers(null);
		questionLanguage.setComments(null);
		questionLanguage.setDetail("detail for language " + language);
		questionLanguage.setLanguage(language);
		questionLanguage.setQuestion("question for language " + language);
		return questionLanguage;
	}

	private Answer createAnswer(String user, String content){
		Answer answer = new Answer();
		answer.setActivateAnswers(true);
		answer.setApprovedAnswers(true);
		answer.setDateResponse(new Date());
		answer.setMarksVoteAnswer(0);
		answer.setMarkVotes(0);
		answer.setNew(true);
		answer.setPostId(null);
		answer.setResponseBy(user);
		answer.setResponses(content);
		answer.setUsersVoteAnswer(null);
		answer.setLanguage("English");
		return answer;
	}

	private Comment createComment(String user, String content){
		Comment comment = new Comment();
		comment.setCommentBy(user);
		comment.setComments(content);
		comment.setDateComment(new Date());
		comment.setNew(true);
		comment.setPostId(null);
		comment.setFullName(user + " " + user);
		return comment;
	}

	private Watch createNewWatch(String user, String mail){
		Watch watch = new Watch();
		watch.setUser(user);
		watch.setEmails(mail);
		return watch;
	}
	
	private BBCode createBBCode(String tag, String replacement, boolean isActive) {
  	BBCode bbCode = new BBCode();
  	bbCode.setTagName(tag);
  	bbCode.setActive(isActive);
  	bbCode.setDescription("Description!");
  	bbCode.setExample("["+tag+"] text example [/"+tag+"]");
  	bbCode.setOption(false);
  	bbCode.setReplacement(replacement);
  	return bbCode;
  }
	
	private FileAttachment createUserAvatar(String fileName) throws Exception{
		FileAttachment attachment = new FileAttachment();
		try {
			File file =  new File("../service/src/test/java/conf/portal/defaultAvatar.jpg");
			attachment.setName(fileName);
			InputStream is = new FileInputStream(file);
			attachment.setInputStream(is);
			attachment.setMimeType("");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return attachment;
	}


	private void defaultData() throws Exception {
	//Create category Home.
		faqService_.getAllCategories();
		//Create some category default
		Category cate = createCategory("Category to test question") ;
		categoryId1 =  Utils.CATEGORY_HOME + "/" + cate.getId();
		Category cate2 = createCategory("Category 2 to test question") ;
		categoryId2 =  Utils.CATEGORY_HOME + "/" + cate2.getId();
		faqService_.saveCategory(Utils.CATEGORY_HOME, cate, true) ;
		faqService_.saveCategory(Utils.CATEGORY_HOME, cate2, true) ;

		Question question1 = createQuestion(categoryId1) ;
		questionId1 = question1.getId();
		Question question2 = createQuestion(categoryId1) ;
		question2.setRelations(new String[]{}) ;
		question2.setLanguage("English") ;
		question2.setAuthor("root") ;
		question2.setEmail("truong_tb1984@yahoo.com") ;
		question2.setDetail("Nguyen van truong test question 2222222 ?") ;
		question2.setCreatedDate(new Date()) ;
		questionId2 = question2.getId();
		
		Question question3 = createQuestion(categoryId1) ;
		question3.setRelations(new String[]{}) ;
		question3.setLanguage("English") ;
		question3.setAuthor("Phung Hai Nam") ;
		question3.setEmail("phunghainam@yahoo.com") ;
		question3.setDetail("Nguyen van truong test question 33333333 nguyenvantruong ?") ;
		question3.setCreatedDate(new Date()) ;
		questionId3 = question3.getId();

		Question question4 = createQuestion(categoryId1) ;
		question4.setRelations(new String[]{}) ;
		question4.setLanguage("English") ;
		question4.setAuthor("Pham Dinh Tan") ;
		question4.setEmail("phamdinhtan@yahoo.com") ;
		question4.setDetail("Nguyen van truong test question nguyenvantruong ?") ;
		question4.setCreatedDate(new Date()) ;
		questionId4 = question4.getId();

		Question question5 = createQuestion(categoryId1) ;
		question5.setRelations(new String[]{}) ;
		question5.setLanguage("English") ;
		question5.setAuthor("Ly Dinh Quang") ;
		question5.setEmail("lydinhquang@yahoo.com") ;
		question5.setDetail("Nguyen van truong test question 55555555555 ?") ;
		question5.setCreatedDate(new Date()) ;
		questionId5 = question5.getId();
		
//		save questions
		faqService_.saveQuestion(question1, true,faqSetting_) ;
		faqService_.saveQuestion(question2, true,faqSetting_) ;
		faqService_.saveQuestion(question3, true,faqSetting_) ;
		faqService_.saveQuestion(question4, true,faqSetting_) ;
		faqService_.saveQuestion(question5, true,faqSetting_) ;
	}
	
	public void testCategory() throws Exception {
	// remove Data before testing category.
		faqService_.removeCategory(Utils.CATEGORY_HOME);
//		add category Id	
		faqService_.getAllCategories();
		Category cate1 = createCategory("Cate 1") ;
		cate1.setIndex(1);
		faqService_.saveCategory(Utils.CATEGORY_HOME, cate1, true) ;

		Category cate2 = createCategory("Cate 2") ;
		cate2.setIndex(2);
		cate2.setName("Nguyen van truong test category222222") ;
		cate2.setModerators(new String[]{"Demo"}) ;
		faqService_.saveCategory(Utils.CATEGORY_HOME, cate2, true) ;
		
//	add sub category 1
		Category subCate1 = createCategory("Sub Cate 1") ;
		subCate1.setIndex(1);
		subCate1.setName("Nguyen van truong test Sub category 1") ;
		subCate1.setModerators(new String[]{"marry","Demo"}) ;
		faqService_.saveCategory(Utils.CATEGORY_HOME+"/"+cate1.getId(), subCate1, true) ;
		
//	is Category Exist	
		assertEquals("Category has name:"+cate1.getName()+"  is no longer exists.", faqService_.isCategoryExist(cate1.getName(), Utils.CATEGORY_HOME), true);
//		Get category by id
		cate1 = faqService_.getCategoryById(Utils.CATEGORY_HOME+"/"+cate1.getId());
		assertNotNull("Category have not been added", cate1) ;
		
//		Check category is already exist
		assertEquals("This category is't already exist", faqService_.isExisting(cate1.getPath()), true);
		
//		get infor of root category:
		assertEquals("Have two categories in root category", faqService_.getCategoryInfo(Utils.CATEGORY_HOME, faqSetting_)[0], 2);

//		Get path of category
		assertNotNull("Path of category node is null", faqService_.getCategoryPath(cate1.getPath()));

		//		Swap 2 category
		cate2 = faqService_.getCategoryById(Utils.CATEGORY_HOME+"/"+cate2.getId());
		assertEquals("Index of category 1 before swap is't 2", cate1.getIndex(), 1);
		assertEquals("Index of category 2 before swap is't 1", cate2.getIndex(), 2);
		faqService_.swapCategories(cate1.getPath(), cate2.getPath());
		cate1 =  faqService_.getCategoryById(cate1.getPath());
		cate2 =  faqService_.getCategoryById(cate2.getPath());
		assertEquals("Index of category 1 after swap is't 1", cate1.getIndex(), 2);
		assertEquals("Index of category 2 after swap is't 2", cate2.getIndex(), 1);

//		update category 
		cate1.setName("Nguyen van truong test category111111") ;
		cate1.setCreatedDate(new Date()) ;
		faqService_.saveCategory(Utils.CATEGORY_HOME, cate1, false);
		cate1 =  faqService_.getCategoryById(cate1.getPath());
		assertEquals("Name of category 1 haven't been changed", "Nguyen van truong test category111111", cate1.getName());

//		get Categories
		List<Category> listCate = faqService_.getSubCategories(Utils.CATEGORY_HOME, faqSetting_, true, null) ;
		assertEquals("In root category don't have two subcategories", listCate.size(), 2) ;
		
//		Get Maxindex of cateogry
		assertEquals("Root have two category and maxIndex of subcategories in root is't 2", 
									faqService_.getMaxindexCategory(Utils.CATEGORY_HOME), 2);

//		get sub category
		List<Category> listSubCate = faqService_.getSubCategories(cate1.getPath(), faqSetting_, false, null) ;
		assertEquals("Category 1 not only have one subcategory", listSubCate.size(), 1) ;

//		update sub category 
		subCate1 = listSubCate.get(0);
		subCate1.setName("Sub category 1") ;
		faqService_.saveCategory(cate1.getPath(), subCate1, false);
		assertEquals("Name of SubCategory 1 have not been changed from \"Sub Cate 1\" to \"Sub category 1\"", 
									"Sub category 1", subCate1.getName());

//		get all Category 
		List<Category> listAll = faqService_.getAllCategories() ;
		assertEquals("In FAQ System have less than 3 categories", listAll.size(), 3) ;

//		move category 
		faqService_.moveCategory(cate2.getPath(), cate1.getPath()) ;
		cate2 = faqService_.getCategoryById(cate1.getPath()+"/"+cate2.getId());
		assertNotNull("Category 2 is not already exist in FAQ", cate2) ;

//		Delete category 2
		faqService_.removeCategory(cate2.getPath()) ;
		List<Category> listAllAfterRemove = faqService_.getAllCategories() ;
		assertEquals("Category 2 have not been removed, in system have more than 2 categoies", listAllAfterRemove.size(), 2) ;

//		get list category by moderator
		List<String> listCateByModerator = faqService_.getListCateIdByModerator(USER_ROOT);
		assertEquals("User Root is't moderator of category Home and cate1", listCateByModerator.size(), 2);
		// remove Data when tested category
		faqService_.removeCategory(Utils.CATEGORY_HOME);
	}
// FAQPortlet
	public void testCategoryInfo() throws Exception {
//	Add new data default
		defaultData();
//	Get categoryInfo
		List<String> categoryIdScoped = new ArrayList<String>();
		CategoryInfo categoryInfo = faqService_.getCategoryInfo(Utils.CATEGORY_HOME, categoryIdScoped);
		assertEquals("Can not get info of category by categoryInfo.", categoryInfo.getSubCateInfos().size(), 2);
//		get QuestionInfo
		categoryIdScoped = new ArrayList<String>();
		categoryInfo = faqService_.getCategoryInfo(categoryId1, categoryIdScoped);
		assertEquals("Can not questionInfo  of category.", categoryInfo.getQuestionInfos().size(), 5);
//	 remove Data when tested category
		faqService_.removeCategory(Utils.CATEGORY_HOME);
  }
	
	public void testQuestion() throws Exception {
//		Add new data default
		defaultData();
//		get question 1
		String questionId = categoryId1 + "/" + Utils.QUESTION_HOME + "/" + questionId1;
		String qsId2 = categoryId1 + "/" + Utils.QUESTION_HOME + "/" + questionId2;
		Question question1 = faqService_.getQuestionById(questionId);
		assertNotNull("Question 1 have not been saved into data", question1) ;
		List<Question> listQuestion = faqService_.getQuestionsNotYetAnswer(Utils.CATEGORY_HOME, false).getAll() ;
		assertEquals("have some questions are not yet answer", listQuestion.size(), 0) ; 

//		update question 1
		question1.setDetail("Nguyen van truong test question 11111111 ?") ;
		faqService_.saveQuestion(question1, false,faqSetting_) ;
		assertNotNull(question1) ;
		assertEquals("Detail of question 1 have not been changed",
									"Nguyen van truong test question 11111111 ?", question1.getDetail());

//  update Question Relatives
		faqService_.updateQuestionRelatives(questionId, new String[]{qsId2});
		question1 = faqService_.getQuestionById(questionId);
		assertNotNull("Question not save relatives ", faqService_.getQuestionById(question1.getRelations()[0]));
//		move question 2 to category 2
		Category cate2 = faqService_.getCategoryById(categoryId2);
		List<String> listId = new ArrayList<String>() ;
		listId.add(qsId2);
		assertEquals("Category 2 have some questions before move question 2", 
							faqService_.getQuestionsByCatetory(cate2.getPath(), faqSetting_).getAll().size(), 0);
		faqService_.moveQuestions(listId, cate2.getPath(), "", faqSetting_);
		assertEquals("Category 2 have more than one question after move question 2", 
								faqService_.getQuestionsByCatetory(cate2.getPath(), faqSetting_).getAll().size(), 1);
		
//	Get question by list category
		listId = new ArrayList<String>() ;
		listId.add(categoryId1.replace(Utils.CATEGORY_HOME, ""));
		JCRPageList pageList = faqService_.getQuestionsByListCatetory(listId, false);
		pageList.setPageSize(10);
//		assertEquals("Can't move question 2 to category 2", pageList.getPage(1, "root").size(), 4);
//		get list all question
		List<Question> listAllQuestion = faqService_.getAllQuestions().getAll();
		assertEquals("the number of categories in FAQ is not 5", listAllQuestion.size(), 5) ;

//		get list question by category of question 1
		List<Question> listQuestionByCategory = faqService_.getQuestionsByCatetory(categoryId1, faqSetting_).getAll() ;
		assertEquals("the number of question in category which contain question 1 is not 4", listQuestionByCategory.size(), 4) ;

//		Get list paths of all question in category - removed
//		List<String> listPaths = faqService_.getListPathQuestionByCategory(cate.getId());
//		assertEquals("In Category 1 have more than 4 questions, because can't move question 2 to category 2", listPaths.size(), 4);

//		Get question node by id - removed
		//assertNotNull("Question1 is not already existing in system", faqService_.getQuestionNodeById(question1.getId()));

//		remove question
		faqService_.removeQuestion(categoryId1 + "/" + Utils.QUESTION_HOME + "/" + questionId5);
		List<Question> listAllQuestionAfterRemove = faqService_.getAllQuestions().getAll();
		assertEquals("Question 5 have not been removed, in system have 5 questions", listAllQuestionAfterRemove.size(), 4) ;
	// remove Data when tested question
		faqService_.removeCategory(Utils.CATEGORY_HOME);
	}

	public void testSearch() throws Exception {
//		set Data default
		defaultData();
		
		FAQEventQuery eventQueryCategory = new FAQEventQuery() ;

//		quick search with text = "test"
		eventQueryCategory.setText("test");
		eventQueryCategory.setType("categoryAndQuestion");
		eventQueryCategory.setAdmin(true);
		List<ObjectSearchResult> listQuickSearch = faqService_.getSearchResults(eventQueryCategory) ;
		assertEquals("Can't get all questions have \"test\" charaters in content", listQuickSearch.size(), 4) ;// 1 category and 4 question

//		advance search all category in database - removed
		//FAQEventQuery eventQueryCategory = new FAQEventQuery() ;
		//eventQueryCategory.setType("faqCategory");
		//List<Category> listAllCategroy = faqService_.getAdvancedSearchCategory(sProvider_, eventQueryCategory) ;
		//assertEquals("In System don't have 4 categories", listAllCategroy.size(), 4) ;

//		advance search with category name = "Sub" - removed
		FAQEventQuery eventQuerySub = new FAQEventQuery() ;
		eventQuerySub.setType("faqCategory");
		eventQuerySub.setName("Sub") ;
		//List<Category> listAllSub = faqService_.getAdvancedSearchCategory(sProvider_, eventQuerySub) ;
		//assertEquals("don't Have any cateogry which have \"Sub\" charater in name", listAllSub.size(), 1) ;

//		advance search all question in database - removed
		FAQEventQuery eventQueryQuestion = new FAQEventQuery() ;
		eventQueryQuestion.setType("faqQuestion");
		//List<Question> listAllQuestion = faqService_.getAdvancedSearchQuestion(sProvider_, eventQueryQuestion) ;
		//assertEquals(listAllQuestion.size(), 0) ;


//		advance search with category name = "Sub" - removed
		FAQEventQuery eventQueryAdvanceQuestion = new FAQEventQuery() ;
		eventQueryAdvanceQuestion.setType("faqQuestion");
		eventQueryAdvanceQuestion.setQuestion("nguyenvantruong") ;
		//List<Question> listSearchAdvanceQuestion = faqService_.getAdvancedSearchQuestion(sProvider_, eventQueryAdvanceQuestion) ;
		//assertEquals("the number of questions which have \"nguyenvantruong\" in question content is not 2", 
									//listSearchAdvanceQuestion.size(), 2) ;
		
	// remove Data when tested search
		faqService_.removeCategory(Utils.CATEGORY_HOME);
	}

	public void testAnswer() throws Exception{
//	set data default
		defaultData() ;
		// create Answer
		Answer answer1 = createAnswer(USER_ROOT, "Root answer 1 for question");
		Answer answer2 = createAnswer(USER_DEMO, "Demo answer 2 for question");
		
//		Save answer:
		String questionId = categoryId1 + "/" + Utils.QUESTION_HOME + "/" + questionId1;
		faqService_.saveAnswer(questionId, new Answer[]{answer1, answer2});

//		Get answer by id:
		assertNotNull("Answer 2 have not been added", faqService_.getAnswerById(questionId, answer2.getId()));

//		Update answers:
		assertEquals(answer1.getResponses(), "Root answer 1 for question");
		String content = "Root answer 1 for question edit";
		answer1.setResponses(content);
		faqService_.saveAnswer(questionId, answer1, false);
		assertEquals("Content of Answer have not been changed to \"Root answer 1 for question edit\"", 
								faqService_.getAnswerById(questionId, answer1.getId()).getResponses(), content);

//		Get all answers of question:
		JCRPageList pageList = faqService_.getPageListAnswer(questionId, null);
		pageList.setPageSize(10);
		assertEquals("Question have 2 answers", pageList.getPageItem(0).size(), 2);

//		Delete answer
		faqService_.deleteAnswer(questionId, answer1.getId());
		pageList = faqService_.getPageListAnswer(questionId, null);
		pageList.setPageSize(10);
		assertEquals("Answer 1 have not been removed, question only have one answer", pageList.getPageItem(0).size(), 1);

		// remove Data when tested answer
		faqService_.removeCategory(Utils.CATEGORY_HOME);
	}
	
	public void testComment() throws Exception{
//	set default data
		defaultData();
		Comment comment1 = createComment(USER_ROOT, "Root comment 1 for question");
		Comment comment2 = createComment(USER_DEMO, "Demo comment 2 for question");
//		Save comment
		String questionId = categoryId1 + "/" + Utils.QUESTION_HOME + "/" + questionId1;
		faqService_.saveComment(questionId, comment1, true);
		faqService_.saveComment(questionId, comment2, true);

//		Get comment by Id:
		assertNotNull("Comment 1 have not been added ", faqService_.getCommentById(questionId, comment1.getId()));
		assertNotNull("Comment 1 have not been added ", faqService_.getCommentById(questionId, comment2.getId()));

//		Get all comment of question
		JCRPageList pageList = faqService_.getPageListComment(questionId);
		pageList.setPageSize(10);
		assertEquals("Question have two comments", pageList.getPageItem(0).size(), 2);

//		Delete comment by id
		faqService_.deleteComment(questionId, comment1.getId());
		pageList = faqService_.getPageListComment(questionId);
		pageList.setPageSize(10);
		assertEquals("Comment 1 is not removed", pageList.getPageItem(0).size(), 1);
		
	// remove Data when tested comment
		faqService_.removeCategory(Utils.CATEGORY_HOME);
	}

	public void testImportData() throws Exception{
		faqService_.getAllCategories();
//		Before import data, number question is 0
		assertEquals("Before import data, number question is not 0", faqService_.getAllQuestions().getAvailable(), 0);
		try {
			File file = new File("../service/src/test/java/conf/portal/Data.xml");
		  String content = FileUtils.readFileToString(file, "UTF-8");
			byte currentXMLBytes[] = content.getBytes();
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(currentXMLBytes);
			faqService_.importData(Utils.CATEGORY_HOME, byteArrayInputStream, false) ;
		} catch (IOException e) {
			e.printStackTrace();
		}
//		After imported data, number questions is 5
		assertEquals("Before import data, number question is not 5", faqService_.getAllQuestions().getAvailable(), 5);
	// remove Data when tested comment
		faqService_.removeCategory(Utils.CATEGORY_HOME);
	}

	public void testWatchCategory() throws Exception {
//	add default data
		defaultData();
		List<Watch> listWatchs = new ArrayList<Watch>();
//		add  watch
		faqService_.addWatchCategory(categoryId1, createNewWatch(USER_ROOT, "maivanha1610@gmail.com")) ;
		faqService_.addWatchCategory(categoryId1, createNewWatch(USER_DEMO, "maivanha1610@yahoo.com")) ;
		faqService_.addWatchCategory(categoryId1, createNewWatch(USER_JOHN, "john@localhost.com")) ;

//	Check hasWatch of category
		assertEquals("This category has not watch.", faqService_.hasWatch(categoryId1), true);
//	check get All watch in category.
		assertEquals("Size of all watch in this category is not 3", faqService_.getWatchByCategory(categoryId1).size(), 3);
//		Check category is watched by user
		assertEquals("User root didn't watch this category", faqService_.isUserWatched(USER_ROOT, categoryId1), true);

//		get all categories are watched by user
		assertEquals("user root have not watched some categories", faqService_.getWatchedCategoryByUser(USER_ROOT).getAvailable(), 1);
		
//		Check unWatch Category by user
		faqService_.unWatchCategory(categoryId1, USER_ROOT);		
		assertEquals("User root has watching this category", faqService_.isUserWatched(USER_ROOT, categoryId1), false);
	// remove Data when tested comment
		faqService_.removeCategory(Utils.CATEGORY_HOME);
	}

  public void testQuestionMultilanguage() throws Exception{
//		set data default
		defaultData();
//		Add question language for question
		String questionId = categoryId1 + "/" + Utils.QUESTION_HOME + "/" + questionId1;
		faqService_.addLanguage(questionId, createQuestionLanguage("VietNam"));
		faqService_.addLanguage(questionId, createQuestionLanguage("French"));
//		Get all question language (it is 3: English(default), VietNam and French):
		List<QuestionLanguage> questionLanguages = faqService_.getQuestionLanguages(questionId);
		assertEquals("Language of this question is not 3", questionLanguages.size(), 3);
//		Get Question_language by language
		QuestionLanguage questionLanguage = faqService_.getQuestionLanguageByLanguage(questionId, "VietNam");
		assertNotNull("QuestionLanguage is Null.", questionLanguage);
//		add answer1 in question language by questionLanguage
		Answer answer = createAnswer(USER_ROOT, "Answer of language VietNam 1");
		String answerId = answer.getId();
		answer.setLanguage("VietNam");
		questionLanguage.setAnswers(new Answer[]{answer});
		faqService_.saveAnswer(questionId, questionLanguage);
		assertNotNull("Answer1 in question language is not save.", faqService_.getAnswerById(questionId, answerId, "VietNam"));
		
//		add answer2 in question language by answer
		answer = createAnswer(USER_ROOT, "Answer of language VietNam 2");
		answerId = answer.getId();
		answer.setLanguage("VietNam");
		faqService_.saveAnswer(questionId, answer, "VietNam");
		assertNotNull("Answer2 in question language is not save.", faqService_.getAnswerById(questionId, answerId, "VietNam"));
		
//	add comment in question language
		Comment comment = createComment(USER_ROOT, "New comment of question language");
		String commentId = comment.getId();
		comment.setNew(true);
		faqService_.saveComment(questionId, comment, "VietNam");
		assertNotNull("Comment in question language is not save.", faqService_.getCommentById(questionId, commentId, "VietNam"));
//		Delete answer in question language.
		faqService_.deleteAnswerQuestionLang(questionId, answerId, "VietNam");
		assertNull("Answer2 in question language is not deleted.", faqService_.getAnswerById(questionId, answerId, "VietNam"));
//		Delete comment in question language.
		faqService_.deleteCommentQuestionLang(questionId, commentId, "VietNam");
		assertNull("Comment in question language is not deleted.", faqService_.getCommentById(questionId, commentId, "VietNam"));
	}
	
	public void testUserSetting() throws Exception {
//		save userSetting information into user node
		faqSetting_.setDisplayMode("both");
		faqSetting_.setOrderBy("created");
		faqSetting_.setOrderType("asc") ;
		assertEquals("All data is not sorted by created date", faqSetting_.getOrderBy(), "created");
		assertEquals("Data is not sorted asc", faqSetting_.getOrderType(), "asc");
		faqService_.saveFAQSetting(faqSetting_, USER_ROOT);

//		get all userSetting information from user node and set for FAQSetting object
		FAQSetting setting = new FAQSetting();
		setting.setOrderBy(null);
		setting.setOrderType(null);
		assertNull("Set order by is not null before get user Setting", setting.getOrderBy());
		assertNull("Set order type is not null before get user setting", setting.getOrderType());
		faqService_.getUserSetting(USER_ROOT, setting);
		assertEquals("Get setting of user,data is not order by created date", setting.getOrderBy(), "created");
		assertEquals("Get setting of user,data is not order asc", setting.getOrderType(), "asc");

//		update userSetting information in to user node
		setting.setSortQuestionByVote(false);
		setting.setOrderBy("alpha");
		setting.setOrderType("des");
		faqService_.saveFAQSetting(setting, USER_ROOT);
		assertEquals("user setting before save,do not order by created date", faqSetting_.getOrderBy(), "created");
		assertEquals("user setting before save,do not order asc", faqSetting_.getOrderType(), "asc");
		faqService_.getUserSetting(USER_ROOT, faqSetting_);
		assertEquals("user setting after saved,do not order by created alphabet", faqSetting_.getOrderBy(), "alpha");
		assertEquals("user setting before saveddo ,do not order des", faqSetting_.getOrderType(), "des");
		
//	Get all admins of FAQ
		List<String> list = faqService_.getAllFAQAdmin();
		assertNotNull(list);
		assertEquals("User demo is addmin of FAQ System", faqService_.isAdminRole(USER_DEMO), false);
/*
//	Test send mail for user:
		Message  message = new Message(); 
    message.setMimeType("text/htm") ;
    message.setFrom("maivanha1610@yahoo.com") ;
    message.setTo("maivanha1610@gmail.com") ;
    message.setSubject("Test send mail") ;
    message.setBody("run JUnit test") ;
    try {
    	faqService_.sendMessage(message) ;
    } catch(Exception e) {
    	e.printStackTrace();
    }*/
	}
	
	public void testUserAvatar()throws Exception{
		//	Add new avatar for user:
		faqService_.saveUserAvatar(USER_ROOT, createUserAvatar("rootAvatar"));
		
		//	Get user avatar 
		assertNotNull(faqService_.getUserAvatar(USER_ROOT));
		
		//	Set default avartar for user
		faqService_.setDefaultAvatar(USER_ROOT);
		assertNull(faqService_.getUserAvatar(USER_ROOT));
	}

}