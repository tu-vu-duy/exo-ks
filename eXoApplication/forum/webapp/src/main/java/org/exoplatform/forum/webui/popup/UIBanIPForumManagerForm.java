/***************************************************************************
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
 ***************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicContainer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * 23-12-2008 - 04:17:18  
 */

@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIBanIPForumManagerForm.gtmpl",
		events = {
			@EventConfig(listeners = UIBanIPForumManagerForm.AddIpActionListener.class), 
			@EventConfig(listeners = UIBanIPForumManagerForm.OpenPostsActionListener.class), 
			@EventConfig(listeners = UIBanIPForumManagerForm.UnBanActionListener.class), 
			@EventConfig(listeners = UIBanIPForumManagerForm.CancelActionListener.class, phase=Phase.DECODE)
		}
)

public class UIBanIPForumManagerForm extends UIForm implements UIPopupComponent{
	public static final String NEW_IP_BAN_INPUT1 = "newIpBan1";
	public static final String NEW_IP_BAN_INPUT2 = "newIpBan2";
	public static final String NEW_IP_BAN_INPUT3 = "newIpBan3";
	public static final String NEW_IP_BAN_INPUT4 = "newIpBan4";
	private Forum forum ;
	public UIBanIPForumManagerForm() {
		addUIFormInput((new UIFormStringInput(NEW_IP_BAN_INPUT1, null)).setMaxLength(3));
		addUIFormInput((new UIFormStringInput(NEW_IP_BAN_INPUT2, null)).setMaxLength(3));
		addUIFormInput((new UIFormStringInput(NEW_IP_BAN_INPUT3, null)).setMaxLength(3));
		addUIFormInput((new UIFormStringInput(NEW_IP_BAN_INPUT4, null)).setMaxLength(3));
		setActions(new String[]{"Cancel"});
  }
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}

	public void setForum(Forum forum) {
	  this.forum = forum;
  }
	
	@SuppressWarnings("unused")
  private String[] getListIpBan() throws Exception {
		String[]ips = forum.getBanIP();
		if(ips == null) ips = new String[]{};
		return ips;
	}
	
	private String checkIpAddress(String[] ipAdd){
		String ip = "";
		try{
			int[] ips = new int[4];
			for(int t = 0; t < ipAdd.length; t ++){
				if(t>0) ip += ".";
				ip += ipAdd[t];
				ips[t] = Integer.parseInt(ipAdd[t]);
			}
			for(int i = 0; i < 4; i ++){
				if(ips[i] < 0 || ips[i] > 255) return null;
			}
			if(ips[0] == 255 && ips[1] == 255 && ips[2] == 255 && ips[3] == 255) return null;
			return ip;
		} catch (Exception e){
			return null;
		}
	}
	
	static	public class AddIpActionListener extends EventListener<UIBanIPForumManagerForm> {
		public void execute(Event<UIBanIPForumManagerForm> event) throws Exception {
			UIBanIPForumManagerForm ipManagerForm = event.getSource();
			String[] ip = new String[]{((UIFormStringInput)ipManagerForm.getChildById(NEW_IP_BAN_INPUT1)).getValue(),
																	((UIFormStringInput)ipManagerForm.getChildById(NEW_IP_BAN_INPUT2)).getValue(),
																	((UIFormStringInput)ipManagerForm.getChildById(NEW_IP_BAN_INPUT3)).getValue(),
																	((UIFormStringInput)ipManagerForm.getChildById(NEW_IP_BAN_INPUT4)).getValue(),
																	};
			UIApplication uiApp = ipManagerForm.getAncestorOfType(UIApplication.class) ;
			String ipAdd = ipManagerForm.checkIpAddress(ip);
			if(ipAdd == null){
				uiApp.addMessage(new ApplicationMessage("UIForumAdministrationForm.sms.ipInvalid", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return ;
			} 
			List<String> listIp = new ArrayList<String>();
			String[] ips = ipManagerForm.forum.getBanIP();
			if(ips != null && ips.length > 0){
				listIp.addAll(Arrays.asList(ipManagerForm.forum.getBanIP()));
			}
			if(listIp.contains(ipAdd)){
				uiApp.addMessage(new ApplicationMessage("UIBanIPForumManagerForm.sms.ipBanFalse", new Object[]{ipAdd}, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return;
			} else {
				ForumService fservice = (ForumService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class) ;
				listIp.add(ipAdd);
				ips = listIp.toArray(new String[]{});
				ipManagerForm.forum.setBanIP(ips);
				SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
				try {
					fservice.modifyForum(sProvider, ipManagerForm.forum, 3);
        } catch (Exception e) {
	        e.printStackTrace();
	        uiApp.addMessage(new ApplicationMessage("UIBanIPForumManagerForm.sms.ipBanFalse", new Object[]{ipAdd}, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
					return;
        }finally {
        	sProvider.close();
        }
			}
			UIForumPortlet forumPortlet = ipManagerForm.getAncestorOfType(UIForumPortlet.class);
			UITopicContainer topicContainer = forumPortlet.findFirstComponentOfType(UITopicContainer.class);
			topicContainer.setIdUpdate(true);
			event.getRequestContext().addUIComponentToUpdateByAjax(ipManagerForm) ;
		}
	}
	
	static	public class OpenPostsActionListener extends EventListener<UIBanIPForumManagerForm> {
		public void execute(Event<UIBanIPForumManagerForm> event) throws Exception {
			UIBanIPForumManagerForm ipManagerForm = event.getSource();
			String ip = event.getRequestContext().getRequestParameter(OBJECTID)	;
			UIPopupContainer popupContainer = ipManagerForm.getAncestorOfType(UIPopupContainer.class);
			UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
			UIPageListPostByIP viewPostedByUser = popupAction.activate(UIPageListPostByIP.class, 650) ;
			viewPostedByUser.setIp(ip) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static	public class UnBanActionListener extends EventListener<UIBanIPForumManagerForm> {
		public void execute(Event<UIBanIPForumManagerForm> event) throws Exception {
			UIBanIPForumManagerForm ipManagerForm = event.getSource();
			String ip = event.getRequestContext().getRequestParameter(OBJECTID)	;
			List<String> listIp = new ArrayList<String>();
			String[] ips = ipManagerForm.forum.getBanIP();
			if(ips != null && ips.length > 0){
				listIp.addAll(Arrays.asList(ipManagerForm.forum.getBanIP()));
				if(listIp.contains(ip)) {
					ForumService fservice = (ForumService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class) ;
					listIp.remove(ip);
					ips = listIp.toArray(new String[]{});
					ipManagerForm.forum.setBanIP(ips);
					SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
					try {
						fservice.modifyForum(sProvider, ipManagerForm.forum, 3);
	        } catch (Exception e) {
		        e.printStackTrace();
		        UIApplication uiApp = ipManagerForm.getAncestorOfType(UIApplication.class) ;
		        uiApp.addMessage(new ApplicationMessage("UIForumAdministrationForm.sms.ipBanRemoveFalse", new Object[]{ip}, ApplicationMessage.WARNING)) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
						return;
	        }finally {
	        	sProvider.close();
	        }
				}
			}
			UIForumPortlet forumPortlet = ipManagerForm.getAncestorOfType(UIForumPortlet.class);
			UITopicContainer topicContainer = forumPortlet.findFirstComponentOfType(UITopicContainer.class);
			topicContainer.setIdUpdate(true);
			event.getRequestContext().addUIComponentToUpdateByAjax(ipManagerForm) ;
		}
	}
	
	static	public class CancelActionListener extends EventListener<UIBanIPForumManagerForm> {
		public void execute(Event<UIBanIPForumManagerForm> event) throws Exception {
			UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}
	
	
	
}
