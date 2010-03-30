package org.exoplatform.wiki.service;

public class WikiPageParams {
  private String type ;
  private String owner ;
  private String pageId ;
  
  
  public void setType(String type) {
    this.type = type;
  }
  public String getType() {
    return type;
  }
  public void setOwner(String owner) {
    this.owner = owner;
  }
  public String getOwner() {
    return owner;
  }
  public void setPageId(String pageId) {
    this.pageId = pageId;
  }
  public String getPageId() {
    return pageId;
  }  
}
