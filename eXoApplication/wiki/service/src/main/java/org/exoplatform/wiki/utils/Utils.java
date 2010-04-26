package org.exoplatform.wiki.utils;

import org.exoplatform.wiki.mow.api.WikiNodeType;

public class Utils {
  
  //The path should get from NodeHierarchyCreator 
  public static String getPortalWikisPath() {    
    String path = "/exo:applications/" 
    + WikiNodeType.Definition.WIKI_APPLICATION + "/"
    + WikiNodeType.Definition.WIKIS ; 
    return path ;
  }
}
