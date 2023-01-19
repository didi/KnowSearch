package org.elasticsearch.ingest.indextemplate;

import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.node.StatisticNode;
import org.elasticsearch.common.util.concurrent.EsRejectedExecutionException;
import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.ConfigurationUtils;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;

import java.util.Locale;
import java.util.Map;

/**
 * author weizijun
 * date：2019-07-25
 */
public class ThrottleProcessor extends AbstractProcessor {
    public static final String TYPE = "throttle";
    private static final String RATE_LIMIT_FIELD = "rate_limit";

    private final int rateLimit;
    private final Node node;

    protected ThrottleProcessor(String tag, int rateLimit) {
        super(tag);
        this.rateLimit = rateLimit;
        this.node = new StatisticNode();

    }

    @Override
    public IngestDocument execute(IngestDocument ingestDocument) throws Exception {
        if (rateLimit < 0) {
            return ingestDocument;
        }

        if (node.passQps() < rateLimit) {
            node.addPassRequest(1);
        } else {
            throw new EsRejectedExecutionException(String.format(Locale.US, "rejected execution of flowLimit,[%s][%s]",
                ingestDocument.getFieldValue("_index", String.class),
                ingestDocument.getFieldValue("_id", String.class)));
        }
        return ingestDocument;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public static final class Factory implements Processor.Factory {

        public ThrottleProcessor create(Map<String, Processor.Factory> registry, String tag,
                                        Map<String, Object> config) throws Exception {
            int rateLimit = ConfigurationUtils.readIntProperty(TYPE, tag, config, RATE_LIMIT_FIELD, -1);
            return new ThrottleProcessor(tag, rateLimit);
        }
    }
}

