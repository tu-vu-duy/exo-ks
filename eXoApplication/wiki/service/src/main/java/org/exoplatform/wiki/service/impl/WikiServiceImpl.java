package org.exoplatform.wiki.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.wiki.mow.api.Model;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.MOWService;
import org.exoplatform.wiki.mow.core.api.WikiStoreImpl;
import org.exoplatform.wiki.mow.core.api.content.ContentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.GroupWiki;
import org.exoplatform.wiki.mow.core.api.wiki.MovedMixin;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PortalWiki;
import org.exoplatform.wiki.mow.core.api.wiki.RemovedMixin;
import org.exoplatform.wiki.mow.core.api.wiki.Trash;
import org.exoplatform.wiki.mow.core.api.wiki.UserWiki;
import org.exoplatform.wiki.mow.core.api.wiki.WikiContainer;
import org.exoplatform.wiki.mow.core.api.wiki.WikiHome;
import org.exoplatform.wiki.mow.core.api.wiki.WikiImpl;
import org.exoplatform.wiki.resolver.TitleResolver;
import org.exoplatform.wiki.service.BreadcumbData;
import org.exoplatform.wiki.service.SearchData;
import org.exoplatform.wiki.service.SearchResult;
import org.exoplatform.wiki.service.Space;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.utils.Utils;
import org.xwiki.rendering.syntax.Syntax;

public class WikiServiceImpl implements WikiService{
  
  final static private String USERS_PATH = "usersPath";
  
  final static private String GROUPS_PATH = "groupsPath";

  final static private String USER_APPLICATION = "userApplicationData";
  
  final static private String GROUP_APPLICATION = "groupApplicationData";
  
  private NodeHierarchyCreator nodeCreator ;
  private JCRDataStorage jcrDataStorage ;
  private static final Log log = ExoLogger.getLogger(WikiServiceImpl.class);
  
  public WikiServiceImpl(NodeHierarchyCreator creator, JCRDataStorage jcrDataStorage) {
    nodeCreator = creator ;
    this.jcrDataStorage = jcrDataStorage ;
  }
  
  public Page createPage(String wikiType, String wikiOwner, String title, String parentId) throws Exception {
    
    Model model = getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
   
    WikiImpl wiki = (WikiImpl) getWiki(wikiType, wikiOwner, model);
    
    PageImpl page = wiki.createWikiPage() ;
    
    PageImpl parentPage = null ;
    String statement = getStatement(wikiType, wikiOwner, parentId) ;
    parentPage = searchPage(statement, wStore.getSession());
    if(parentPage == null) throw new Exception() ;
    
    String pageId = TitleResolver.getPageId(title, false);
    page.setName(pageId) ;
    parentPage.addWikiPage(page) ;
    ConversationState conversationState = ConversationState.getCurrent();
    String creator = null;
    if (conversationState != null && conversationState.getIdentity() != null) {
      creator = conversationState.getIdentity().getUserId();
    }
    page.setOwner(creator);
    page.getContent().setTitle(title);
    page.makeVersionable();
    page.setSession(wStore.getSession());
    model.save();    
    return page ;
  }
  
  public boolean deletePage(String wikiType, String wikiOwner, String pageId) throws Exception {
    if(WikiNodeType.Definition.WIKI_HOME_NAME.equals(pageId) || pageId == null) return false ;
    try{
      PageImpl page = (PageImpl)getPageById(wikiType, wikiOwner, pageId)  ;
      Model model = getModel();
      WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
      ChromatticSession session = wStore.getSession() ;
      RemovedMixin mix = session.create(RemovedMixin.class) ;
      session.setEmbedded(page, RemovedMixin.class, mix) ;
      mix.setRemovedBy(Utils.getCurrentUser()) ;
      Calendar calendar = GregorianCalendar.getInstance() ;
      mix.setRemovedDate(calendar.getTime()) ;
      mix.setParentPath(page.getParentPage().getPath()) ;    
      WikiImpl wiki = (WikiImpl)getWiki(wikiType, wikiOwner, model) ;
      Trash trash = wiki.getTrash() ;
      if(trash == null) {
        trash = wiki.createTrash() ;
        wiki.setTrash(trash) ;
      }
      trash.addRemovedWikiPage(page) ;      
      session.save() ;
    }catch(Exception e) {
      return false ;
    }
    return true ;
    
    //return jcrDataStorage.deletePage(page.getPath(), wiki.getPath(), wStore.getSession()) ;    
  }
  
  public boolean renamePage(String wikiType, String wikiOwner, String pageName, String newName, String newTitle) throws Exception {
    if(WikiNodeType.Definition.WIKI_HOME_NAME.equals(pageName) || pageName == null) return false ;
    PageImpl currentPage = (PageImpl)getPageById(wikiType, wikiOwner, pageName)  ;
    Model model = getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();    
    return jcrDataStorage.renamePage(currentPage.getPath(), newName, newTitle, wStore.getSession()) ;    
  }  
  
