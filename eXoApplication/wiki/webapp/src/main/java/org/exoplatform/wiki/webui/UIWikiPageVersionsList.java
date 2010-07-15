/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wiki.webui;

import java.util.List;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.wiki.chromattic.ext.ntdef.NTFrozenNode;
import org.exoplatform.wiki.chromattic.ext.ntdef.NTVersion;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.content.ContentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jul 13, 2010  
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiPageVersionsList.gtmpl",
  events = {
    @EventConfig(listeners = UIWikiPageVersionsList.RestoreActionListener.class),
    @EventConfig(listeners = UIWikiPageVersionsList.ViewRevisionActionListener.class)
  }
)
public class UIWikiPageVersionsList extends UIForm {

  private List<NTVersion>    versionsList;

  public static final String RESTORE_ACTION = "Restore";

  public static final String VIEW_REVISION  = "ViewRevision";

  public List<NTVersion> getVersionsList() {
    return versionsList;
  }

  public void setVersionsList(List<NTVersion> versionsList) {
    this.versionsList = versionsList;
  }

  static public class RestoreActionListener extends EventListener<UIWikiPageVersionsList> {
    @Override
    public void execute(Event<UIWikiPageVersionsList> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      String versionName = event.getRequestContext().getRequestParameter(OBJECTID);
      PageImpl wikipage = (PageImpl) Utils.getCurrentWikiPage();
      wikipage.restore(versionName, false);
      wikipage.checkout();
      wikiPortlet.changeMode(WikiMode.VIEW);
    }
  }

  static public class ViewRevisionActionListener extends EventListener<UIWikiPageVersionsList> {
    @Override
    public void execute(Event<UIWikiPageVersionsList> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIWikiPageContentArea pageContentArea = wikiPortlet.findFirstComponentOfType(UIWikiPageContentArea.class);
      String versionName = event.getRequestContext().getRequestParameter(OBJECTID);
      PageImpl wikipage = (PageImpl) Utils.getCurrentWikiPage();
      NTVersion version = wikipage.getVersionableMixin().getVersionHistory().getVersion(versionName);
      NTFrozenNode frozenNode = version.getNTFrozenNode();
      ContentImpl content = (ContentImpl) (frozenNode.getChildren().get(WikiNodeType.Definition.CONTENT));
      String pageContent = content.getText();
      String pageSyntax = content.getSyntax();
      pageContentArea.renderWikiMarkup(pageContent, pageSyntax);
      wikiPortlet.changeMode(WikiMode.VIEW);
    }
  }

}
