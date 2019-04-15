package com.lucid.rules.drools.stateful;

import com.lucid.rules.drools.BaseDroolsRulesEngine;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.drools.KnowledgeBase;
import org.drools.command.Command;
import org.drools.command.CommandFactory;
import org.drools.runtime.CommandExecutor;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatefulPoolDroolsRulesEngine extends BaseDroolsRulesEngine {
	private static final Logger log = LoggerFactory.getLogger(StatefulPoolDroolsRulesEngine.class);

	private static final String POOL_SIZE = "poolSize";

	private static final int MAX_SESSIONS_DEFAULT = 10;

	protected BlockingQueue<StatefulKnowledgeSession> ksessions;

	public void init(String engineName, NamedList args, SolrCore core) throws Exception {
		super.init(engineName, args, core);
		Integer poolSize = (Integer) args.get("poolSize");
		if (poolSize == null) {
			poolSize = Integer.valueOf(10);
		}
		log.info("Creating a pool with '" + poolSize + "' sessions for rules engine '" + engineName + "'");
		this.ksessions = new ArrayBlockingQueue(poolSize.intValue());
		for (int i = 0; i < poolSize.intValue(); i++) {
			this.ksessions.add(this.kbase.newStatefulKnowledgeSession());
		}
	}

	protected CommandExecutor getSession() {
		try {
			StatefulKnowledgeSession ksession = (StatefulKnowledgeSession) this.ksessions.poll(60L, TimeUnit.SECONDS);
			if (ksession != null) {
				return ksession;
			}
			throw new RuntimeException("Unnable to get a session");
		} catch (InterruptedException e) {
			throw new RuntimeException("Unnable to get a session", e);
		}
	}

	protected void execute(CommandExecutor ksession, List<Command<?>> cmds, Collection<?> facts) {
		cmds.add(CommandFactory.newInsertElements(facts, "allfacts", false, null));

		cmds.add(CommandFactory.newFireAllRules());
		Command<?> batchCmd = CommandFactory.newBatchExecution(cmds);
		StatefulKnowledgeSession sksession = (StatefulKnowledgeSession) ksession;
		sksession.execute(batchCmd);
		Iterable<FactHandle> handles = sksession.getFactHandles();
		for (FactHandle handle : handles) {
			if (handle != null) {
				sksession.retract(handle);
			}
		}
		assert (sksession.getObjects().isEmpty());
		assert (sksession.getFactHandles().isEmpty());
		this.ksessions.add(sksession);
	}

	public void close() throws IOException {
		super.close();
		for (StatefulKnowledgeSession ksession : this.ksessions) {
			ksession.dispose();
		}
	}
}
