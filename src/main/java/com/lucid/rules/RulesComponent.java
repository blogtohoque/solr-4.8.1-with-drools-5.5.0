package com.lucid.rules;

import java.io.IOException;
import java.util.Map;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RulesComponent extends BaseRulesEngineComponent implements SolrCoreAware {
	private static transient Logger log = LoggerFactory.getLogger(RulesComponent.class);

	public static final String COMPONENT_NAME = "rules";
	private static final int STAGE_RULES = 2147483646;

	public void inform(SolrCore core) {
		log.info("Loading RulesComponent");
	}

	public void prepare(ResponseBuilder rb) throws IOException {
		SolrQueryRequest req = rb.req;
		SolrParams params = req.getParams();

		if (!req.getParams().getBool("isShard", false)) {
			params = convertParams(rb, params);

			if (isComponentOn("rules", params, "prepare")) {
				RulesEngine engine = RulesHelper.getEngine(rb.req.getCore(), this.handlerName, this.engineName);
				if (engine != null) {
					rb.req.getContext().put("rulesPhase", "prepare");
					rb.req.getContext().put("rulesHandle", this.handle);
					try {
						engine.prepareSearch(rb);
					} finally {
						rb.req.getContext().remove("rulesPhase");
						rb.req.getContext().remove("rulesHandle");
					}
				} else {
					throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Unable to find engine with name "
							+ this.engineName + " on the Request Handler named: " + this.handlerName);
				}
			}
		}
	}

	public void process(ResponseBuilder rb) throws IOException {
		SolrQueryRequest req = rb.req;
		SolrParams params = req.getParams();

		if ((!req.getParams().getBool("isShard", false)) && (isComponentOn("rules", params, "process"))) {
			RulesEngine engine = RulesHelper.getEngine(rb.req.getCore(), this.handlerName, this.engineName);
			if (engine != null) {
				rb.req.getContext().put("rulesPhase", "process");
				rb.req.getContext().put("rulesHandle", this.handle);
				try {
					engine.postSearch(rb);
				} finally {
					rb.req.getContext().remove("rulesPhase");
					rb.req.getContext().remove("rulesHandle");
				}
			} else {
				throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Unable to find engine with name "
						+ this.engineName + " on the Request Handler named: " + this.handlerName);
			}
		}
	}

	public int distributedProcess(ResponseBuilder rb) throws IOException {
		int result = 2147483646;
		if (rb.stage == 2147483646) {
			result = ResponseBuilder.STAGE_DONE;
		}
		return result;
	}

	public void finishStage(ResponseBuilder rb) {
		if (rb.stage == 2147483646) {
			RulesEngine engine = RulesHelper.getEngine(rb.req.getCore(), this.handlerName, this.engineName);
			if (engine != null) {
				rb.req.getContext().put("rulesPhase", "finishStage");
				rb.req.getContext().put("rulesHandle", this.handle);
				try {
					engine.postSearch(rb);
				} finally {
					rb.req.getContext().remove("rulesPhase");
					rb.req.getContext().remove("rulesHandle");
				}
			} else {
				throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Unable to find engine with name "
						+ this.engineName + " on the Request Handler named: " + this.handlerName);
			}
		}
	}

	public String getDescription() {
		return "Provides integration with Business Rules engines like Drools and others via the RulesEngine API";
	}

	public String getSource() {
		return "$URL:  $";
	}

	public String getVersion() {
		return "$Revision:  $";
	}
}

