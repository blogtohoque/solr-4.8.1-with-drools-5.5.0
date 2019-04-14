package com.lucid.rules;

import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;

public class RulesUpdateProcessorFactory extends UpdateRequestProcessorFactory {
	protected String handlerName;
	protected String engineName;
	protected String handle;

	public void init(NamedList args) {
		super.init(args);
		SolrParams params = SolrParams.toSolrParams(args);
		this.handlerName = params.get("requestHandler");
		if ((this.handlerName == null) || (this.handlerName.equals(""))) {
			throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
					"Unable to determine RulesEngineManagerHandler for requestHandler=" + this.handlerName);
		}
		this.engineName = params.get("engine");
		if (this.engineName == null) {
			throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
					"Unable to determine engineName for=" + this.engineName);
		}
		this.handle = params.get("handle");
	}

	public UpdateRequestProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp,
			UpdateRequestProcessor next) {
		boolean handleParam = req.getParams().getBool("rules." + this.handle, true);
		if (handleParam == true) {
			RulesEngine engine = RulesHelper.getEngine(req.getCore(), this.handlerName, this.engineName);
			if ((engine != null) && (engine.hasRules())) {
				return new RulesUpdateProcessor(next, engine);
			}

			return null;
		}
		return null;
	}
}