  public boolean movePage(String pageId, String newParentId, String wikiType, String srcSpace, String destSpace) throws Exception {
    try {
      if(!isHasCreatePagePermission(Utils.getCurrentUser(), destSpace)){ return false ;}
      Model model = getModel();
      WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
      ChromatticSession session = wStore.getSession() ;
      PageImpl movePage = (PageImpl)getPageById(wikiType, srcSpace, pageId) ;
      MovedMixin mix = session.create(MovedMixin.class) ;
      session.setEmbedded(movePage, MovedMixin.class, mix) ;      
      PageImpl destPage = (PageImpl)getPageById(wikiType, destSpace, newParentId) ;
      movePage.setParentPage(destPage) ;
    }catch(Exception e) {
      return false ;
    }    
    return true;
  }
 
  public List<Space> getSpaces(String wikiType) throws Exception {
    return jcrDataStorage.getSpaces(wikiType, null) ;
  }
  
  public List<Space> getAllSpaces() throws Exception {
    return jcrDataStorage.getAllSpaces(null) ;
  }
  
  private boolean isHasCreatePagePermission(String userId, String destSpace) {
    
    return true ;
  }
  
  public Page getPageById(String wikiType, String wikiOwner, String pageId) throws Exception {
    
    Model model = getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    
    String statement = getStatement(wikiType, wikiOwner, pageId);
    if(statement != null) {
      PageImpl page = searchPage(statement, wStore.getSession()) ;
      //page.setChromatticSession(wStore.getSession()) ;
      if(WikiNodeType.Definition.WIKI_HOME_NAME.equals(pageId) || pageId == null) {
        return getWikiHome(wikiType, wikiOwner) ;        
      }
      return page ;
    }
    return null;
  }
  
  public Page getPageByUUID(String uuid) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public PageList<ContentImpl> searchContent(String wikiType, String wikiOwner, SearchData data) throws Exception {
    Model model = getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    if(data.getPath() == null || data.getPath().length() <= 0 ) {
      WikiHome home = getWikiHome(wikiType, wikiOwner) ;
      data.setPath(home.getPath()) ;
    }
    String statement = data.getChromatticStatement() ;
    List<ContentImpl> list = new ArrayList<ContentImpl>() ;
    if(statement != null) {
      Iterator<ContentImpl> result = wStore.getSession()
        .createQueryBuilder(ContentImpl.class)
        .where(statement).get().objects() ;
      while(result.hasNext()) {
        list.add(result.next()) ;
      }
    }
    return new ObjectPageList<ContentImpl>(list, 5);
  }
  
  public PageList<SearchResult> search(String wikiType, String wikiOwner, SearchData data) throws Exception {
    
    Model model = getModel();
    try{
      WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
      if(data.getPath() == null || data.getPath().length() <= 0 ) {
        WikiHome home = getWikiHome(wikiType, wikiOwner) ;
        data.setPath(home.getPath()) ;
      }
      PageList<SearchResult> result = jcrDataStorage.search(wStore.getSession(), data) ;      
      return result;
    }catch(Exception e) {}
    return null ;
  }
  
  public List<SearchResult> searchRenamedPage(String wikiType, String wikiOwner, String pageId) throws Exception {
    Model model = getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiHome home = getWikiHome(wikiType, wikiOwner) ;
    SearchData data = new SearchData(home.getPath(), pageId) ;
    return jcrDataStorage.searchRenamedPage(wStore.getSession(), data) ; 
  }
  
  public Object findByPath(String path, String objectNodeType) throws Exception  {
    String relPath  = path;
    if (relPath.startsWith("/")) relPath = relPath.substring(1) ;
    Model model = getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    if(WikiNodeType.WIKI_PAGE_CONTENT.equals(objectNodeType)) {
      return wStore.getSession().findByPath(ContentImpl.class, relPath) ;
    }else if (WikiNodeType.WIKI_ATTACHMENT_CONTENT.equals(objectNodeType)){
      relPath = relPath.substring(0, relPath.lastIndexOf("/")) ;
      return wStore.getSession().findByPath(AttachmentImpl.class, relPath) ;
    }    
    return null ;
  }
  
  public String getPageTitleOfAttachment(String path) throws Exception  {
    try{
      String relPath  = path;
      if (relPath.startsWith("/")) relPath = relPath.substring(1) ;
      String temp = relPath.substring(0,relPath.lastIndexOf("/")) ;
      temp = temp.substring(0,temp.lastIndexOf("/")) ;
      relPath = temp  + "/" + WikiNodeType.Definition.CONTENT;
      Model model = getModel();
      WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();      
      ContentImpl content =  wStore.getSession().findByPath(ContentImpl.class, relPath) ;
      return content.getTitle() ;
    }catch (Exception e) {}
    return null ;    
  }
  
  public InputStream getAttachmentAsStream(String path) throws Exception  {
    Model model = getModel();
    try{
      WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();      
      return jcrDataStorage.getAttachmentAsStream(path, wStore.getSession()) ;
    }catch(Exception e) {}
    return null ; 
  }
  
