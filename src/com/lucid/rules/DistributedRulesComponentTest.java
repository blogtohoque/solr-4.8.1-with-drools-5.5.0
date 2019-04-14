package com.lucid.rules;

import java.io.PrintStream;
import org.apache.solr.BaseDistributedSearchTestCase;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;





public class DistributedRulesComponentTest
  extends BaseDistributedSearchTestCase
{
  public void doTest()
    throws Exception
  {
    indexr(new Object[] { this.id, "0", "cat_s", "a", "val_s", "AAA", "sort_i", "4", "sort_f", "3.0" });
    indexr(new Object[] { this.id, "1", "cat_s", "b", "val_s", "BBB", "sort_i", "3", "sort_f", "4.0" });
    indexr(new Object[] { this.id, "2", "cat_s", "leap", "title", "Fox leaps over dogs!", "body", "The quick red fox jumped over the lazy brown dogs.", "sort_i", "2", "sort_f", "1.0" });
    indexr(new Object[] { this.id, "3", "cat_s", "cool", "title", "Fox fox fox fox", "body", "foxes are cool, foxes are red, foxes are the best.", "sort_i", "1", "sort_f", "2.0" });
    commit();
    
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.add("qt", new String[] { "/lucidRules" });
    params.add("q", new String[] { "id:0" });
    params.add("rules.prepare", new String[] { "false" });
    params.add("rules.process", new String[] { "false" });
    params.add("rules.finishStage", new String[] { "true" });
    params.add("rules.first", new String[] { "true" });
    params.set("shards", new String[] { getShardsString() });
    





    QueryResponse response = queryServer(params);
    System.out.println("Results: " + response);
    assertEquals(1L, response.getResults().getNumFound());
    assertEquals("finish", response.getResponse().get("first"));
  }
}


