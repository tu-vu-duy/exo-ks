package org.exoplatform.wiki.service.impl;

import java.util.List;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.wiki.mow.api.Model;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.MOWService;
import org.exoplatform.wiki.mow.core.api.WikiStoreImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PortalWiki;
import org.exoplatform.wiki.mow.core.api.wiki.WikiContainer;
import org.exoplatform.wiki.mow.core.api.wiki.WikiHome;
import org.exoplatform.wiki.service.BreadcumbData;
import org.exoplatform.wiki.service.SearchData;
import org.exoplatform.wiki.service.WikiService;

public class WikiServiceImpl implements WikiService{

  public void createPage(String wikiType, String wikiOwner, Page page, String parentId) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void deletePage(String wikiType, String wikiOwner, String pageId) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public List<BreadcumbData> getBreadcumb(String wikiType, String wikiOwner, String pageId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public Page getPageById(String wikiType, String wikiOwner, String pageId) throws Exception {
    //TODO: just an implement for test, pls writing a real implement
    MOWService mowService = (MOWService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(MOWService.class);
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    PageImpl wikipage = null;
    if("portal".equalsIgnoreCase(wikiType)){
      WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
      PortalWiki wiki = portalWikiContainer.getWiki(wikiOwner);
      WikiHome wikiHomePage = wiki.getWikiHome();
      wikipage = wikiHomePage.getWikiPage(pageId);
    }
    return wikipage;
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

  public void updatePage(String wikiType, String wikiOwner, Page page) throws Exception {
    // TODO Auto-generated method stub
    
  }

 
  
}
