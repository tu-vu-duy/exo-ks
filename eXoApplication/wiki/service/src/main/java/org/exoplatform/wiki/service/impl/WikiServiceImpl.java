package org.exoplatform.wiki.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
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
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PortalWiki;
import org.exoplatform.wiki.mow.core.api.wiki.UserWiki;
import org.exoplatform.wiki.mow.core.api.wiki.WikiContainer;
import org.exoplatform.wiki.mow.core.api.wiki.WikiHome;
import org.exoplatform.wiki.mow.core.api.wiki.WikiImpl;
import org.exoplatform.wiki.resolver.TitleResolver;
import org.exoplatform.wiki.service.BreadcumbData;
import org.exoplatform.wiki.service.SearchData;
import org.exoplatform.wiki.service.SearchResult;
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
  
  public WikiServiceImpl(NodeHierarchyCreator creator, JCRDataStorage jcrDataStorage) {
    nodeCreator = creator ;
    this.jcrDataStorage = jcrDataStorage ;
  }
  
  public Page createPage(String wikiType, String wikiOwner, String title, String parentId) throws Exception {
    
    Model model = getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    
    WikiImpl wiki = (WikiImpl) getWiki(wikiType, wikiOwner, model);
    PageImpl page = wiki.createWikiPage() ;
    ContentImpl content = wiki.createContent();
    
    PageImpl parentPage = null ;
    String statement = getStatement(wikiType, wikiOwner, parentId) ;
    parentPage = searchPage(statement, wStore.getSession());
    if(parentPage == null) throw new Exception() ;
    
    String pageId = TitleResolver.getPageId(title, false);
    page.setName(pageId) ;
    parentPage.addWikiPage(page) ;
    page.setContent(content);
    page.getContent().setTitle(title);
    model.save();    

    return page ;
  }
  
  public boolean deletePage(String wikiType, String wikiOwner, String pageId) throws Exception {
    try{
      PageImpl page = (PageImpl)getPageById(wikiType, wikiOwner, pageId) ;
      page.remove() ;
    }catch(Exception e) {
      return false ;
    }
    return true ;    
  }
  
  public boolean movePage(String pageId, String newParentId, String wikiType, String wikiOwner) throws Exception {
    try {
      PageImpl movePage = (PageImpl)getPageById(wikiType, wikiOwner, pageId) ;
      PageImpl destPage = (PageImpl)getPageById(wikiType, wikiOwner, newParentId) ;
      movePage.setParentPage(destPage) ;
    }catch(Exception e) {
      return false ;
    }    
    return true;
  }
  
  public Page getPageById(String wikiType, String wikiOwner, String pageId) throws Exception {
    
    Model model = getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    
    String statement = getStatement(wikiType, wikiOwner, pageId);
    if(statement != null) {
      Page page = searchPage(statement, wStore.getSession()) ;
      if(page == null && pageId.equals(WikiNodeType.Definition.WIKI_HOME_NAME)) {
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
  
  public Object findByPath(String path, String objectNodeType) throws Exception  {
    String relPath  = path;
    if (relPath.startsWith("/")) relPath = relPath.substring(1) ;
    Model model = getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    if(objectNodeType.equals(WikiNodeType.WIKI_PAGE_CONTENT)) {
      return wStore.getSession().findByPath(ContentImpl.class, relPath) ;
    }else if (objectNodeType.equals(WikiNodeType.WIKI_ATTACHMENT_CONTENT)){
      relPath = relPath.substring(0, relPath.lastIndexOf("/")) ;
      return wStore.getSession().findByPath(AttachmentImpl.class, relPath) ;
    }    
    return null ;
  }
  
  public String getPageTitleOfAttachment(String path) throws Exception  {
    try{
      String relPath  = path;
      if (relPath.startsWith("/")) relPath = relPath.substring(1) ;
      if(relPath.indexOf("/att") > 0) {
        relPath = relPath.substring(0, relPath.indexOf("/att")) + "/" + WikiNodeType.Definition.CONTENT;
      }
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
    if(wikiType.equals(PortalConfig.PORTAL_TYPE)) {
      path = Utils.getPortalWikisPath() ;      
    }else if(wikiType.equals(PortalConfig.GROUP_TYPE)) {
      path = nodeCreator.getJcrPath(GROUPS_PATH) ;
      path = (path != null) ? path : "/Groups";
    }else if(wikiType.equals(PortalConfig.USER_TYPE)) {
      path = nodeCreator.getJcrPath(USERS_PATH) ;
      path = (path != null) ? path : "/Users";
    }
    
    if(path != null) {
      path = path + "/" + Utils.validateWikiOwner(wikiType, wikiOwner) ;
      if(!wikiType.equals(PortalConfig.PORTAL_TYPE)){
        String appPath = null;
        if(wikiType.equals(PortalConfig.GROUP_TYPE)){
          appPath = nodeCreator.getJcrPath(GROUP_APPLICATION);
        } else {
          appPath = nodeCreator.getJcrPath(USER_APPLICATION);
        }
        appPath = (appPath != null) ? appPath : "ApplicationData";
        path = path + "/" + appPath + "/" + WikiNodeType.Definition.WIKI_APPLICATION;
      }
      String statement = "jcr:path LIKE '"+ path + "/%/" + pageId + "' OR " + "jcr:path='"+ path + "/" + pageId + "'";
      return statement ;
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
    return wikiPage ;
  }
  
  private Wiki getWiki(String wikiType, String owner, Model model){
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiImpl wiki = null;
    if(wikiType.equals(PortalConfig.PORTAL_TYPE)) {
      WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
      wiki = portalWikiContainer.getWiki(owner);
    }else if(wikiType.equals(PortalConfig.GROUP_TYPE)) {
      WikiContainer<GroupWiki> groupWikiContainer = wStore.getWikiContainer(WikiType.GROUP);
      wiki = groupWikiContainer.getWiki(owner);
    }else if(wikiType.equals(PortalConfig.USER_TYPE)) {
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
      return wiki.getWikiHome();
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