  public List<BreadcumbData> getBreadcumb(String wikiType, String wikiOwner, String pageId) throws Exception {
    return getBreadcumb(null, wikiType, wikiOwner, pageId);
  }
  
  public String getDefaultWikiSyntaxId() {
    return Syntax.XWIKI_2_0.toIdString();
  }
  
  private Model getModel(){
    MOWService mowService = (MOWService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(MOWService.class);
    return mowService.getModel();
  }
  
  private String getStatement(String wikiType, String wikiOwner, String pageId) throws Exception  {
    String path = null;
    if(PortalConfig.PORTAL_TYPE.equals(wikiType)) {
      path = Utils.getPortalWikisPath() ;      
    }else if(PortalConfig.GROUP_TYPE.equals(wikiType)) {
      path = nodeCreator.getJcrPath(GROUPS_PATH) ;
      path = (path != null) ? path : "/Groups";
    }else if(PortalConfig.USER_TYPE.equals(wikiType)) {
      path = nodeCreator.getJcrPath(USERS_PATH) ;
      path = (path != null) ? path : "/Users";
    }
    
    if(path != null) {
      path = path + "/" + Utils.validateWikiOwner(wikiType, wikiOwner) ;
      if(!PortalConfig.PORTAL_TYPE.equals(wikiType)){
        String appPath = null;
        if(PortalConfig.GROUP_TYPE.equals(wikiType)){
          appPath = nodeCreator.getJcrPath(GROUP_APPLICATION);
        } else {
          appPath = nodeCreator.getJcrPath(USER_APPLICATION);
        }
        appPath = (appPath != null) ? appPath : "ApplicationData";
        path = path + "/" + appPath + "/" + WikiNodeType.Definition.WIKI_APPLICATION;
      }
      StringBuilder statement = new StringBuilder() ;
      statement.append("(jcr:path LIKE '").append(path).append("/%/").append(pageId).append("' OR ")
      .append("jcr:path='").append(path).append("/").append(pageId).append("')");
      statement.append(" AND ")
      .append("( jcr:mixinTypes IS NULL OR NOT(jcr:mixinTypes = '").append(WikiNodeType.WIKI_REMOVED).append("') )") ;
      return statement.toString() ;
    }
    return null;
  }
  
  private PageImpl searchPage(String statement, ChromatticSession session) throws Exception {  
    PageImpl wikiPage = null;
    if(statement != null) {
      Iterator<PageImpl> result = session
        .createQueryBuilder(PageImpl.class)
        .where(statement).get().objects() ;
      if(result.hasNext()) wikiPage = result.next() ;
    }
    // TODO: still don't know reason but following code is necessary.
    if (wikiPage != null) {
      String path = wikiPage.getPath();
      if (path.startsWith("/")) {
        path = path.substring(1, path.length());
      }
      wikiPage = session.findByPath(PageImpl.class, path);
    }
    if (wikiPage != null) {
      wikiPage.setSession(session);
    }
    return wikiPage ;
  }
  
  private Wiki getWiki(String wikiType, String owner, Model model){
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiImpl wiki = null;
    if(PortalConfig.PORTAL_TYPE.equals(wikiType)) {
      WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
      wiki = portalWikiContainer.getWiki(owner);
    }else if(PortalConfig.GROUP_TYPE.equals(wikiType)) {
      WikiContainer<GroupWiki> groupWikiContainer = wStore.getWikiContainer(WikiType.GROUP);
      wiki = groupWikiContainer.getWiki(owner);
    }else if(PortalConfig.USER_TYPE.equals(wikiType)) {
      WikiContainer<UserWiki> userWikiContainer = wStore.getWikiContainer(WikiType.USER);
      wiki = userWikiContainer.getWiki(owner);
    }
    model.save();
    return wiki;
  }
  
  private WikiHome getWikiHome(String wikiType, String owner) throws Exception {
    Model model = getModel();
    WikiImpl wiki = (WikiImpl) getWiki(wikiType, owner, model);
    if(wiki != null){
      WikiHome wikiHome = wiki.getWikiHome();
      wikiHome.setSession(((WikiStoreImpl) model.getWikiStore()).getSession());
      return wikiHome;
    } else {
      return null ;
    }
  }
  
  private List<BreadcumbData> getBreadcumb(List<BreadcumbData> list, String wikiType, String wikiOwner, String pageId) throws Exception {
    if (list == null) {
      list = new ArrayList<BreadcumbData>(5);
    }
    if (pageId == null) {
      return list;
    }
    PageImpl page = (PageImpl) getPageById(wikiType, wikiOwner, pageId);
    if (page == null) {
      return list;
    }
    list.add(0, new BreadcumbData(page.getName(), page.getPath(), page.getContent().getTitle()));
    PageImpl parentPage = page.getParentPage();
    if (parentPage != null) {
      getBreadcumb(list, wikiType, wikiOwner, parentPage.getName());
    }
    
    return list;
  }
  
}
