package com.lucid.rules;

import org.apache.solr.util.TestHarness;
import org.junit.BeforeClass;
import org.junit.Test;

public class RulesComponentTest
  extends RulesEngineTestBase
{
  @BeforeClass
  public static void beforeClass()
    throws Exception
  {
    initCore("solrconfig.xml", "schema.xml");
  }
  
  @Test
  public void testInputParams()
    throws Exception
  {
    update(adoc(new String[] { "id", "0", "cat_s", "a", "val_s", "AAA", "sort_i", "4", "sort_f", "3.0" }), "/update/rules", h.getCore());
    update(adoc(new String[] { "id", "1", "cat_s", "b", "val_s", "BBB", "sort_i", "3", "sort_f", "4.0" }), "/update/rules", h.getCore());
    update(adoc(new String[] { "id", "2", "cat_s", "leap", "title", "Fox leaps over dogs!", "body", "The quick red fox jumped over the lazy brown dogs.", "sort_i", "2", "sort_f", "1.0" }), "/update/rules", h.getCore());
    update(adoc(new String[] { "id", "3", "cat_s", "cool", "title", "Fox fox fox fox", "body", "foxes are cool, foxes are red, foxes are the best.", "sort_i", "1", "sort_f", "2.0" }), "/update/rules", h.getCore());
    update(commit(new String[0]), "/update/rules", h.getCore());
    
    assertQ(req(new String[] { "qt", "/lucidRules", "q", "id:0" }), new String[] { "//*[@numFound='1']", "//str[@name='hello'][.='world']", "//str[@name='good'][.='bye']", "//str[@name='first'][.='prepare']", "//str[@name='first'][.='process']" });
    







    assertQ(req(new String[] { "qt", "/lucidRules", "q", "id:0", "rules", "false" }), new String[] { "//*[@numFound='1']", "not(//str[@name='hello'][.='world'])", "not(//str[@name='good'][.='bye'])", "not(//str[@name='first'][.='prepare'])", "not(//str[@name='first'][.='process'])" });
    





    assertQ(req(new String[] { "qt", "/lucidRules", "q", "id:0", "rules.first", "false" }), new String[] { "//*[@numFound='1']", "not(//str[@name='hello'][.='world'])", "//str[@name='good'][.='bye']", "not(//str[@name='first'][.='prepare'])", "not(//str[@name='first'][.='process'])" });
    






    assertQ(req(new String[] { "qt", "/lucidRules", "q", "id:0", "rules.prepare", "true", "rules.process", "false", "rules.first", "true" }), new String[] { "//*[@numFound='1']", "//str[@name='hello'][.='world']", "//str[@name='good'][.='bye']", "//str[@name='first'][.='prepare']", "not(//str[@name='first'][.='process'])" });
    










    assertQ(req(new String[] { "qt", "/lucidRules", "q", "id:0", "rules.prepare", "false", "rules.process", "true", "rules.first", "true" }), new String[] { "//*[@numFound='1']", "//str[@name='hello'][.='world']", "//str[@name='good'][.='bye']", "not(//str[@name='first'][.='prepare'])", "//str[@name='first'][.='process']" });
    










    assertQ(req(new String[] { "qt", "/lucidRules", "q", "id:0", "rules.last", "false" }), new String[] { "//*[@numFound='1']", "//str[@name='hello'][.='world']", "not(//str[@name='good'][.='bye'])", "//str[@name='first'][.='prepare']", "//str[@name='first'][.='process']" });
  }
}


