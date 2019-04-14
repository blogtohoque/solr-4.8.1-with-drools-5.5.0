package com.lucid.rules;

import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.servlet.DirectSolrConnection;



public abstract class RulesEngineTestBase
  extends SolrTestCaseJ4
{
  public String update(String xml, String handlerName, SolrCore core)
  {
    DirectSolrConnection connection = new DirectSolrConnection(core);
    SolrRequestHandler handler = core.getRequestHandler(handlerName);
    try {
      return connection.request(handler, null, xml);
    } catch (SolrException e) {
      throw e;
    } catch (Exception e) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
    }
  }
}


