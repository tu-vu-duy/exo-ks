package org.exoplatform.wiki.service;

public class SearchData {
  private String text ;
  private String title ;
  private String content ;
  private String path ;
  
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
  
  public String getStatement() {
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
          statement.append(" CONTAINS(wiki:title, '").append(title).append("') ") ;
          isAnd = true ;
        }
        if(content != null && content.length() > 0) {
          // search on content 
        }
      }
    }catch(Exception e) {}
    
    return statement.toString() ;
  }

  
  
}
