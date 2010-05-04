package org.exoplatform.wiki.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.wiki.mow.api.Model;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.MOWService;
import org.exoplatform.wiki.mow.core.api.WikiStoreImpl;
import org.exoplatform.wiki.mow.core.api.content.ContentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.GroupWiki;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PortalWiki;
import org.exoplatform.wiki.mow.core.api.wiki.UserWiki;
import org.exoplatform.wiki.mow.core.api.wiki.WikiContainer;
import org.exoplatform.wiki.mow.core.api.wiki.WikiHome;
import org.exoplatform.wiki.service.BreadcumbData;
import org.exoplatform.wiki.service.SearchData;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.utils.Utils;

public class WikiServiceImpl implements WikiService{
  
  private NodeHierarchyCreator nodeCreator ;
  
  public WikiServiceImpl(NodeHierarchyCreator creator) {
    nodeCreator = creator ;
  }
  
  public Page createPage(String wikiType, String wikiOwner, String title, String parentId) throws Exception {
    
    MOWService mowService = (MOWService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(MOWService.class);
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    ContentImpl content = null;
    PageImpl page = null ;
    if(wikiType.equals(PortalConfig.PORTAL_TYPE)) {
      WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
      PortalWiki wiki = portalWikiContainer.getWiki(wikiOwner);      
      page = wiki.createWikiPage() ;
      content = wiki.createContent();
    }else if(wikiType.equals(PortalConfig.GROUP_TYPE)) {
      WikiContainer<GroupWiki> groupWikiContainer = wStore.getWikiContainer(WikiType.GROUP);
      GroupWiki wiki = groupWikiContainer.getWiki(wikiOwner);
      page = wiki.createWikiPage() ;
      content = wiki.createContent();
    }else if(wikiType.equals(PortalConfig.USER_TYPE)) {
      WikiContainer<UserWiki> userWikiContainer = wStore.getWikiContainer(WikiType.USER);
      UserWiki wiki = userWikiContainer.getWiki(wikiOwner);
      page = wiki.createWikiPage() ;
      content = wiki.createContent();
    }    
    
    String statement = getStatement(wikiType, wikiOwner, parentId) ;
    
    PageImpl parentPage = null ;
    if(statement != null) {            
      Iterator<PageImpl> result = wStore.getSession()
        .createQueryBuilder(PageImpl.class)
        .where(statement).get().objects() ;
      if(result.hasNext()) parentPage = result.next() ;
    }
    
    if(parentPage == null) throw new Exception() ;
    // TODO: still don't know reason but following code is necessary.
    String path = parentPage.getPath();
    if (path.startsWith("/")) {
      path = path.substring(1, path.length());
    }
    parentPage = wStore.getSession().findByPath(PageImpl.class, path);
    
    page.setName(title) ;
    parentPage.addWikiPage(page) ;
    page.setPageId(title) ;
    page.setContent(content);
    
    return page ;
  }
  
  /*private PageImpl createPage(String wikiType, String owner, WikiStoreImpl wStore) throws Exception {
    PageImpl page = null ;
    if(wikiType.equals(PortalConfig.PORTAL_TYPE)) {
      WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
      PortalWiki wiki = portalWikiContainer.getWiki(owner);      
      page = wiki.createWikiPage() ;
    }else if(wikiType.equals(PortalConfig.GROUP_TYPE)) {
      WikiContainer<GroupWiki> groupWikiContainer = wStore.getWikiContainer(WikiType.GROUP);
      GroupWiki wiki = groupWikiContainer.getWiki(owner);
      page = wiki.createWikiPage() ;
    }else if(wikiType.equals(PortalConfig.USER_TYPE)) {
      WikiContainer<UserWiki> userWikiContainer = wStore.getWikiContainer(WikiType.USER);
      UserWiki wiki = userWikiContainer.getWiki(owner);
      page = wiki.createWikiPage() ;
    }    
    return page ;
  }*/
  
  public void deletePage(String wikiType, String wikiOwner, String pageId) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public List<BreadcumbData> getBreadcumb(String wikiType, String wikiOwner, String pageId) throws Exception {
    return getBreadcumb(null, wikiType, wikiOwner, pageId);
  }
  
  public Page getPageById(String wikiType, String wikiOwner, String pageId) throws Exception {
    String statement = getStatement(wikiType, wikiOwner, pageId);
    
    MOWService mowService = (MOWService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(MOWService.class);
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    
    if(statement != null) {
      Page page = searchPage(statement, wStore.getSession()) ;
      if(page == null && pageId.equals(WikiNodeType.Definition.WIKI_HOME_NAME)) {
        return getWikiHome(wikiType, wikiOwner) ;        
      }      
      return page ;
    }
    return null;
  }
  
  private String getStatement(String wikiType, String wikiOwner, String pageId) throws Exception  {
    String path = null;
    if(wikiType.equals(PortalConfig.PORTAL_TYPE)) {
      path = Utils.getPortalWikisPath() ;      
    }else if(wikiType.equals(PortalConfig.GROUP_TYPE)) {
      path = nodeCreator.getJcrPath("groupsPath") ;    
    }else if(wikiType.equals(PortalConfig.USER_TYPE)) {
      path = nodeCreator.getJcrPath("usersPath") ;
    }
    
    if(path != null) {
      path = path + "/" + wikiOwner ;
      String statement = "jcr:path LIKE '"+ path + "/%'" + " AND pageId='" + pageId + "'" ;
      return statement ;
    }
    return null;
  }
  
  private PageImpl searchPage(String statement, ChromatticSession session) throws Exception {  
    MOWService mowService = (MOWService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(MOWService.class);
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    PageImpl wikiPage = null;
    if(statement != null) {
      Iterator<PageImpl> result = wStore.getSession()
        .createQueryBuilder(PageImpl.class)
        .where(statement).get().objects() ;
      if(result.hasNext()) wikiPage = result.next() ;
    }
    return wikiPage ;
  }
  
  private WikiHome getWikiHome(String wikiType, String owner) throws Exception {
    MOWService mowService = (MOWService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(MOWService.class);
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    if(wikiType.equals(PortalConfig.PORTAL_TYPE)) {
      WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
      PortalWiki wiki = portalWikiContainer.getWiki(owner);
      model.save() ;
      return wiki.getWikiHome() ;
    }else if(wikiType.equals(PortalConfig.GROUP_TYPE)) {
      WikiContainer<GroupWiki> groupWikiContainer = wStore.getWikiContainer(WikiType.GROUP);
      GroupWiki wiki = groupWikiContainer.getWiki(owner);
      model.save() ;
      return wiki.getWikiHome() ;
    }else if(wikiType.equals(PortalConfig.USER_TYPE)) {
      WikiContainer<UserWiki> userWikiContainer = wStore.getWikiContainer(WikiType.USER);
      UserWiki wiki = userWikiContainer.getWiki(owner);
      model.save() ;
      return wiki.getWikiHome() ;
    }
    return null ;
  }
  
  private List<BreadcumbData> getBreadcumb(List<BreadcumbData> list, String wikiType, String wikiOwner, String pageId) throws Exception {
    if (list == null) {
      list = new ArrayList<BreadcumbData>(5);
    }
    if (pageId == null) {
      return list;
    }
    Page page = getPageById(wikiType, wikiOwner, pageId);
    if (page == null) {
      return list;
    }
    list.add(0, new BreadcumbData(page.getPageId(), ((PageImpl) page).getPath(), page.getPageId()));
    Page parentPage = ((PageImpl) page).getParentPage();
    if (parentPage != null) {
      getBreadcumb(list, wikiType, wikiOwner, parentPage.getPageId());
    }
    
    return list;
  }
  
  public Page getPageByUUID(String uuid) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean movePage(String pageId, String newParentId) throws Exception {
    // TODO Auto-generated method stub
    return false;
  }

  public PageList search(String wikiType, String wikiOwner, SearchData data) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  
}
