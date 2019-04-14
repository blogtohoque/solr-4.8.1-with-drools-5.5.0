package com.lucid.rules;

import java.io.IOException;
import java.util.Map;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.transform.DocTransformer;
import org.apache.solr.response.transform.TransformerFactory;
import org.apache.solr.schema.IndexSchema;





public class RulesDocTransformerFactory
  extends TransformerFactory
{
  protected String handlerName;
  protected String engineName;
  protected String handle;
  
  public void init(NamedList args)
  {
    super.init(args);
    SolrParams params = SolrParams.toSolrParams(args);
    this.handlerName = params.get("requestHandler");
    if ((this.handlerName == null) || (this.handlerName.equals(""))) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Unable to determine RulesEngineManagerHandler for requestHandler=" + this.handlerName);
    }
    this.engineName = params.get("engine");
    if (this.engineName == null) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Unable to determine engineName for=" + this.engineName);
    }
    this.handle = params.get("handle");
  }
  
  public DocTransformer create(String field, SolrParams params, SolrQueryRequest req)
  {
    RulesEngine engine = RulesHelper.getEngine(req.getCore(), this.handlerName, this.engineName);
    if (engine != null) {
      return new RulesDocTransformer(field, engine, req);
    }
    throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Unable to find engine with name " + this.engineName + " on the Request Handler named: " + this.handlerName);
  }
  
  class RulesDocTransformer extends DocTransformer {
    private String name;
    private RulesEngine engine;
    private SolrQueryRequest req;
    
    RulesDocTransformer(String name, RulesEngine engine, SolrQueryRequest req) {
      this.name = name;
      this.engine = engine;
      this.req = req;
    }
    
    public String getName()
    {
      return this.name;
    }
    
    public void transform(SolrDocument doc, int docid) throws IOException
    {
      try
      {
        this.req.getContext().put("rulesPhase", "docTransformation");
        this.req.getContext().put("rulesHandle", RulesDocTransformerFactory.this.handle);
        IndexSchema schema = this.req.getSchema();
        this.engine.transformDocument(doc, docid, schema);
      } finally {
        this.req.getContext().remove("rulesPhase");
        this.req.getContext().remove("rulesHandle");
      }
    }
  }
}


