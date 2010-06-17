package org.exoplatform.wiki.service;

public class SearchData {
  private String text ;
  private String title ;
  private String content ;
  private String path ;
  private String pageId ;
  
  public SearchData(String path, String pageId) {
    this.path = path ;
    this.pageId = pageId ;
  }
  
  public SearchData(String text, String title, String content, String path) {
    this.text = text ;
    this.title = title ;
    this.content = content ;
    this.path = path ;
  }
  
  public void setTitle(String title) {
    this.title = title;
  }
  public String getTitle() {
    return title;
  }
  
  public void setContent(String content) {
    this.content = content;
  }
  public String getContent() {
    return content;
  }
  
  public void setPath(String path) {
    this.path = path;
  }
  public String getPath() {
    return path;
  }
  
  public void setText(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }
  
  public void setPageId(String pageId) {
    this.pageId = pageId;
  }

  public String getPageId() {
    return pageId;
  }

  public String getChromatticStatement() {
    StringBuilder statement = new StringBuilder();    
    try {
      boolean isAnd = false ;
      if(path != null && path.length() > 0) {
        statement.append("jcr:path LIKE '"+ path + "/%'") ;
        isAnd = true ;
      }
      if(text != null && text.length() > 0) {
        if(isAnd) statement.append(" AND ") ;
        statement.append(" CONTAINS(*, '").append(text).append("')") ; 
        isAnd = true ;
      }else {        
        if(title != null && title.length() > 0) {
          if(isAnd) statement.append(" AND ") ;
          statement.append(" CONTAINS(title, '").append(title).append("') ") ;
          isAnd = true ;
        }
        if(content != null && content.length() > 0) {
          if(isAnd) statement.append(" AND ") ;
          statement.append(" CONTAINS(text, '").append(content).append("') ") ; 
        }
      }
    }catch(Exception e) {}
    return statement.toString() ;
  }
  
  public String getStatement() {
    StringBuilder statement = new StringBuilder();    
    try {
      statement.append("SELECT title, jcr:primaryType, path, excerpt(.) ")
               .append("FROM nt:base ")
               .append("WHERE ") ;
      boolean isAnd = false ;
      if(path != null && path.length() > 0) {
        statement.append("jcr:path LIKE '"+ path + "/%'") ;
        isAnd = true ;
      }
      if(text != null && text.length() > 0) {
        if(isAnd) statement.append(" AND ") ;
        statement.append(" CONTAINS(*, '").append(text).append("')") ; 
        isAnd = true ;
      }else {        
        if(title != null && title.length() > 0) {
          if(isAnd) statement.append(" AND ") ;
          statement.append(" CONTAINS(title, '").append(title).append("') ") ;
          isAnd = true ;
        }
        if(content != null && content.length() > 0) {
          if(isAnd) statement.append(" AND ") ;
          statement.append(" CONTAINS(text, '").append(content).append("') ") ; 
        }
      }
      statement.append(" ORDER BY jcr:score") ;
    }catch(Exception e) {}
    return statement.toString() ;
  }
  
  public String getStatementForRenamedPage() {
    StringBuilder statement = new StringBuilder();    
    try {
      statement.append("SELECT * ")
               .append("FROM wiki:renamed ")
               .append("WHERE ") ;
      
      if(path != null && path.length() > 0) {
        statement.append("jcr:path LIKE '"+ path + "/%'") ;
      
      }
      if(getPageId() != null && getPageId().length() > 0) {
        statement.append(" AND ") ;
        statement.append(" oldPageIds = '").append(getPageId()).append("'") ;
      }      
    }catch(Exception e) {}
    return statement.toString() ;
  }
  
}
