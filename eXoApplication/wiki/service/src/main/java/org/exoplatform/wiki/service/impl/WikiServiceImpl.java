package org.exoplatform.wiki.service.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;
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
import org.exoplatform.wiki.mow.core.api.wiki.RenamedMixin;
import org.exoplatform.wiki.mow.core.api.wiki.Trash;
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

public class WikiServiceImpl implements WikiService {

  final static private String   USERS_PATH        = "usersPath";

  final static private String   GROUPS_PATH       = "groupsPath";

  final static private String   USER_APPLICATION  = "userApplicationData";

  final static private String   GROUP_APPLICATION = "groupApplicationData";

  private ConfigurationManager  configManager;

  private NodeHierarchyCreator  nodeCreator;

  private JCRDataStorage        jcrDataStorage;

  private Iterator<ValuesParam> syntaxHelpParams;

  private static final Log      log               = ExoLogger.getLogger(WikiServiceImpl.class);

  public WikiServiceImpl(ConfigurationManager configManager,               
                         NodeHierarchyCreator creator,
                         JCRDataStorage jcrDataStorage,
                         InitParams initParams) {
    this.configManager = configManager;
    this.nodeCreator = creator;
    this.jcrDataStorage = jcrDataStorage;      
    if (initParams != null) {
      syntaxHelpParams = initParams.getValuesParamIterator();
    }
  }

  public Page createPage(String wikiType, String wikiOwner, String title, String parentId) throws Exception {
    String pageId = TitleResolver.getPageId(title, false);
    if(isExisting(wikiType, wikiOwner, pageId)) throw new Exception();
    Model model = getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiImpl wiki = (WikiImpl) getWiki(wikiType, wikiOwner, model);
    PageImpl page = wiki.createWikiPage();
    PageImpl parentPage = null;
    String statement = getStatement(wikiType, wikiOwner, parentId);
    parentPage = searchPage(statement, wStore.getSession());
    if (parentPage == null)
      throw new Exception();    
    page.setName(pageId);
    parentPage.addWikiPage(page);
    ConversationState conversationState = ConversationState.getCurrent();
    String creator = null;
    if (conversationState != null && conversationState.getIdentity() != null) {
      creator = conversationState.getIdentity().getUserId();
    }
    page.setOwner(creator);
    page.getContent().setTitle(title);
    page.makeVersionable();
    page.setChromatticSession(wStore.getSession());
    model.save();
    return page;
  }
  
  public void createDraftNewPage(String draftNewPageId) throws Exception {
    Model model = getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    PageImpl draftNewPagesContainer = wStore.getDraftNewPagesContainer();
    PageImpl oldDraftPage = draftNewPagesContainer.getChildPages().get(draftNewPageId);
    if (oldDraftPage != null) {
      oldDraftPage.remove();
    }
    PageImpl draftNewPage = wStore.createPage();
    draftNewPagesContainer.getChildPages().put(draftNewPageId, draftNewPage);
  }
  
  public boolean isExisting(String wikiType, String wikiOwner, String pageId) throws Exception {
    Model model = getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    String statement = getStatement(wikiType, wikiOwner, pageId);
    if (statement != null) {
      Iterator<PageImpl> result = wStore.getSession().createQueryBuilder(PageImpl.class)
      .where(statement)
      .get()
      .objects();
      return result.hasNext() ;
    }
    return false;
  }
  
  public boolean deletePage(String wikiType, String wikiOwner, String pageId) throws Exception {
    if (WikiNodeType.Definition.WIKI_HOME_NAME.equals(pageId) || pageId == null)
      return false;
    try {
      PageImpl page = (PageImpl) getPageById(wikiType, wikiOwner, pageId);
      Model model = getModel();
      WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
      ChromatticSession session = wStore.getSession();
      RemovedMixin mix = session.create(RemovedMixin.class);
      session.setEmbedded(page, RemovedMixin.class, mix);
      mix.setRemovedBy(Utils.getCurrentUser());
      Calendar calendar = GregorianCalendar.getInstance();
      calendar.setTimeInMillis(new Date().getTime()) ;
      mix.setRemovedDate(calendar.getTime());
      mix.setParentPath(page.getParentPage().getPath());
      WikiImpl wiki = (WikiImpl) getWiki(wikiType, wikiOwner, model);
      Trash trash = wiki.getTrash();
      if (trash == null) {
        trash = wiki.createTrash();
        wiki.setTrash(trash);
      }
      if(trash.isHasPage(page.getName())) {
        PageImpl oldDeleted = trash.getPage(page.getName()) ;
        String removedDate = oldDeleted.getRemovedMixin().getRemovedDate().toGMTString() ;
        String newName = page.getName()+ "_" + removedDate.replaceAll(" ", "-").replaceAll(":", "-");
        trash.addChild(newName, oldDeleted) ;        
      }      
      trash.addRemovedWikiPage(page);      
      session.save();
    } catch (Exception e) {
      log.error("Can't delete page '" + pageId + "' ", e) ;
      return false;
    }
    return true;    
  }
  
