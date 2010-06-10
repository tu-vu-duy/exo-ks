package org.exoplatform.wiki.service;

import java.io.InputStream;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.utils.PageList;

public interface DataStorage {
  public PageList<SearchResult> search(ChromatticSession session, SearchData data) throws Exception ;
  public InputStream getAttachmentAsStream(String path, ChromatticSession session) throws Exception ;
  public boolean deletePage(String path, String wikiPath, ChromatticSession session) throws Exception ;
}
