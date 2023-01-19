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

package org.elasticsearch.ingest.common;

import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.ConfigurationUtils;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;
import org.elasticsearch.ingest.WrappingProcessor;
import org.elasticsearch.script.ScriptService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.elasticsearch.ingest.ConfigurationUtils.newConfigurationException;
import static org.elasticsearch.ingest.ConfigurationUtils.readBooleanProperty;
import static org.elasticsearch.ingest.ConfigurationUtils.readMap;
import static org.elasticsearch.ingest.ConfigurationUtils.readStringProperty;

/**
 * A processor that for each value in a list executes a one or more processors.
 *
 * This can be useful in cases to do string operations on json array of strings,
 * or remove a field from objects inside a json array.
 *
 * Note that this processor is experimental.
 */
public final class ForEachProcessor extends AbstractProcessor implements WrappingProcessor {

    public static final String TYPE = "foreach";
    static final int MAX_RECURSE_PER_THREAD = 10;

    private final String field;
    private final Processor processor;
    private final boolean ignoreMissing;
    private final Consumer<Runnable> genericExecutor;

    ForEachProcessor(String tag, String field, Processor processor, boolean ignoreMissing, Consumer<Runnable> genericExecutor) {
        super(tag);
        this.field = field;
        this.processor = processor;
        this.ignoreMissing = ignoreMissing;
        this.genericExecutor = genericExecutor;
    }

    boolean isIgnoreMissing() {
        return ignoreMissing;
    }

    @Override
    public void execute(IngestDocument ingestDocument, BiConsumer<IngestDocument, Exception> handler) {
        List<?> values = ingestDocument.getFieldValue(field, List.class, ignoreMissing);
        if (values == null) {
            if (ignoreMissing) {
                handler.accept(ingestDocument, null);
            } else {
                handler.accept(null, new IllegalArgumentException("field [" + field + "] is null, cannot loop over its elements."));
            }
        } else {
            List<Object> newValues = new CopyOnWriteArrayList<>();
            innerExecute(0, values, newValues, ingestDocument, handler);
        }
    }

    void innerExecute(int index, List<?> values, List<Object> newValues, IngestDocument document,
                      BiConsumer<IngestDocument, Exception> handler) {
        if (index == values.size()) {
            document.setFieldValue(field, new ArrayList<>(newValues));
            handler.accept(document, null);
            return;
        }

        Object value = values.get(index);
        Object previousValue = document.getIngestMetadata().put("_value", value);
        final Thread thread = Thread.currentThread();
        processor.execute(document, (result, e) -> {
            if (e != null)  {
                newValues.add(document.getIngestMetadata().put("_value", previousValue));
                handler.accept(null, e);
            } else if (result == null) {
                handler.accept(null, null);
            } else {
                newValues.add(document.getIngestMetadata().put("_value", previousValue));
                if (thread == Thread.currentThread() && (index + 1) % MAX_RECURSE_PER_THREAD == 0) {
                    // we are on the same thread and we need to fork to another thread to avoid recursive stack overflow on a single thread
                    // only fork after 10 recursive calls, then fork every 10 to keep the number of threads down
                    genericExecutor.accept(() -> innerExecute(index + 1, values, newValues, document, handler));
                } else {
                    // we are on a different thread (we went asynchronous), it's safe to recurse
                    // or we have recursed less then 10 times with the same thread, it's safe to recurse
                    innerExecute(index + 1, values, newValues, document, handler);
                }
            }
        });
    }

    @Override
    public IngestDocument execute(IngestDocument ingestDocument) throws Exception {
        throw new UnsupportedOperationException("this method should not get executed");
    }

    @Override
    public String getType() {
        return TYPE;
    }

    String getField() {
        return field;
    }

    public Processor getInnerProcessor() {
        return processor;
    }

    public static final class Factory implements Processor.Factory {

        private final ScriptService scriptService;
        private final Consumer<Runnable> genericExecutor;

        Factory(ScriptService scriptService, Consumer<Runnable> genericExecutor) {
            this.scriptService = scriptService;
            this.genericExecutor = genericExecutor;
        }

        @Override
        public ForEachProcessor create(Map<String, Processor.Factory> factories, String tag,
                                       Map<String, Object> config) throws Exception {
            String field = readStringProperty(TYPE, tag, config, "field");
            boolean ignoreMissing = readBooleanProperty(TYPE, tag, config, "ignore_missing", false);
            Map<String, Map<String, Object>> processorConfig = readMap(TYPE, tag, config, "processor");
            Set<Map.Entry<String, Map<String, Object>>> entries = processorConfig.entrySet();
            if (entries.size() != 1) {
                throw newConfigurationException(TYPE, tag, "processor", "Must specify exactly one processor type");
            }
            Map.Entry<String, Map<String, Object>> entry = entries.iterator().next();
            Processor processor =
                ConfigurationUtils.readProcessor(factories, scriptService, entry.getKey(), entry.getValue());
            return new ForEachProcessor(tag, field, processor, ignoreMissing, genericExecutor);
        }
    }
}
