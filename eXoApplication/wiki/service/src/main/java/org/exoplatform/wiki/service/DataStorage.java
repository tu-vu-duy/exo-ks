package org.exoplatform.wiki.service;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.utils.PageList;

public interface DataStorage {
  public PageList<SearchResult> search(ChromatticSession session, SearchData data) throws Exception ;
}
