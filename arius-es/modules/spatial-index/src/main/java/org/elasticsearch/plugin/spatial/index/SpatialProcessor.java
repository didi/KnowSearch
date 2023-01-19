package org.elasticsearch.plugin.spatial.index;

import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;
import org.elasticsearch.plugin.spatial.router.Router;
import org.elasticsearch.plugin.spatial.router.RouterResult;

import java.util.Map;

public class SpatialProcessor extends AbstractProcessor {
    public static final String TYPE = "spatial";

    private Router router;
    protected SpatialProcessor(String tag, Router router) {
        super(tag);
        this.router = router;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public IngestDocument execute(IngestDocument ingestDocument) throws Exception {
        RouterResult rr = router.doIndexRequest(ingestDocument);

        if(rr.getShardIds().size()>1) {
           throw new RuntimeException("have multi shardId");
        }

        if(rr.getShardIds().size()==0) {
           throw new RuntimeException("not have shardId");
        }

        ingestDocument.setFieldValue(IngestDocument.MetaData.ROUTING.getFieldName(), rr.getRoutingStr());
        return ingestDocument;
    }

    public static final class Factory implements Processor.Factory {
        private Router router;

        public Factory(Router router) {
            this.router = router;
        }

        public SpatialProcessor create(Map<String, Processor.Factory> registry, String tag, Map<String, Object> config) throws Exception {
            return new SpatialProcessor(tag, router);
        }
    }



}

