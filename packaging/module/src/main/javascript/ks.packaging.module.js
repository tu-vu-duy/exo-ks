eXo.require("eXo.projects.Module");
eXo.require("eXo.projects.Product");

function getModule(params)
{


   var ws = params.ws;
   var portal = params.portal;
   var module = new Module();

   module.version = "${project.version}"; 
   module.relativeMavenRepo = "org/exoplatform/ks";
   module.relativeSRCRepo = "ks";
   module.name = "ks";
 
  	// COMET (required by KS)
  	// TODO, should be passed in params and have its own module .js definition 
  var cometVersion = "${org.exoplatform.commons.version}";
  module.comet = {};

    
  module.comet.cometd =
	new Project("org.exoplatform.commons", "exo.platform.commons.comet.webapp", "war", cometVersion).
    addDependency(new Project("org.mortbay.jetty", "cometd-bayeux", "jar", "6.1.11")).
	addDependency(new Project("org.mortbay.jetty", "jetty-util", "jar", "6.1.11")).
	addDependency(new Project("org.mortbay.jetty", "cometd-api", "jar", "0.9.20080221")).
	addDependency(new Project("org.exoplatform.commons", "exo.platform.commons.comet.service", "jar", cometVersion));  	
	module.comet.cometd.deployName = "cometd";
  // KS

  // KS components
  module.component = {};
  module.component.common = new Project("org.exoplatform.ks", "exo.ks.component.common","jar", module.version);
  module.component.rendering =  new Project("org.exoplatform.ks", "exo.ks.component.rendering","jar", module.version);
  module.component.bbcode =  new Project("org.exoplatform.ks", "exo.ks.component.bbcode","jar", module.version);
  
	
  // KS apps
  module.eXoApplication = {};
  module.eXoApplication.common = new Project("org.exoplatform.ks", "exo.ks.eXoApplication.common","jar", module.version);

  
  // FAQ
  module.eXoApplication.faq = 
    new Project("org.exoplatform.ks", "exo.ks.eXoApplication.faq.webapp", "war", module.version).
      addDependency(new Project("rome", "rome", "jar", "0.9")).
	  addDependency(new Project("jdom", "jdom", "jar", "1.0")).
	  addDependency(new Project("org.exoplatform.ks", "exo.ks.eXoApplication.faq.service", "jar",  module.version));
	  
  module.eXoApplication.faq.deployName = "faq";

  // FORUM
  module.eXoApplication.forum = 
    new Project("org.exoplatform.ks", "exo.ks.eXoApplication.forum.webapp", "war", module.version).       
	addDependency(ws.frameworks.json).
	addDependency(module.comet.cometd).
    addDependency(new Project("org.exoplatform.ks", "exo.ks.eXoApplication.forum.service", "jar",  module.version));
    
  module.eXoApplication.forum.deployName = "forum";
  
  //WIKI
  module.eXoApplication.wiki = 
    new Project("org.exoplatform.ks", "exo.ks.eXoApplication.wiki.webapp", "war", "2.0.0-SNAPSHOT").
    addDependency(new Project("org.exoplatform.ks", "exo.ks.eXoApplication.wiki.service", "jar",  "2.0.0-SNAPSHOT")).
	addDependency(new Project("org.exoplatform.platform", "exo.platform.commons.webui.ext", "jar",  "3.0.0-Alpha04-SNAPSHOT")).
	addDependency(new Project("com.google.gwt", "gwt-servlet", "jar",  "2.0.0")).
	addDependency(new Project("com.google.gwt", "gwt-user", "jar",  "2.0.0")).
	addDependency(new Project("org.xwiki.platform", "xwiki-web-gwt-wysiwyg-client", "jar",  "2.3.1")).
	addDependency(new Project("org.apache.pdfbox", "fontbox", "jar",  "1.1.0")).
	addDependency(new Project("javax.validation", "validation-api", "jar",  "1.0.0.GA")).
	addDependency(new Project("org.hibernate", "hibernate-validator", "jar",  "4.0.0.GA")).
	addDependency(new Project("org.python", "jython-standalone", "jar",  "2.5.1-xwiki")).
	addDependency(new Project("pygments", "pygments", "jar",  "1.3.1-xwiki")).
	addDependency(new Project("net.sourceforge.htmlcleaner", "htmlcleaner", "jar",  "2.1")).
	addDependency(new Project("org.xwiki.platform", "xwiki-core-configuration-api", "jar",  "2.3.1")).
	addDependency(new Project("org.xwiki.platform", "xwiki-core-model", "jar",  "2.3.1")).
	addDependency(new Project("org.xwiki.platform", "xwiki-core-context", "jar",  "2.3.1")).
	addDependency(new Project("org.xwiki.platform", "xwiki-core-component-api", "jar",  "2.3.1")).
	addDependency(new Project("org.xwiki.platform", "xwiki-core-properties", "jar",  "2.3.1")).
	addDependency(new Project("org.xwiki.platform", "xwiki-core-xml", "jar",  "2.3.1")).
	addDependency(new Project("org.xwiki.platform", "xwiki-core-script", "jar",  "2.3.1")).
	addDependency(new Project("org.xwiki.platform", "xwiki-core-rendering-api", "jar",  "2.3.1")).
	addDependency(new Project("org.xwiki.platform", "xwiki-core-component-default", "jar",  "2.3.1")).
	addDependency(new Project("org.xwiki.platform", "xwiki-core-rendering-syntax-wikimodel", "jar",  "2.3.1")).
	addDependency(new Project("org.xwiki.platform", "xwiki-core-rendering-macro-box", "jar",  "2.3.1")).
	addDependency(new Project("org.xwiki.platform", "xwiki-core-rendering-macro-message", "jar",  "2.3.1")).
	addDependency(new Project("org.xwiki.platform", "xwiki-core-rendering-macro-code", "jar",  "2.3.1")).
	addDependency(new Project("org.xwiki.contrib", "xwiki-macro-column", "jar",  "1.1-SNAPSHOT")).
	addDependency(new Project("org.wikimodel", "org.wikimodel.wem", "jar",  "2.0.7-20100629-1")).
	addDependency(new Project("org.suigeneris", "jrcs.diff", "jar",  "0.4.2")).
	addDependency(new Project("org.suigeneris", "jrcs.rcs", "jar",  "0.4.2")).
	addDependency(new Project("ecs", "ecs", "jar",  "1.4.2"));
    
  module.eXoApplication.wiki.deployName = "wiki";

  // CRASH
  module.eXoApplication.crash = 
    new Project("org.crsh", "crsh", "war", "1.0.0-beta4");
    
  module.eXoApplication.crash.deployName = "crash";
  
  // KS we resources and services
  module.web = {}
  module.web.ksResources = 
    new Project("org.exoplatform.ks", "exo.ks.web.ksResources", "war", module.version) ;

   // KS extension for tomcat 
   module.extension = {};
   module.extension.webapp = new Project("org.exoplatform.ks", "exo.ks.extension.webapp", "war", module.version).
   addDependency(new Project("org.exoplatform.ks", "exo.ks.extension.config", "jar", module.version));
   module.extension.webapp.deployName = "ks-extension";
   
   module.server = {}
   module.server.tomcat = {}
   module.server.tomcat.patch =
	new Project("org.exoplatform.ks", "exo.ks.server.tomcat.patch", "jar", module.version);
	
   module.server.jboss = {}
   module.server.jboss.patchear =
	new Project("org.exoplatform.ks", "exo.ks.server.jboss.patch-ear", "jar", module.version);
   
  // KS demo 
   module.demo = {};
   // demo portal
   module.demo.portal = 
	   new Project("org.exoplatform.ks", "exo.ks.demo.webapp", "war", module.version).
	   addDependency(new Project("org.exoplatform.ks", "exo.ks.demo.config", "jar", module.version));
	   module.demo.portal.deployName = "ksdemo";  
	
	module.demo.cometd=
	new Project("org.exoplatform.ks", "exo.ks.demo.cometd-war", "war", module.version);  	
	module.demo.cometd.deployName = "cometd-ksdemo";
	   
   // demo rest endpoint	   
   module.demo.rest = 
       new Project("org.exoplatform.ks", "exo.ks.demo.rest-ksdemo", "war", module.version);
       module.extension.deployName = "rest-ksdemo"; 
       
       
   
   return module;
}
