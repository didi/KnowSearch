/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.search.aggregations.metrics;

import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ConstructingObjectParser;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptedMetricAggContexts;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregatorFactories.Builder;
import org.elasticsearch.search.aggregations.AggregatorFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static org.elasticsearch.common.xcontent.ConstructingObjectParser.constructorArg;

public class ScriptedMetricAggregationBuilder extends AbstractAggregationBuilder<ScriptedMetricAggregationBuilder> {
    public static final String NAME = "scripted_metric";

    private static final ParseField INIT_SCRIPT_FIELD = new ParseField("init_script");
    private static final ParseField MAP_SCRIPT_FIELD = new ParseField("map_script");
    private static final ParseField COMBINE_SCRIPT_FIELD = new ParseField("combine_script");
    private static final ParseField REDUCE_SCRIPT_FIELD = new ParseField("reduce_script");
    private static final ParseField PARAMS_FIELD = new ParseField("params");

    public static final ConstructingObjectParser<ScriptedMetricAggregationBuilder, String> PARSER =
            new ConstructingObjectParser<>(NAME, false, (args, name) -> {
                ScriptedMetricAggregationBuilder builder = new ScriptedMetricAggregationBuilder(name);
                builder.mapScript((Script) args[0]);
                return builder;
            });
    static {
        Script.declareScript(PARSER, ScriptedMetricAggregationBuilder::initScript, INIT_SCRIPT_FIELD);
        Script.declareScript(PARSER, constructorArg(), MAP_SCRIPT_FIELD);
        Script.declareScript(PARSER, ScriptedMetricAggregationBuilder::combineScript, COMBINE_SCRIPT_FIELD);
        Script.declareScript(PARSER, ScriptedMetricAggregationBuilder::reduceScript, REDUCE_SCRIPT_FIELD);
        PARSER.declareObject(ScriptedMetricAggregationBuilder::params, (p, name) -> p.map(), PARAMS_FIELD);
    }

    private Script initScript;
    private Script mapScript;
    private Script combineScript;
    private Script reduceScript;
    private Map<String, Object> params;

    public ScriptedMetricAggregationBuilder(String name) {
        super(name);
    }

    protected ScriptedMetricAggregationBuilder(ScriptedMetricAggregationBuilder clone,
                                               Builder factoriesBuilder, Map<String, Object> metaData) {
        super(clone, factoriesBuilder, metaData);
        this.initScript = clone.initScript;
        this.mapScript = clone.mapScript;
        this.combineScript = clone.combineScript;
        this.reduceScript = clone.reduceScript;
        this.params = clone.params;
    }

    @Override
    protected AggregationBuilder shallowCopy(Builder factoriesBuilder, Map<String, Object> metaData) {
        return new ScriptedMetricAggregationBuilder(this, factoriesBuilder, metaData);
    }

    /**
     * Read from a stream.
     */
    public ScriptedMetricAggregationBuilder(StreamInput in) throws IOException {
        super(in);
        initScript = in.readOptionalWriteable(Script::new);
        mapScript = in.readOptionalWriteable(Script::new);
        combineScript = in.readOptionalWriteable(Script::new);
        reduceScript = in.readOptionalWriteable(Script::new);
        if (in.readBoolean()) {
            params = in.readMap();
        }
    }

    @Override
    protected void doWriteTo(StreamOutput out) throws IOException {
        out.writeOptionalWriteable(initScript);
        out.writeOptionalWriteable(mapScript);
        out.writeOptionalWriteable(combineScript);
        out.writeOptionalWriteable(reduceScript);
        boolean hasParams = params != null;
        out.writeBoolean(hasParams);
        if (hasParams) {
            out.writeMap(params);
        }
    }

    /**
     * Set the {@code init} script.
     */
    public ScriptedMetricAggregationBuilder initScript(Script initScript) {
        if (initScript == null) {
            throw new IllegalArgumentException("[initScript] must not be null: [" + name + "]");
        }
        this.initScript = initScript;
        return this;
    }

    /**
     * Get the {@code init} script.
     */
    public Script initScript() {
        return initScript;
    }

    /**
     * Set the {@code map} script.
     */
    public ScriptedMetricAggregationBuilder mapScript(Script mapScript) {
        if (mapScript == null) {
            throw new IllegalArgumentException("[mapScript] must not be null: [" + name + "]");
        }
        this.mapScript = mapScript;
        return this;
    }

    /**
     * Get the {@code map} script.
     */
    public Script mapScript() {
        return mapScript;
    }

