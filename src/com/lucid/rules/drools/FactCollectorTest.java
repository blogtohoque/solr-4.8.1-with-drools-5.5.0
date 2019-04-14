package com.lucid.rules.drools;

import com.lucid.rules.RulesEngineTestBase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.util.TestHarness;
import org.junit.BeforeClass;
import org.junit.Test;

public class FactCollectorTest
  extends RulesEngineTestBase
{
  @BeforeClass
  public static void beforeClass() throws Exception
  {
    initCore("solrconfig.xml", "schema.xml");
  }
  
  @Test
  public void testFactCollector() throws Exception {
    FactCollector fc = new FactCollector();
    NamedList args = new NamedList();
    fc.init(args, h.getCore());
    Collection facts = new ArrayList();
    

    SolrQueryRequest req = req(new String[] { "q", "*:*" });
    req.setParams(new ModifiableSolrParams(req.getParams()));
    
    ResponseBuilder rb = new ResponseBuilder(req, new SolrQueryResponse(), new ArrayList(h.getCore().getSearchComponents().values()));
    
    rb.setQuery(new MatchAllDocsQuery());
    fc.addFacts(rb, facts);
    


    String qStr = "lastModified:[NOW-1HOUR+TO+NOW]";
    req = req(new String[] { "q", qStr });
    req.setParams(new ModifiableSolrParams(req.getParams()));
    QParserPlugin qp = h.getCore().getQueryPlugin("edismax");
    QParser parser = qp.createParser(qStr, req.getParams(), req.getParams(), req);
    rb = new ResponseBuilder(req, new SolrQueryResponse(), new ArrayList(h.getCore().getSearchComponents().values()));
    
    rb.setQuery(parser.parse());
    facts = new ArrayList();
    try {
      fc.addFacts(rb, facts);
    } catch (UnsupportedOperationException e) {
      assertTrue(false);
    }
  }
}


