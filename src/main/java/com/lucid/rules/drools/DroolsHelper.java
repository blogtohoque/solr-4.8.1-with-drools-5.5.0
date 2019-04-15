package com.lucid.rules.drools;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.IndexSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroolsHelper {
	private static transient Logger log = LoggerFactory.getLogger(DroolsHelper.class);

	public static final String RULES_PHASE = "rulesPhase";

	public static final String RULES_HANDLE = "rulesHandle";

	public static boolean hasPhaseMatch(ResponseBuilder builder, String expectedPhase) {
		String value = (String) builder.req.getContext().get("rulesPhase");
		return (value != null) && (value.equals(expectedPhase) == true);
	}

	public static boolean hasPhaseMatch(ResponseBuilder builder, String expectedPhase, String handleName) {
		Map<Object, Object> context = builder.req.getContext();
		String phase = (String) context.get("rulesPhase");
		String handler = (String) context.get("rulesHandle");
		return (phase != null) && (phase.equals(expectedPhase) == true) && (handler != null)
				&& (handler.equals(handleName));
	}

	public static boolean hasHandlerNameMatch(ResponseBuilder builder, String handlerName) {
		Map<Object, Object> context = builder.req.getContext();
		String handler = (String) context.get("rulesHandle");
		return (handler != null) && (handler.equals(handlerName));
	}

	public static void boostQuery(Query q, float boost) {
		q.setBoost(boost);
	}

	public static void addToResponse(ResponseBuilder builder, String key, Object val) {
		addToResponse(builder.rsp.getValues(), key, val);
	}

	public static void addToResponse(NamedList namedList, String key, Object val) {
		namedList.add(key, val);
	}

	public static void mergeFacets(ResponseBuilder builder, String targetField, int position, String... facetQueries) {
		NamedList facetCounts = (NamedList) builder.rsp.getValues().get("facet_counts");
		if (facetCounts != null) {
			NamedList facetFields = (NamedList) facetCounts.get("facet_fields");
			NamedList facetQueryNL = (NamedList) facetCounts.get("facet_queries");
			SolrParams facetQueryResults = SolrParams.toSolrParams(facetQueryNL);
			if ((facetFields != null) && (facetQueryResults != null)) {
				NamedList target = (NamedList) facetFields.get(targetField);
				if (target != null) {
					log.info("Merging into " + targetField + " at position: " + position);
					NamedList tmpTarget = new NamedList();
					int j = 0;
					for (Object o : target) {
						Map.Entry entry = (Map.Entry) o;
						if (j == position) {
							for (int i = 0; i < facetQueries.length; i++) {
								String facetQuery = facetQueries[i];
								Integer count = facetQueryResults.getInt(facetQuery);
								count = null == count ? new Integer(0) : count;

								tmpTarget.add(facetQuery, count);
							}
						}

						tmpTarget.add((String) entry.getKey(), entry.getValue());

						j++;
					}
					int index = facetFields.indexOf(targetField, 0);
					facetCounts.setVal(index, tmpTarget);
				}
			}
		}
	}

	public static void addFacet(ResponseBuilder builder, String facetName, String facetValue, int facetCount,
			int position) {
		NamedList facetCounts = (NamedList) builder.rsp.getValues().get("facet_counts");
		if (facetCounts != null) {
			NamedList facetFields = (NamedList) facetCounts.get("facet_fields");
			if (facetFields != null) {
				NamedList target = (NamedList) facetFields.get(facetName);
				if (target != null) {
					log.info("Adding into " + facetName);
					target.add(facetValue, Integer.valueOf(facetCount));
				}
			}
		}
	}

	public static void modRequest(ResponseBuilder builder, String key, String... values) {
		((ModifiableSolrParams) builder.req.getParams()).set(key, values);
	}

	public static void modRequest(ResponseBuilder builder, String key, int value) {
		((ModifiableSolrParams) builder.req.getParams()).set(key, value);
	}

	public static void modRequest(ResponseBuilder builder, String key, boolean value) {
		((ModifiableSolrParams) builder.req.getParams()).set(key, value);
	}

	public static boolean contains(String query, String value) {
		return query.toString().contains(value);
	}

	public static Collection<String> analyze(IndexSchema schema, String field, String text) throws IOException {
		Analyzer analyzer = schema.getAnalyzer();
		StringReader reader = new StringReader(text);
		TokenStream ts = analyzer.tokenStream(field, reader);
		CharTermAttribute termAtt = (CharTermAttribute) ts.addAttribute(CharTermAttribute.class);
		ts.reset();
		List<String> result = new ArrayList();

		while (ts.incrementToken()) {
			result.add(termAtt.toString());
		}

		ts.end();
		ts.close();

		return result;
	}

	public static String getSolrDocFirstValueAsString(SolrDocument doc, String fieldName) {
		if ((null == doc) || (null == fieldName)) {
			return null;
		}
		Object answerObj = doc.getFirstValue(fieldName);
		return objectToString(answerObj);
	}

	public static List<String> getSolrDocValuesAsStringList(SolrDocument doc, String fieldName) {
		List<String> answer = new ArrayList();
		if ((null == doc) || (null == fieldName)) {
			return answer;
		}
		Object listObj = doc.getFieldValue(fieldName);
		if ((listObj instanceof List)) {
			List values = (List) listObj;
			for (Object o : values) {
				String value = objectToString(o);
				if (null != value) {
					answer.add(value);
				}
			}
		} else {
			String singleton = objectToString(listObj);
			if (null != singleton) {
				answer.add(singleton);
			}
		}
		return answer;
	}

	static String objectToString(Object answerObj) {
		String answer = null;
		if (null != answerObj) {

			if ((answerObj instanceof String)) {
				answer = (String) answerObj;

			} else if ((answerObj instanceof Field)) {
				Field strField = (Field) answerObj;
				answer = strField.stringValue();
			}
		}
		return answer;
	}

	public static Float getSolrDocFirstValueAsFloat(SolrDocument doc, String fieldName) {
		if ((null == doc) || (null == fieldName)) {
			return null;
		}
		Object answerObj = doc.getFirstValue(fieldName);
		return objectToFloat(answerObj);
	}

	public static List<Float> getSolrDocValuesAsFloatList(SolrDocument doc, String fieldName) {
		List<Float> answer = new ArrayList();
		if ((null == doc) || (null == fieldName)) {
			return answer;
		}
		Object listObj = doc.getFieldValue(fieldName);
		if ((listObj instanceof List)) {
			List values = (List) listObj;
			for (Object o : values) {
				Float value = objectToFloat(o);
				if (null != value) {
					answer.add(value);
				}
			}
		} else {
			Float singleton = objectToFloat(listObj);
			if (null != singleton) {
				answer.add(singleton);
			}
		}
		return answer;
	}

	static Float objectToFloat(Object answerObj) {
		Float answer = null;
		if (null != answerObj) {
			if ((answerObj instanceof Float)) {
				answer = (Float) answerObj;

			} else if ((answerObj instanceof Field)) {
				Field answerField = (Field) answerObj;
				answer = Float.valueOf(answerField.numericValue().floatValue());
			}
		}
		return answer;
	}

	public static Double getSolrDocFirstValueAsDouble(SolrDocument doc, String fieldName) {
		if ((null == doc) || (null == fieldName)) {
			return null;
		}
		Object answerObj = doc.getFirstValue(fieldName);
		return objectToDouble(answerObj);
	}

	public static List<Double> getSolrDocValuesAsDoubleList(SolrDocument doc, String fieldName) {
		List<Double> answer = new ArrayList();
		if ((null == doc) || (null == fieldName)) {
			return answer;
		}
		Object listObj = doc.getFieldValue(fieldName);
		if ((listObj instanceof List)) {
			List values = (List) listObj;
			for (Object o : values) {
				Double value = objectToDouble(o);
				if (null != value) {
					answer.add(value);
				}
			}
		} else {
			Double singleton = objectToDouble(listObj);
			if (null != singleton) {
				answer.add(singleton);
			}
		}
		return answer;
	}

	static Double objectToDouble(Object answerObj) {
		Double answer = null;
		if (null != answerObj) {
			if ((answerObj instanceof Double)) {
				answer = (Double) answerObj;

			} else if ((answerObj instanceof Field)) {
				Field answerField = (Field) answerObj;
				answer = Double.valueOf(answerField.numericValue().doubleValue());
			}
		}
		return answer;
	}

	public static Integer getSolrDocFirstValueAsInt(SolrDocument doc, String fieldName) {
		if ((null == doc) || (null == fieldName)) {
			return null;
		}
		Object answerObj = doc.getFirstValue(fieldName);
		return objectToInt(answerObj);
	}

	public static List<Integer> getSolrDocValuesAsIntList(SolrDocument doc, String fieldName) {
		List<Integer> answer = new ArrayList();
		if ((null == doc) || (null == fieldName)) {
			return answer;
		}
		Object listObj = doc.getFieldValue(fieldName);
		if ((listObj instanceof List)) {
			List values = (List) listObj;
			for (Object o : values) {
				Integer value = objectToInt(o);
				if (null != value) {
					answer.add(value);
				}
			}
		} else {
			Integer singleton = objectToInt(listObj);
			if (null != singleton) {
				answer.add(singleton);
			}
		}
		return answer;
	}

	static Integer objectToInt(Object answerObj) {
		Integer answer = null;
		if (null != answerObj) {
			if ((answerObj instanceof Integer)) {
				answer = (Integer) answerObj;

			} else if ((answerObj instanceof Field)) {
				Field answerField = (Field) answerObj;
				answer = Integer.valueOf(answerField.numericValue().intValue());
			}
		}
		return answer;
	}

	public static Long getSolrDocFirstValueAsLong(SolrDocument doc, String fieldName) {
		if ((null == doc) || (null == fieldName)) {
			return null;
		}
		Object answerObj = doc.getFirstValue(fieldName);
		return objectToLong(answerObj);
	}

	public static List<Long> getSolrDocValuesAsLongList(SolrDocument doc, String fieldName) {
		List<Long> answer = new ArrayList();
		if ((null == doc) || (null == fieldName)) {
			return answer;
		}
		Object listObj = doc.getFieldValue(fieldName);
		if ((listObj instanceof List)) {
			List values = (List) listObj;
			for (Object o : values) {
				Long value = objectToLong(o);
				if (null != value) {
					answer.add(value);
				}
			}
		} else {
			Long singleton = objectToLong(listObj);
			if (null != singleton) {
				answer.add(singleton);
			}
		}
		return answer;
	}

	static Long objectToLong(Object answerObj) {
		Long answer = null;
		if (null != answerObj) {
			if ((answerObj instanceof Long)) {
				answer = (Long) answerObj;

			} else if ((answerObj instanceof Field)) {
				Field answerField = (Field) answerObj;
				answer = Long.valueOf(answerField.numericValue().longValue());
			}
		}
		return answer;
	}

	public static Short getSolrDocFirstValueAsShort(SolrDocument doc, String fieldName) {
		if ((null == doc) || (null == fieldName)) {
			return null;
		}
		Object answerObj = doc.getFirstValue(fieldName);
		return objectToShort(answerObj);
	}

	public static List<Short> getSolrDocValuesAsShortList(SolrDocument doc, String fieldName) {
		List<Short> answer = new ArrayList();
		if ((null == doc) || (null == fieldName)) {
			return answer;
		}
		Object listObj = doc.getFieldValue(fieldName);
		if ((listObj instanceof List)) {
			List values = (List) listObj;
			for (Object o : values) {
				Short value = objectToShort(o);
				if (null != value) {
					answer.add(value);
				}
			}
		} else {
			Short singleton = objectToShort(listObj);
			if (null != singleton) {
				answer.add(singleton);
			}
		}
		return answer;
	}

	static Short objectToShort(Object answerObj) {
		Short answer = null;
		if (null != answerObj) {
			if ((answerObj instanceof Short)) {
				answer = (Short) answerObj;

			} else if ((answerObj instanceof Field)) {
				Field answerField = (Field) answerObj;
				answer = Short.valueOf(answerField.numericValue().shortValue());
			}
		}
		return answer;
	}

	public static Byte getSolrDocFirstValueAsByte(SolrDocument doc, String fieldName) {
		if ((null == doc) || (null == fieldName)) {
			return null;
		}
		Object answerObj = doc.getFirstValue(fieldName);
		return objectToByte(answerObj);
	}

	public static List<Byte> getSolrDocValuesAsByteList(SolrDocument doc, String fieldName) {
		List<Byte> answer = new ArrayList();
		if ((null == doc) || (null == fieldName)) {
			return answer;
		}
		Object listObj = doc.getFieldValue(fieldName);
		if ((listObj instanceof List)) {
			List values = (List) listObj;
			for (Object o : values) {
				Byte value = objectToByte(o);
				if (null != value) {
					answer.add(value);
				}
			}
		} else {
			Byte singleton = objectToByte(listObj);
			if (null != singleton) {
				answer.add(singleton);
			}
		}
		return answer;
	}

	static Byte objectToByte(Object answerObj) {
		if (null == answerObj) {
			return null;
		}
		Byte answer = null;
		if (null != answerObj) {
			if ((answerObj instanceof Byte)) {
				answer = (Byte) answerObj;

			} else if ((answerObj instanceof Field)) {
				Field answerField = (Field) answerObj;
				answer = Byte.valueOf(answerField.numericValue().byteValue());
			}
		}
		return answer;
	}
}

