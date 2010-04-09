package org.exoplatform.wiki.service;

public class WikiPageParams {
  private String type ;
  private String owner ;
  private String pageId ;
  private String attachmentName;
  
  
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
  public String getAttachmentName() {
    return attachmentName;
  }
  public void setAttachmentName(String attachmentName) {
    this.attachmentName = attachmentName;
  }
}
