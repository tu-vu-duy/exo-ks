package org.exoplatform.wiki.service.impl;

import java.util.List;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.wiki.mow.api.Page;
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
    // TODO Auto-generated method stub
    return null;
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