    /**
     * Set the {@code combine} script.
     */
    public ScriptedMetricAggregationBuilder combineScript(Script combineScript) {
        if (combineScript == null) {
            throw new IllegalArgumentException("[combineScript] must not be null: [" + name + "]");
        }
        this.combineScript = combineScript;
        return this;
    }

    /**
     * Get the {@code combine} script.
     */
    public Script combineScript() {
        return combineScript;
    }

    /**
     * Set the {@code reduce} script.
     */
    public ScriptedMetricAggregationBuilder reduceScript(Script reduceScript) {
        if (reduceScript == null) {
            throw new IllegalArgumentException("[reduceScript] must not be null: [" + name + "]");
        }
        this.reduceScript = reduceScript;
        return this;
    }

    /**
     * Get the {@code reduce} script.
     */
    public Script reduceScript() {
        return reduceScript;
    }

    /**
     * Set parameters that will be available in the {@code init},
     * {@code map} and {@code combine} phases.
     */
    public ScriptedMetricAggregationBuilder params(Map<String, Object> params) {
        if (params == null) {
            throw new IllegalArgumentException("[params] must not be null: [" + name + "]");
        }
        this.params = params;
        return this;
    }

    /**
     * Get parameters that will be available in the {@code init},
     * {@code map} and {@code combine} phases.
     */
    public Map<String, Object> params() {
        return params;
    }

    @Override
    protected ScriptedMetricAggregatorFactory doBuild(QueryShardContext queryShardContext, AggregatorFactory parent,
                                                      Builder subfactoriesBuilder) throws IOException {

        if (combineScript == null) {
            throw new IllegalArgumentException("[combineScript] must not be null: [" + name + "]");
        }

        if(reduceScript == null) {
            throw new IllegalArgumentException("[reduceScript] must not be null: [" + name + "]");
        }

        // Extract params from scripts and pass them along to ScriptedMetricAggregatorFactory, since it won't have
        // access to them for the scripts it's given precompiled.

        ScriptedMetricAggContexts.InitScript.Factory compiledInitScript;
        Map<String, Object> initScriptParams;
        if (initScript != null) {
            compiledInitScript = queryShardContext.compile(initScript, ScriptedMetricAggContexts.InitScript.CONTEXT);
            initScriptParams = initScript.getParams();
        } else {
            compiledInitScript = (p, a) -> null;
            initScriptParams = Collections.emptyMap();
        }

        ScriptedMetricAggContexts.MapScript.Factory compiledMapScript = queryShardContext.compile(mapScript,
            ScriptedMetricAggContexts.MapScript.CONTEXT);
        Map<String, Object> mapScriptParams = mapScript.getParams();


        ScriptedMetricAggContexts.CombineScript.Factory compiledCombineScript;
        Map<String, Object> combineScriptParams;

        compiledCombineScript = queryShardContext.compile(combineScript,
            ScriptedMetricAggContexts.CombineScript.CONTEXT);
        combineScriptParams = combineScript.getParams();

        return new ScriptedMetricAggregatorFactory(name, compiledMapScript, mapScriptParams, compiledInitScript,
                initScriptParams, compiledCombineScript, combineScriptParams, reduceScript,
                params, queryShardContext.lookup(), queryShardContext, parent, subfactoriesBuilder, metaData);
    }


    @Override
    protected XContentBuilder internalXContent(XContentBuilder builder, Params builderParams) throws IOException {
        builder.startObject();
        if (initScript != null) {
            builder.field(INIT_SCRIPT_FIELD.getPreferredName(), initScript);
        }

        if (mapScript != null) {
            builder.field(MAP_SCRIPT_FIELD.getPreferredName(), mapScript);
        }

        if (combineScript != null) {
            builder.field(COMBINE_SCRIPT_FIELD.getPreferredName(), combineScript);
        }

        if (reduceScript != null) {
            builder.field(REDUCE_SCRIPT_FIELD.getPreferredName(), reduceScript);
        }
        if (params != null) {
            builder.field(PARAMS_FIELD.getPreferredName());
            builder.map(params);
        }
        builder.endObject();
        return builder;
    }

    @Override
    public String getType() {
        return NAME;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), initScript, mapScript, combineScript, reduceScript, params);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (super.equals(obj) == false) return false;
        ScriptedMetricAggregationBuilder other = (ScriptedMetricAggregationBuilder) obj;
        return Objects.equals(initScript, other.initScript)
            && Objects.equals(mapScript, other.mapScript)
            && Objects.equals(combineScript, other.combineScript)
            && Objects.equals(reduceScript, other.reduceScript)
            && Objects.equals(params, other.params);
    }

}
