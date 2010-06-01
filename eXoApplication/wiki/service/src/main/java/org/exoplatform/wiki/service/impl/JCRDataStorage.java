package org.exoplatform.wiki.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.wiki.service.DataStorage;
import org.exoplatform.wiki.service.SearchData;
import org.exoplatform.wiki.service.SearchResult;

public class JCRDataStorage implements DataStorage{
  
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
    return new ObjectPageList<SearchResult>(resultList, 10) ;
  }  
  
  private SearchResult getResult(Row row) throws Exception {
    String type = row.getValue("jcr:primaryType").getString() ;
    String path = row.getValue("jcr:path").getString() ;
    String excerpt = row.getValue("rep:excerpt(.)").getString() ;
    String title = (row.getValue("title")== null ? null : row.getValue("title").getString()) ;
    SearchResult result = new SearchResult(excerpt,title, path, type) ;
    return result ;
  }
}
