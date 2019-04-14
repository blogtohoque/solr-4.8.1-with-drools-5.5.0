package com.lucid.rules;

import org.apache.solr.util.TestHarness;

public class EmptyRulesTest extends RulesEngineTestBase
{
  @org.junit.BeforeClass
  public static void beforeClass() throws Exception {
    initCore("solrconfig-rules.xml", "schema.xml");
  }
  
  public void test()
  {
    RulesEngineManagerHandler handler = (RulesEngineManagerHandler)h.getCore().getRequestHandler("/rulesMgr");
    assertNotNull(handler);
    assertNotNull(handler.getEngine("empty"));
    assertTrue(handler.getEngine("empty") instanceof NoopRulesEngine);
    


    update(adoc(new String[] { "id", "0", "cat_s", "a", "val_s", "AAA", "sort_i", "4", "sort_f", "3.0" }), "/update/rules", h.getCore());
    update(adoc(new String[] { "id", "1", "cat_s", "b", "val_s", "BBB", "sort_i", "3", "sort_f", "4.0" }), "/update/rules", h.getCore());
    update(adoc(new String[] { "id", "2", "cat_s", "leap", "title", "Fox leaps over dogs!", "body", "The quick red fox jumped over the lazy brown dogs.", "sort_i", "2", "sort_f", "1.0" }), "/update/rules", h.getCore());
    update(adoc(new String[] { "id", "3", "cat_s", "cool", "title", "Fox fox fox fox", "body", "foxes are cool, foxes are red, foxes are the best.", "sort_i", "1", "sort_f", "2.0" }), "/update/rules", h.getCore());
    update(commit(new String[0]), "/update/rules", h.getCore());
    
    assertQ(req(new String[] { "qt", "/lucidRules", "q", "id:0" }), new String[] { "//*[@numFound='1']" });
    

    assertQ(req(new String[] { "qt", "/lucidRules", "q", "val_s:AAA" }), new String[] { "//*[@numFound='1']" });
  }
}


