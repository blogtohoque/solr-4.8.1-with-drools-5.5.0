package com.lucid.rules;

import com.lucid.lwx.license.ExpiredLicenseException;
//import com.lucid.lwx.license.License;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrInfoMBean;
import org.apache.solr.core.SolrInfoMBean.Category;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RulesEngineManagerHandler extends RequestHandlerBase implements SolrCoreAware {
	private static transient Logger log = LoggerFactory.getLogger(RulesEngineManagerHandler.class);
	public static final String ENGINES = "engines";
	public static final String ENGINE = "engine";
	public static final String ENGINE_CLASS_NAME = "class";
	public static final String NAME = "name";
	protected SolrParams params;
	protected Map<String, RulesEngine> engines = new HashMap();

	public void init(NamedList args) {
		super.init(args);
		this.params = SolrParams.toSolrParams(args);
	}
	
	/**
	 * 
	 * modified below code base by Hoque
	 */
	public void inform(SolrCore core) {
		NamedList enginesArgs = (NamedList) this.initArgs.get("engines");
		if (enginesArgs != null) {
			List all = enginesArgs.getAll("engine");
			for (Object obj : all) {
				NamedList engineArgs = (NamedList) obj;
				String name = (String) engineArgs.get("name");
				String className = engineArgs.get("class").toString();
				Class<? extends RulesEngine> engineClass = core.getResourceLoader().findClass(className,
						RulesEngine.class);
				try {
					log.info("Loading " + name + " rules engine as class: " + className);
					RulesEngine engine = (RulesEngine) engineClass.asSubclass(RulesEngine.class).newInstance();
					engine.init(name, engineArgs, core);

				//	License license = new License(5184000000L);

					try {
						/*
						if (!license.validate()) {
							log.warn(
									"You are using an unlicensed version of this software - but will still function during eval period");
						}
						*/

						if (engine.hasRules()) {
							this.engines.put(name, engine);
						} else {
							log.info("No rules found for engine \"" + name + "\". ");
							this.engines.put(name, new NoopRulesEngine());
						}

					} catch (ExpiredLicenseException ele) {
						log.error("Business Rules Modules will not be active");
						this.engines.put(name, new NoopRulesEngine());
					}
				} catch (InstantiationException e) {
					log.error("Exception", e);
					throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
							"Unable to instantiate rules engine: " + className, e);
				} catch (IllegalAccessException e) {
					log.error("Exception", e);
					throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
							"Unable to instantiate rules engine: " + className, e);
				} catch (Exception e) {
					log.error("Exception", e);
					throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
							"Unable to initialize rules engine: " + className, e);
				}
			}
		}
	}

	
	
/**
 * 
 * License part removed from code base 
 * below is the backup code which is commented.
 */
	/*
	public void inform(SolrCore core) {
		NamedList enginesArgs = (NamedList) this.initArgs.get("engines");
		if (enginesArgs != null) {
			List all = enginesArgs.getAll("engine");
			for (Object obj : all) {
				NamedList engineArgs = (NamedList) obj;
				String name = (String) engineArgs.get("name");
				String className = engineArgs.get("class").toString();
				Class<? extends RulesEngine> engineClass = core.getResourceLoader().findClass(className,
						RulesEngine.class);
				try {
					log.info("Loading " + name + " rules engine as class: " + className);
					RulesEngine engine = (RulesEngine) engineClass.asSubclass(RulesEngine.class).newInstance();
					engine.init(name, engineArgs, core);

					License license = new License(5184000000L);

					try {
						if (!license.validate()) {
							log.warn(
									"You are using an unlicensed version of this software - but will still function during eval period");
						}

						if (engine.hasRules()) {
							this.engines.put(name, engine);
						} else {
							log.info("No rules found for engine \"" + name + "\". ");
							this.engines.put(name, new NoopRulesEngine());
						}

					} catch (ExpiredLicenseException ele) {
						log.error("Invalid license - Business Rules Modules will not be active");
						this.engines.put(name, new NoopRulesEngine());
					}
				} catch (InstantiationException e) {
					log.error("Exception", e);
					throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
							"Unable to instantiate rules engine: " + className, e);
				} catch (IllegalAccessException e) {
					log.error("Exception", e);
					throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
							"Unable to instantiate rules engine: " + className, e);
				} catch (Exception e) {
					log.error("Exception", e);
					throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
							"Unable to initialize rules engine: " + className, e);
				}
			}
		}
	}
	*/

	public RulesEngine getEngine(String name) {
		return (RulesEngine) this.engines.get(name);
	}

	public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
		SolrParams params = req.getParams();
		String command = params.get("rulesCmd");
	}

	public String getVersion() {
		return "0.1";
	}

	public String getDescription() {
		return "Initializes and controls the RulesEngine and provides various other functionality related to it";
	}

	public SolrInfoMBean.Category getCategory() {
		return SolrInfoMBean.Category.OTHER;
	}

	public String getSource() {
		return "RulesRequestHandler.java";
	}

	public NamedList getStatistics() {
		NamedList result = null;
		return result;
	}
}
