package org.exoplatform.wiki.service.impl;

import java.util.Iterator;
import java.util.List;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.wiki.mow.api.Model;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.core.api.MOWService;
import org.exoplatform.wiki.mow.core.api.WikiStoreImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
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
    
    return null ;
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
    PageImpl wikiPage = null;    
    
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
      Iterator<PageImpl> result = 
        wStore.getSession()
        .createQueryBuilder(PageImpl.class)
        .where("jcr:path LIKE '"+ path + "/%'" + " AND pageId='" + pageId + "'").get().objects() ;
      if(result.hasNext()) wikiPage = result.next() ;
    }
    return wikiPage;
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
