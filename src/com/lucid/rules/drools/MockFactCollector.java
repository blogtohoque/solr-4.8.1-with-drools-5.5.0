package com.lucid.rules.drools;

import java.util.Collection;
import org.apache.solr.handler.component.ResponseBuilder;





public class MockFactCollector
  extends FactCollector
{
  public void addFacts(ResponseBuilder rb, Collection facts)
  {
    super.addFacts(rb, facts);
    facts.add("MockFact");
  }
}


