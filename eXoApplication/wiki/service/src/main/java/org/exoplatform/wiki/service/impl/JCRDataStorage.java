package org.exoplatform.wiki.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.service.DataStorage;
import org.exoplatform.wiki.service.SearchData;
import org.exoplatform.wiki.service.SearchResult;

public class JCRDataStorage implements DataStorage{
  private static final Log log = ExoLogger.getLogger(JCRDataStorage.class);
  
  public PageList<SearchResult> search(ChromatticSession session, SearchData data) throws Exception {
    List<SearchResult> resultList = new ArrayList<SearchResult>() ;
    String statement = data.getStatement() ;
    QueryManager qm = session.getJCRSession().getWorkspace().getQueryManager();
    Query q = qm.createQuery(statement, Query.SQL);
    QueryResult result = q.execute();
    RowIterator iter = result.getRows() ;
    while(iter.hasNext()) {
      try{resultList.add(getResult(iter.nextRow())) ;} catch(Exception e){}
    }
    return new ObjectPageList<SearchResult>(resultList, 5) ;
  }  
  
  private SearchResult getResult(Row row) throws Exception {
    String type = row.getValue("jcr:primaryType").getString() ;
    String path = row.getValue("jcr:path").getString() ;
    String excerpt = row.getValue("rep:excerpt(.)").getString() ;
    String title = (row.getValue("title")== null ? null : row.getValue("title").getString()) ;
    SearchResult result = new SearchResult(excerpt,title, path, type) ;
    return result ;
  }
  
  public InputStream getAttachmentAsStream(String path, ChromatticSession session) throws Exception {
    Node attContent = (Node)session.getJCRSession().getItem(path) ;
    return attContent.getProperty("jcr:data").getStream() ;    
  }
  
  public boolean deletePage(String pagePath, String wikiPath, ChromatticSession session) throws Exception {
    try {
      Node deletePage = (Node)session.getJCRSession().getItem(pagePath) ;
      deletePage.addMixin(WikiNodeType.WIKI_REMOVED) ;
      deletePage.setProperty("removedBy", getCurrentUser()) ;
      Calendar calendar = GregorianCalendar.getInstance() ;
      deletePage.setProperty("removedDate", calendar) ;
      deletePage.save() ;
      Node wiki = (Node)session.getJCRSession().getItem(wikiPath) ;
      Node trashNode ;
      try{
        trashNode = wiki.getNode(WikiNodeType.Definition.TRASH_NAME) ;
      }catch(PathNotFoundException e) {
        trashNode = wiki.addNode(WikiNodeType.Definition.TRASH_NAME, WikiNodeType.WIKI_TRASH) ;
        wiki.save() ;
      }
      trashNode.getSession().getWorkspace().move(deletePage.getPath(), trashNode.getPath() + "/" + deletePage.getName()) ;    
      
      return true ;
    } catch(Exception e) {
      log.error("Could not delete page: " + pagePath) ;
      return false ;
    }   
  }
  
  private String getCurrentUser() {
    try {
      ConversationState conversationState = ConversationState.getCurrent();
      return conversationState.getIdentity().getUserId();
    }catch(Exception e){}
    return "system" ;
  }
  
}
