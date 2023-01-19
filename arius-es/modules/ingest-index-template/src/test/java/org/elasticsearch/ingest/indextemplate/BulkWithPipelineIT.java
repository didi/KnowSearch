package org.elasticsearch.ingest.indextemplate;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.test.ESIntegTestCase;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Ignore;

import static org.elasticsearch.test.ESIntegTestCase.Scope.SUITE;
import static org.hamcrest.Matchers.equalTo;

/**
 * author weizijun
 * date：2020-08-26
 */
@ESIntegTestCase.ClusterScope(scope = SUITE, numClientNodes = 0, transportClientRatio = 0, maxNumDataNodes = 1, supportsDedicatedMasters = false)
public class BulkWithPipelineIT extends ESIntegTestCase {
    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        return Arrays.asList(IngestIndexTemplatePlugin.class);
    }

    @Ignore
    public void testOneBulkItem() {
        Map<String, Integer> twoShardsSettings = Collections.singletonMap(IndexMetaData.SETTING_NUMBER_OF_SHARDS, 2);
        client().admin().indices().prepareCreate("index1").setSettings(twoShardsSettings).get();
        client().admin().cluster().preparePutPipeline("pipeline", new BytesArray("{\"description\":\"test\",\"processors\":[{\"index_template\":{\"index_version\":1,\"field\":\"timestamp\",\"index_name_format\":\"_yyyy-MM-dd\",\"expire_day\":2,\"field_format\":\"UNIX_MS\"}}]}"), XContentType.JSON).get();

        long now = System.currentTimeMillis();
        DateTimeFormatter formatter = DateTimeFormat.forPattern("_yyyy-MM-dd");
        DateTime dateTime = new DateTime(now);
        String indexPrefix = formatter.print(dateTime) + "_v1";

        {
            IndexRequest indexRequest = new IndexRequest("index1", "type", "id").setPipeline("pipeline");
            indexRequest.source(Collections.singletonMap("timestamp", now));
            BulkResponse bulkResponse = client().prepareBulk().add(indexRequest).get();
            assertThat(bulkResponse.getItems()[0].getResponse().getIndex(), equalTo("index1" + indexPrefix));
            assertThat(bulkResponse.getItems()[0].getResponse().getVersion(), equalTo(1L));
            assertThat(bulkResponse.getItems()[0].getResponse().status(), equalTo(RestStatus.CREATED));
            assertThat(client().prepareGet("index1" + indexPrefix, "type", "id").get().getSource().get("timestamp"), equalTo(now));
        }

        {
            UpdateRequest updateRequest = new UpdateRequest("index1", "type", "id");
            updateRequest.doc(Collections.singletonMap("timestamp", now + 1));
            updateRequest.doc().index(updateRequest.index());
            updateRequest.doc().type(updateRequest.type());
            updateRequest.doc().id(updateRequest.id());
            updateRequest.doc().setPipeline("pipeline");
            BulkResponse bulkResponse = client().prepareBulk().add(updateRequest).pipeline("pipeline").get();
            assertThat(bulkResponse.getItems()[0].getResponse().getIndex(), equalTo("index1" + indexPrefix));
            assertThat(bulkResponse.getItems()[0].getResponse().getVersion(), equalTo(2L));
            assertThat(bulkResponse.getItems()[0].getResponse().status(), equalTo(RestStatus.OK));
            assertThat(client().prepareGet("index1" + indexPrefix, "type", "id").get().getSource().get("timestamp"), equalTo(now+1));
        }

        // upsert
        {
            UpdateRequest updateRequest = new UpdateRequest("index1", "type", "id").setPipeline("pipeline");
            updateRequest.doc(Collections.singletonMap("timestamp", now + 2));
            updateRequest.upsert(Collections.singletonMap("timestamp", now + 2));
            updateRequest.upsertRequest().setPipeline("pipeline");
            updateRequest.upsertRequest().index(updateRequest.index());
            updateRequest.upsertRequest().type(updateRequest.type());
            updateRequest.upsertRequest().id(updateRequest.id());
            BulkResponse bulkResponse = client().prepareBulk().add(updateRequest).get();
            assertThat(bulkResponse.getItems()[0].getResponse().getIndex(), equalTo("index1" + indexPrefix));
            assertThat(bulkResponse.getItems()[0].getResponse().getVersion(), equalTo(3L));
            assertThat(bulkResponse.getItems()[0].getResponse().status(), equalTo(RestStatus.OK));
            assertThat(client().prepareGet("index1" + indexPrefix, "type", "id").get().getSource().get("timestamp"), equalTo(now+2));
        }

        {
            DeleteRequest deleteRequest = new DeleteRequest("index1", "type", "id").setPipeline("pipeline");
            deleteRequest.source(Collections.singletonMap("timestamp", now));
            BulkResponse bulkResponse = client().prepareBulk().add(deleteRequest).get();
            assertThat(bulkResponse.getItems()[0].getResponse().getIndex(), equalTo("index1" + indexPrefix));
            assertThat(bulkResponse.getItems()[0].getResponse().getVersion(), equalTo(4L));
            assertThat(bulkResponse.getItems()[0].getResponse().status(), equalTo(RestStatus.OK));
            assertThat(client().prepareGet("index1" + indexPrefix, "type", "id").get().isExists(), equalTo(false));
        }

    }
}
