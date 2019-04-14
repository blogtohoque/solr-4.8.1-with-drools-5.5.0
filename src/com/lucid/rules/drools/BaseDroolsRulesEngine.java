package com.lucid.rules.drools;

import com.lucid.rules.RulesEngine;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.DatatypeConverter;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.update.AddUpdateCommand;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.command.Command;
import org.drools.definition.KnowledgePackage;
import org.drools.io.Resource;
import org.drools.io.impl.InputStreamResource;
import org.drools.runtime.CommandExecutor;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public abstract class BaseDroolsRulesEngine
  extends RulesEngine
{
  private static transient Logger log = LoggerFactory.getLogger(BaseDroolsRulesEngine.class);
  
  public static final String RULES_TAG = "rules";
  public static final String FILE_TAG = "file";
  public static final String GUVNOR_URL_TAG = "guvnorUrl";
  public static final String GUVNOR_PACKAGE_TAG = "guvnorPackage";
  public static final String GUVNOR_USERNAME_TAG = "guvnorUsername";
  public static final String GUVNOR_PASSWORD_TAG = "guvnorPassword";
  public static final String FACT_COLLECTOR_TAG = "factCollector";
  protected KnowledgeBase kbase;
  protected IndexSchema schema;
  protected boolean reload = false;
  

  protected FactCollector factCollector;
  


  public void init(String engineName, NamedList args, SolrCore core)
    throws Exception
  {
    super.init(engineName, args, core);
    createKnowledgeBase(core);
    createFactCollector((NamedList)args.get("factCollector"), core);
  }
  
  protected void createFactCollector(NamedList args, SolrCore core) throws IllegalAccessException, InstantiationException { String className;
    String className1=null;
    
    if (args != null) {
      Object arg = args.get("class");
      
      //String className;
      
      if (arg == null) {
        className1 = FactCollector.class.getName();
      } else {
        className1 = arg.toString();
      }
    } else {
      className1 = FactCollector.class.getName();
    }
    log.info("Instantiating FactCollector: " + className1);
    this.factCollector = ((FactCollector)core.getResourceLoader().findClass(className1, FactCollector.class).newInstance());
    this.factCollector.init(args, core);
  }
  
  static String utilNamedListToTrimmedStringOrNull(NamedList inVal, String targetName) {
    if (null == inVal) {
      return null;
    }
    Object o = inVal.get(targetName);
    if (null == o) {
      return null;
    }
    if ((o instanceof String)) {
      return utilTrimmedStringOrNull((String)o);
    }
    return utilNamedListToTrimmedStringOrNull((NamedList)o);
  }
  
  static String utilNamedListToTrimmedStringOrNull(NamedList inVal) { if (null == inVal) {
      return null;
    }
    return utilTrimmedStringOrNull(inVal.toString());
  }
  
  static String utilTrimmedStringOrNull(String inVal) { if (null == inVal) {
      return null;
    }
    inVal = inVal.trim();
    if (inVal.length() > 1) {
      return inVal;
    }
    
    return null;
  }
  
  static String fetchUrlAsString(String urlStr, String optUsername, String optPassword) throws IOException {
    int CONNECT_TIMEOUT_MS = 60000;
    int READ_TIMEOUT_MS = 60000;
    String ENCODING = "UTF-8";
    URL url = new URL(urlStr);
    URLConnection conn = url.openConnection();
    conn.setConnectTimeout(60000);
    conn.setReadTimeout(60000);
    if ((null != optUsername) && (null != optPassword)) {
      String userpass = optUsername + ":" + optPassword;
      String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());
      conn.setRequestProperty("Authorization", basicAuth);
    }
    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
    
    StringBuffer buff = new StringBuffer();
    String line; while (null != (line = in.readLine())) {
      buff.append(line).append("\r\n");
    }
    in.close();
    return new String(buff);
  }
  
  String featchRulesFromGuvnor(String guvnorUrl, String guvnorPackage, String optUsername, String optPassword)
    throws IOException
  {
    guvnorUrl = guvnorUrl + "/";
    if (guvnorUrl.indexOf("package") < 0) {
      guvnorUrl = guvnorUrl + "package/";
    }
    guvnorUrl = guvnorUrl + guvnorPackage + "/LATEST.drl";
    return fetchUrlAsString(guvnorUrl, optUsername, optPassword);
  }
  
  protected void createKnowledgeBase(SolrCore core) throws IOException { KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
    NamedList rules = (NamedList)this.initArgs.get("rules");
    this.hasRules = true;
    if ((rules != null) && (rules.size() > 0))
    {
      List all = rules.getAll("file");
      if ((all != null) && (all.size() > 0)) {
        for (Object o : all) {
          String file = (String)o;
          log.info("Loading rules from file: " + file);
          SolrResourceLoader solrLoader = core.getResourceLoader();
          InputStream ruleRes = solrLoader.openResource(file);
          
          if (ruleRes != null) {
            Resource droolsResource = new InputStreamResource(ruleRes);
            kbuilder.add(droolsResource, ResourceType.DRL);
            
            if (kbuilder.hasErrors()) {
              log.error(kbuilder.getErrors().toString());
              throw new RuntimeException("Error constructing the rules KnowledgeBase from file \"" + file + "\"");
            }
          } else {
            throw new IOException("Can't load rule file (" + file + ") from classpath using the SolrResourceLoader");
          }
        }
      }
      else
      {
        String guvnorUrl = utilNamedListToTrimmedStringOrNull(rules, "guvnorUrl");
        String guvnorPackage = utilNamedListToTrimmedStringOrNull(rules, "guvnorPackage");
        
        if ((null != guvnorUrl) || (null != guvnorPackage)) {
          if ((null == guvnorUrl) || (null == guvnorPackage)) {
            throw new IllegalArgumentException("When configuring Gurnor, must specify both guvnorUrl and guvnorPackage");
          }
          
          String guvnorUsername = utilNamedListToTrimmedStringOrNull(rules, "guvnorUsername");
          String guvnorPassword = utilNamedListToTrimmedStringOrNull(rules, "guvnorPassword");
          
          if (((null != guvnorUsername) && (null == guvnorPassword)) || ((null != guvnorUsername) && (null == guvnorPassword))) {
            throw new IllegalArgumentException("When configuring Gurnor basic authentication, must specify both guvnorUsername and guvnorPassword");
          }
          
          log.info("Loading rules from Guvnor: \"" + guvnorUrl + "\" for package \"" + guvnorPackage + "\"");
          String rulesSrc = featchRulesFromGuvnor(guvnorUrl, guvnorPackage, guvnorUsername, guvnorPassword);
          InputStream is = new ByteArrayInputStream(rulesSrc.getBytes("UTF-8"));
          Resource droolsResource = new InputStreamResource(is);
          kbuilder.add(droolsResource, ResourceType.DRL);
          if (kbuilder.hasErrors()) {
            log.error(kbuilder.getErrors().toString());
            throw new RuntimeException("Error constructing the rules KnowledgeBase from Guvnor \"" + guvnorUrl + "\" for package \"" + guvnorPackage + "\"");
          }
        }
        else
        {
          log.warn("No file nor guvnorUrl set to read rules from");
        }
      }
      

      if (emptyRulesSet(kbuilder)) {
        log.info("No rules defined for Drools engine");
        this.hasRules = false;
        this.kbase = null;
        return;
      }
    }
    else {
      log.info("No rules defined for Drools engine");
      this.hasRules = false;
      this.kbase = null;
      return;
    }
    this.kbase = KnowledgeBaseFactory.newKnowledgeBase();
    this.kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
  }
  
  private boolean emptyRulesSet(KnowledgeBuilder kbuilder) {
    boolean foundRules = false;
    Iterator<KnowledgePackage> kpackages = kbuilder.getKnowledgePackages().iterator();
    while ((kpackages.hasNext()) && (!foundRules)) {
      foundRules = !((KnowledgePackage)kpackages.next()).getRules().isEmpty();
    }
    return !foundRules;
  }
  
  protected abstract CommandExecutor getSession();
  
  protected StatelessKnowledgeSession createStatelessSession() {
    return this.kbase.newStatelessKnowledgeSession();
  }
  





  protected StatefulKnowledgeSession createStatefulSession()
  {
    return this.kbase.newStatefulKnowledgeSession();
  }
  
  protected abstract void execute(CommandExecutor paramCommandExecutor, List<Command<?>> paramList, Collection<?> paramCollection);
  
  public void findLandingPage(ResponseBuilder rb)
  {
    log.info("Executing findLandingPage in engine: " + this.engineName);
    CommandExecutor ksession = getSession();
    processRules(rb, ksession);
  }
  

  public void prepareSearch(ResponseBuilder rb)
  {
    log.info("Executing prepareSearch in engine: " + this.engineName);
    CommandExecutor ksession = getSession();
    processRules(rb, ksession);
  }
  
  protected void processRules(ResponseBuilder rb, CommandExecutor ksession) {
    List<Command<?>> cmds = new ArrayList();
    Collection<?> facts = new ArrayList();
    this.factCollector.addFacts(rb, facts);
    execute(ksession, cmds, facts);
  }
  
  public void postSearch(ResponseBuilder rb)
  {
    log.info("Executing postSearch in engine: " + this.engineName);
    CommandExecutor ksession = getSession();
    processRules(rb, ksession);
  }
  
  public void transformDocument(SolrDocument doc, int docId, IndexSchema schema)
  {
    CommandExecutor ksession = getSession();
    List<Command<?>> cmds = new ArrayList();
    Collection<?> facts = new ArrayList();
    this.factCollector.addFacts(doc, docId, facts, schema);
    execute(ksession, cmds, facts);
  }
  
  public void prepareDocument(AddUpdateCommand addUpCmd)
  {
    List<Command<?>> cmds = new ArrayList();
    CommandExecutor ksession = getSession();
    Collection<Object> facts = new ArrayList();
    this.factCollector.addFacts(addUpCmd, facts);
    execute(ksession, cmds, facts);
  }
}


