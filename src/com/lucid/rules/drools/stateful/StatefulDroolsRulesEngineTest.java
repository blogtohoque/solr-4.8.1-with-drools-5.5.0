package com.lucid.rules.drools.stateful;

import com.lucid.rules.RulesEngineManagerHandler;
import java.util.concurrent.BlockingQueue;
import org.apache.solr.util.TestHarness;

public class StatefulDroolsRulesEngineTest extends com.lucid.rules.RulesEngineTestBase
{
  @org.junit.BeforeClass
  public static void beforeClass() throws Exception
  {
    initCore("solrconfig-rules-stateful.xml", "schema.xml");
  }
  
  @org.junit.Test
  public void testInputParams()
    throws Exception
  {
    update(adoc(new String[] { "id", "0", "cat_s", "a", "val_s", "AAA", "sort_i", "4", "sort_f", "3.0" }), "/update/rules", h.getCore());
    update(adoc(new String[] { "id", "1", "cat_s", "b", "val_s", "BBB", "sort_i", "3", "sort_f", "4.0" }), "/update/rules", h.getCore());
    update(adoc(new String[] { "id", "2", "cat_s", "leap", "title", "Fox leaps over dogs! Foxy!", "body", "The quick red fox jumped over the lazy brown dogs.", "sort_i", "2", "sort_f", "1.0" }), "/update/rules", h.getCore());
    update(adoc(new String[] { "id", "3", "cat_s", "cool", "title", "Fox fox fox fox foxy", "body", "foxes are cool, foxes are red, foxes are the best.", "sort_i", "1", "sort_f", "2.0" }), "/update/rules", h.getCore());
    update(commit(new String[0]), "/update/rules", h.getCore());
    
    assertQ(req(new String[] { "qt", "standard", "q", "id:0" }), new String[] { "//*[@numFound='1']", "//str[@name='foo_s'][.='bar']" });
    



    assertQ(req(new String[] { "qt", "/lucidRules", "q", "*:*" }), new String[] { "//*[@numFound='4']" });
    


    assertQ(req(new String[] { "qt", "/lucidRules", "q", "id:0" }), new String[] { "//*[@numFound='1']", "//str[@name='hello'][.='world']", "//str[@name='good'][.='bye']" });
    



    assertQ(req(new String[] { "qt", "/lucidRules", "q", "title:fox" }), new String[] { "//*[@numFound='2']" });
    


    assertQ(req(new String[] { "qt", "/lucidRules", "q", "title:foxy" }), new String[] { "//*[@numFound='2']", "//str[@name='landingPage'][.='http://www.fox.com']" });
    



    assertQ(req(new String[] { "qt", "/lucidRules", "q", "foo_s:bar" }), new String[] { "//*[@numFound='1']" });
    


    assertQ(req(new String[] { "qt", "/lucidRules", "q", "id:1", "fl", "*, [rules], score" }), new String[] { "//*[@numFound='1']", "//str[@name='foo'][.='bar']" });
    



    assertQ(req(new String[] { "qt", "/lucidRules", "q", "title:\"cool foxes\"", "fl", "cat_s", "echoParams", "all" }), new String[] { "//*[@numFound='1']", "//str[@name='q'][.='title:foxes +cat_s:cool']", "//str[@name='modQuery'][.='title:foxes +cat_s:cool']", "//str[@name='origQuery'][.='title:\"cool foxes\"']", "//str[@name='cat_s'][.='cool']" });
    







    assertQ(req(new String[] { "qt", "/lucidRules", "q", "*:*", "facet", "true", "facet.field", "cat_s", "fl", "cat_s", "sort", "sort_i", "facet.limit", "3", "echoParams", "all" }), new String[] { "//*[@numFound='4']", "//str[@name='modFacet'][.='cat_s']", "//str[@name='facetMerged'][.='cat_s']", "//str[@name='facet.query'][.='val_s:AAA']", "*[count(//lst[@name='cat_s']/int)=2]", "//lst[@name='facet_queries']/int[@name='val_s:AAA'][.='1']", "//lst[@name='cat_s']/int[@name='butter'][.='27']", "//str[@name='sort'][.='sort_f desc']" });
  }
  















  public void testPoolSize()
  {
    RulesEngineManagerHandler handler = (RulesEngineManagerHandler)h.getCore().getRequestHandler("/rulesMgr");
    assertEquals(((StatefulPoolDroolsRulesEngine)handler.getEngine("docs")).ksessions.size(), 20L);
    assertEquals(((StatefulPoolDroolsRulesEngine)handler.getEngine("first")).ksessions.size(), 10L);
  }
}


