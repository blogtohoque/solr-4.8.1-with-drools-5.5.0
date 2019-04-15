package com.lucid.rules;

import java.io.IOException;
import org.apache.solr.core.CloseHook;
import org.apache.solr.core.SolrCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RulesEngineCloseHook extends CloseHook {
	private static transient Logger log = LoggerFactory.getLogger(RulesEngineCloseHook.class);
	protected RulesEngine engine;

	public RulesEngineCloseHook(RulesEngine engine) {
		this.engine = engine;
	}

	public void preClose(SolrCore core) {
	}

	public void postClose(SolrCore core) {
		try {
			this.engine.close();
		} catch (IOException e) {
			log.error("Exception", e);
		}
	}
}
