package com.lucid.rules;

import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.request.SolrQueryRequest;

public abstract class BaseRulesEngineComponent extends SearchComponent {
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

	protected boolean isComponentOn(String compName, SolrParams params, String phase) {
		boolean handleParam = params.getBool(compName + "." + this.handle, true);
		return (params.getBool(compName, false)) && (params.getBool(compName + "." + phase, true))
				&& (handleParam == true);
	}

	protected SolrParams convertParams(ResponseBuilder rb, SolrParams params) {
		if (!(params instanceof ModifiableSolrParams)) {
			params = new ModifiableSolrParams(params);
			rb.req.setParams(params);
		}
		return params;
	}
}