  public void deleteDraftNewPage(String newDraftPageId) throws Exception {
    Model model = getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    PageImpl draftNewPagesContainer = wStore.getDraftNewPagesContainer();
    draftNewPagesContainer.getChildPages().remove(newDraftPageId);
  }

  public boolean renamePage(String wikiType,
                            String wikiOwner,
                            String pageName,
                            String newName,
                            String newTitle) throws Exception {
    if (WikiNodeType.Definition.WIKI_HOME_NAME.equals(pageName) || pageName == null)
      return false;
    PageImpl currentPage = (PageImpl) getPageById(wikiType, wikiOwner, pageName);
    currentPage.getContent().setTitle(newTitle) ;
    PageImpl parentPage = (PageImpl) getPageById(wikiType, wikiOwner, currentPage.getParentPage().getName());
    parentPage.addPage(newName, currentPage) ;
    if(currentPage.getRenamedMixin() != null) {
      RenamedMixin mix = currentPage.getRenamedMixin() ;
      List<String> ids = new ArrayList<String>() ;
      for(String id : mix.getOldPageIds()) {
        ids.add(id) ;
      }
      ids.add(pageName) ;
      mix.setOldPageIds(ids.toArray(new String[]{}));
    }else {
      RenamedMixin mix = parentPage.getChromatticSession().create(RenamedMixin.class);
      currentPage.setRenamedMixin(mix) ;
      List<String> ids = new ArrayList<String>() ;
      ids.add(pageName) ;
      mix.setOldPageIds(ids.toArray(new String[]{}));
    }
    parentPage.getChromatticSession().save() ;
    return true ;    
  }

