package org.elasticsearch.ingest.indextemplate;

import org.elasticsearch.ingest.Processor;
import org.elasticsearch.plugins.IngestPlugin;
import org.elasticsearch.plugins.Plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * author weizijun
 * date：2019-05-24
 */
public class IngestIndexTemplatePlugin extends Plugin implements IngestPlugin {
    @Override
    public Map<String, Processor.Factory> getProcessors(Processor.Parameters parameters) {
        Map<String, Processor.Factory> processors = new HashMap<>();
        processors.put(ThrottleProcessor.TYPE, new ThrottleProcessor.Factory());
        processors.put(IndexTemplateProcessor.TYPE, new IndexTemplateProcessor.Factory());
        return Collections.unmodifiableMap(processors);
    }
}