  public boolean movePage(String pageId,
                          String newParentId,
                          String wikiType,
                          String srcSpace,
                          String destSpace) throws Exception {
    try {
      if (!isHasCreatePagePermission(Utils.getCurrentUser(), destSpace)) {
        return false;
      }
      Model model = getModel();
      WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
      ChromatticSession session = wStore.getSession();
      PageImpl movePage = (PageImpl) getPageById(wikiType, srcSpace, pageId);
      MovedMixin mix = session.create(MovedMixin.class);
      session.setEmbedded(movePage, MovedMixin.class, mix);
      PageImpl destPage = (PageImpl) getPageById(wikiType, destSpace, newParentId);
      movePage.setParentPage(destPage);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  /*
   * public List<Space> getSpaces(String wikiType) throws Exception { return
   * jcrDataStorage.getSpaces(wikiType, null) ; } public List<Space>
   * getAllSpaces() throws Exception { return jcrDataStorage.getAllSpaces(null)
   * ; }
   */

  private boolean isHasCreatePagePermission(String userId, String destSpace) {

    return true;
  }

  public Page getPageById(String wikiType, String wikiOwner, String pageId) throws Exception {

    Model model = getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();

    String statement = getStatement(wikiType, wikiOwner, pageId);
    if (statement != null) {
      PageImpl page = searchPage(statement, wStore.getSession());
      // page.setChromatticSession(wStore.getSession()) ;
      if (WikiNodeType.Definition.WIKI_HOME_NAME.equals(pageId) || pageId == null) {
        return getWikiHome(wikiType, wikiOwner);
      }
      return page;
    }
    return null;
  }

  public Page getExsitedOrNewDraftPageById(String wikiType, String wikiOwner, String pageId) throws Exception {
    Page existedPage = getPageById(wikiType, wikiOwner, pageId);
    if (existedPage != null) {
      return existedPage;
    }
    Model model = getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    PageImpl draftNewPagesContainer = wStore.getDraftNewPagesContainer();
    return draftNewPagesContainer.getChildPages().get(pageId);
  }
  
  public Page getPageByUUID(String uuid) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public PageList<ContentImpl> searchContent(String wikiType, String wikiOwner, SearchData data) throws Exception {
    Model model = getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    if (data.getPath() == null || data.getPath().length() <= 0) {
      WikiHome home = getWikiHome(wikiType, wikiOwner);
      data.setPath(home.getPath());
    }
    String statement = data.getChromatticStatement();
    List<ContentImpl> list = new ArrayList<ContentImpl>();
    if (statement != null) {
      Iterator<ContentImpl> result = wStore.getSession()
                                           .createQueryBuilder(ContentImpl.class)
                                           .where(statement)
                                           .get()
                                           .objects();
      while (result.hasNext()) {
        list.add(result.next());
      }
    }
    return new ObjectPageList<ContentImpl>(list, 5);
  }

  public PageList<SearchResult> search(String wikiType, String wikiOwner, SearchData data) throws Exception {

    Model model = getModel();
    try {
      WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
      if (data.getPath() == null || data.getPath().length() <= 0) {
        WikiHome home = getWikiHome(wikiType, wikiOwner);
        data.setPath(home.getPath());
      }
      PageList<SearchResult> result = jcrDataStorage.search(wStore.getSession(), data);
      return result;
    } catch (Exception e) {
    }
    return null;
  }

  public List<SearchResult> searchRenamedPage(String wikiType, String wikiOwner, String pageId) throws Exception {
    Model model = getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiHome home = getWikiHome(wikiType, wikiOwner);
    SearchData data = new SearchData(home.getPath(), pageId);
    return jcrDataStorage.searchRenamedPage(wStore.getSession(), data);
  }

  public Object findByPath(String path, String objectNodeType) throws Exception {
    String relPath = path;
    if (relPath.startsWith("/"))
      relPath = relPath.substring(1);
    Model model = getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    if (WikiNodeType.WIKI_PAGE_CONTENT.equals(objectNodeType)) {
      return wStore.getSession().findByPath(ContentImpl.class, relPath);
    } else if (WikiNodeType.WIKI_ATTACHMENT_CONTENT.equals(objectNodeType)) {
      relPath = relPath.substring(0, relPath.lastIndexOf("/"));
      return wStore.getSession().findByPath(AttachmentImpl.class, relPath);
    }
    return null;
  }

  public String getPageTitleOfAttachment(String path) throws Exception {
    try {
      String relPath = path;
      if (relPath.startsWith("/"))
        relPath = relPath.substring(1);
      String temp = relPath.substring(0, relPath.lastIndexOf("/"));
      temp = temp.substring(0, temp.lastIndexOf("/"));
      relPath = temp + "/" + WikiNodeType.Definition.CONTENT;
      Model model = getModel();
      WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
      ContentImpl content = wStore.getSession().findByPath(ContentImpl.class, relPath);
      return content.getTitle();
    } catch (Exception e) {
    }
    return null;
  }

  public InputStream getAttachmentAsStream(String path) throws Exception {
    Model model = getModel();
    try {
      WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
      return jcrDataStorage.getAttachmentAsStream(path, wStore.getSession());
    } catch (Exception e) {
    }
    return null;
  }

  public List<BreadcumbData> getBreadcumb(String wikiType, String wikiOwner, String pageId) throws Exception {
    return getBreadcumb(null, wikiType, wikiOwner, pageId);
  }

  public PageImpl getHelpSyntaxPage(String syntaxId) {
    Model model = getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    if (wStore.getHelpPagesContainer().getChildPages().size() == 0) {
      createHelpPages(wStore);
    }
    Iterator<PageImpl> syntaxPageIterator = wStore.getHelpPagesContainer().getChildPages().values().iterator();
    while (syntaxPageIterator.hasNext()) {
      PageImpl syntaxPage = syntaxPageIterator.next();
      if (syntaxPage.getContent().getSyntax().equals(syntaxId)) {
        return syntaxPage;
      }
    }
    return null;
  }
  
  public String getDefaultWikiSyntaxId() {
    return Syntax.XWIKI_2_0.toIdString();
  }

  private Model getModel() {
    MOWService mowService = (MOWService) ExoContainerContext.getCurrentContainer()
                                                            .getComponentInstanceOfType(MOWService.class);
    return mowService.getModel();
  }

  private String getStatement(String wikiType, String wikiOwner, String pageId) throws Exception {
    String path = null;
    if (PortalConfig.PORTAL_TYPE.equals(wikiType)) {
      path = Utils.getPortalWikisPath();
    } else if (PortalConfig.GROUP_TYPE.equals(wikiType)) {
      path = nodeCreator.getJcrPath(GROUPS_PATH);
      path = (path != null) ? path : "/Groups";
    } else if (PortalConfig.USER_TYPE.equals(wikiType)) {
      path = nodeCreator.getJcrPath(USERS_PATH);
      path = (path != null) ? path : "/Users";
    }

    if (path != null) {
      path = path + "/" + Utils.validateWikiOwner(wikiType, wikiOwner);
      if (!PortalConfig.PORTAL_TYPE.equals(wikiType)) {
        String appPath = null;
        if (PortalConfig.GROUP_TYPE.equals(wikiType)) {
          appPath = nodeCreator.getJcrPath(GROUP_APPLICATION);
        } else {
          appPath = nodeCreator.getJcrPath(USER_APPLICATION);
        }
        appPath = (appPath != null) ? appPath : "ApplicationData";
        path = path + "/" + appPath + "/" + WikiNodeType.Definition.WIKI_APPLICATION;
      }
      StringBuilder statement = new StringBuilder();
      statement.append("(jcr:path LIKE '")
               .append(path)
               .append("/%/")
               .append(pageId)
               .append("' OR ")
               .append("jcr:path='")
               .append(path)
               .append("/")
               .append(pageId)
               .append("')");
      statement.append(" AND ")
               .append("( jcr:mixinTypes IS NULL OR NOT(jcr:mixinTypes = '")
               .append(WikiNodeType.WIKI_REMOVED)
               .append("') )");
      return statement.toString();
    }
    return null;
  }

  private PageImpl searchPage(String statement, ChromatticSession session) throws Exception {
    PageImpl wikiPage = null;
    if (statement != null) {
      Iterator<PageImpl> result = session.createQueryBuilder(PageImpl.class)
                                         .where(statement)
                                         .get()
                                         .objects();
      if (result.hasNext())
        wikiPage = result.next();
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
      wikiPage.setChromatticSession(session);
    }
    return wikiPage;
  }

  private Wiki getWiki(String wikiType, String owner, Model model) {
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiImpl wiki = null;
    if (PortalConfig.PORTAL_TYPE.equals(wikiType)) {
      WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
      wiki = portalWikiContainer.getWiki(owner);
    } else if (PortalConfig.GROUP_TYPE.equals(wikiType)) {
      WikiContainer<GroupWiki> groupWikiContainer = wStore.getWikiContainer(WikiType.GROUP);
      wiki = groupWikiContainer.getWiki(owner);
    } else if (PortalConfig.USER_TYPE.equals(wikiType)) {
      WikiContainer<UserWiki> userWikiContainer = wStore.getWikiContainer(WikiType.USER);
      wiki = userWikiContainer.getWiki(owner);
    }
    model.save();
    return wiki;
  }

  private WikiHome getWikiHome(String wikiType, String owner) throws Exception {
    Model model = getModel();
    WikiImpl wiki = (WikiImpl) getWiki(wikiType, owner, model);
    if (wiki != null) {
      WikiHome wikiHome = wiki.getWikiHome();
      wikiHome.setChromatticSession(((WikiStoreImpl) model.getWikiStore()).getSession());
      return wikiHome;
    } else {
      return null;
    }

  }

  private List<BreadcumbData> getBreadcumb(List<BreadcumbData> list,
                                           String wikiType,
                                           String wikiOwner,
                                           String pageId) throws Exception {
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

  private void createHelpPages(WikiStoreImpl wStore) {
    PageImpl helpPage = wStore.getHelpPagesContainer();
    while (syntaxHelpParams.hasNext()) {
      try {
        ValuesParam syntaxhelpParam = syntaxHelpParams.next();
        String syntaxName = syntaxhelpParam.getName();
        ArrayList<String> syntaxValues = syntaxhelpParam.getValues();
        String shortFile = syntaxValues.get(0);
        String fullFile = syntaxValues.get(1);
        PageImpl syntaxPage = addSyntaxPage(wStore, helpPage, syntaxName, shortFile, " Short help Page");
        addSyntaxPage(wStore, syntaxPage, syntaxName, fullFile, " Full help Page");
      } catch (Exception e) {
        // TODO Auto-generated catch block
        log.error("Can not create Help page", e);
      }
    }
  }

  private PageImpl addSyntaxPage(WikiStoreImpl wStore,
                                 PageImpl parentPage,
                                 String name,
                                 String path,
                                 String type) throws Exception {
    StringBuffer stringContent = new StringBuffer();
    InputStream inputContent = null;
    BufferedReader bufferReader = null;
    String tempLine;
    inputContent = configManager.getInputStream(path);
    bufferReader = new BufferedReader(new InputStreamReader(inputContent));
    while ((tempLine = bufferReader.readLine()) != null) {
      stringContent.append(tempLine + "\n");
    }

    PageImpl syntaxPage = wStore.createPage();
    String realName = name.replace("/", "");
    syntaxPage.setName(realName + type);
    syntaxPage.setParentPage(parentPage);
    ContentImpl content = syntaxPage.getContent();
    content.setTitle(realName + type);
    content.setText(stringContent.toString());
    content.setSyntax(name);
    inputContent.close();
    bufferReader.close();
    return syntaxPage;
  }

}
