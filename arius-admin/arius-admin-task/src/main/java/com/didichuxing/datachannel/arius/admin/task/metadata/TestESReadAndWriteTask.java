package com.didichuxing.datachannel.arius.admin.task.metadata;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpClient;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.request.batch.BatchType;
import com.didiglobal.logi.elasticsearch.client.request.batch.ESBatchRequest;
import com.didiglobal.logi.elasticsearch.client.response.batch.ESBatchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusTaskThreadPool;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESGatewayClient;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;

import lombok.EqualsAndHashCode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author cjm
 *
 * admin中有一个定时任务，会定期的从该索引读数据和写数据
 *
 * 在前期开发过程中，gateway难免出现空闲时段，需要制造一定的轻量读和轻量写的请求，
 * 让gateway空闲时期的请求量不为0，有利于界面的展示。然后读和写操作对象都是该索引。
 *
 * 0秒开始，每10秒执行一次
 * 0/10 * * * * ? *
 */
//@Task(name = "TestESReadAndWriteTask", description = "ES集群读写性能测试任务", cron = "0/10 * * * * ? *", autoRegister = true)
//@Component
public class TestESReadAndWriteTask implements Job {

    private static final Logger    LOGGER = LoggerFactory.getLogger(TestESReadAndWriteTask.class);

    @Autowired
    private ESGatewayClient        esGatewayClient;

    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

    @Autowired
    private ESOpClient             esOpClient;

    private AriusTaskThreadPool    ariusTaskThreadPool;

    @PostConstruct
    private void init() {
        ariusTaskThreadPool = new AriusTaskThreadPool();
        ariusTaskThreadPool.init(20, "TestESReadAndWriteTask");
    }


    //@Scheduled(cron = "0/10 * * * * ?")
    private void doExecute(){
        try {

            execute(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final String TYPE_NAME    = "_doc";

    private static final String INDEX_NAME1   = "dcdr6.6.1-test04";

    private static final String INDEX_NAME2   = "dcdr6.6.1-test06";

    private static final String INDEX_NAME11   = "dcdr6.6.1-test01_2021-12-17";
    private static final String INDEX_NAME12   = "dcdr6.6.1-test01_2021-12-18";
    private static final String INDEX_NAME13   = "dcdr6.6.1-test01_2021-12-19";
    private static final String INDEX_NAME14   = "dcdr6.6.1-test01_2021-12-20";

    private static final String INDEX_NAME21   = "dcdr6.6.1-test02_2021-12-17";
    private static final String INDEX_NAME22   = "dcdr6.6.1-test02_2021-12-18";
    private static final String INDEX_NAME23   = "dcdr6.6.1-test02_2021-12-19";
    private static final String INDEX_NAME24   = "dcdr6.6.1-test02_2021-12-20";

    private static final String INDEX_NAME31   = "dcdr6.6.1-test03_2021-12-17";
    private static final String INDEX_NAME32   = "dcdr6.6.1-test03_2021-12-18";
    private static final String INDEX_NAME33   = "dcdr6.6.1-test03_2021-12-19";
    private static final String INDEX_NAME34   = "dcdr6.6.1-test03_2021-12-20";

    private static final String INDEX_NAME51   = "dcdr6.6.1-test05_2021-12-17";
    private static final String INDEX_NAME52   = "dcdr6.6.1-test05_2021-12-18";
    private static final String INDEX_NAME53   = "dcdr6.6.1-test05_2021-12-19";
    private static final String INDEX_NAME54   = "dcdr6.6.1-test05_2021-12-20";

    private static final String INDEX_NAME71   = "dcdr-test07_2021-12-17";
    private static final String INDEX_NAME72   = "dcdr-test07_2021-12-18";
    private static final String INDEX_NAME73   = "dcdr-test07_2021-12-19";
    private static final String INDEX_NAME74   = "dcdr-test07_2021-12-20";

    private static final String CLUSTER_NAME = "dcdr-6.6.1-master";

    @lombok.Data
    @EqualsAndHashCode(callSuper = true)
    static class Data extends BaseESPO {
        String name;
        double cpu;
        String content;
        long timestamp;

        @Override
        public String getKey() {
            return this.timestamp + "@" + this.name;
        }

        @Override
        public String getRoutingValue() {
            return null;
        }
    }

    private void write(long writeCount) {
        Data data = new Data();

        ESBatchRequest batchRequest = new ESBatchRequest();
        for (int i = 0; i < 200; i++) {
            data.name = "test" + i;
            data.cpu = 10.11;
            data.content = getContent(writeCount);
            data.timestamp = System.currentTimeMillis();
            // gateway写入
            batchRequest.addNode(BatchType.INDEX, INDEX_NAME1, TYPE_NAME, data.getKey(), JSON.toJSONString(data));

            batchRequest.addNode(BatchType.INDEX, INDEX_NAME2, TYPE_NAME, data.getKey(), JSON.toJSONString(data));

            batchRequest.addNode(BatchType.INDEX, INDEX_NAME11, TYPE_NAME, data.getKey(), JSON.toJSONString(data));
            batchRequest.addNode(BatchType.INDEX, INDEX_NAME12, TYPE_NAME, data.getKey(), JSON.toJSONString(data));
            batchRequest.addNode(BatchType.INDEX, INDEX_NAME13, TYPE_NAME, data.getKey(), JSON.toJSONString(data));
            batchRequest.addNode(BatchType.INDEX, INDEX_NAME14, TYPE_NAME, data.getKey(), JSON.toJSONString(data));


            batchRequest.addNode(BatchType.INDEX, INDEX_NAME21, TYPE_NAME, data.getKey(), JSON.toJSONString(data));
            batchRequest.addNode(BatchType.INDEX, INDEX_NAME22, TYPE_NAME, data.getKey(), JSON.toJSONString(data));
            batchRequest.addNode(BatchType.INDEX, INDEX_NAME23, TYPE_NAME, data.getKey(), JSON.toJSONString(data));
            batchRequest.addNode(BatchType.INDEX, INDEX_NAME24, TYPE_NAME, data.getKey(), JSON.toJSONString(data));

            batchRequest.addNode(BatchType.INDEX, INDEX_NAME31, TYPE_NAME, data.getKey(), JSON.toJSONString(data));
            batchRequest.addNode(BatchType.INDEX, INDEX_NAME32, TYPE_NAME, data.getKey(), JSON.toJSONString(data));
            batchRequest.addNode(BatchType.INDEX, INDEX_NAME33, TYPE_NAME, data.getKey(), JSON.toJSONString(data));
            batchRequest.addNode(BatchType.INDEX, INDEX_NAME34, TYPE_NAME, data.getKey(), JSON.toJSONString(data));

         /*   batchRequest.addNode(BatchType.INDEX, INDEX_NAME41, TYPE_NAME, data.getKey(), JSON.toJSONString(data));
            batchRequest.addNode(BatchType.INDEX, INDEX_NAME42, TYPE_NAME, data.getKey(), JSON.toJSONString(data));
            batchRequest.addNode(BatchType.INDEX, INDEX_NAME43, TYPE_NAME, data.getKey(), JSON.toJSONString(data));
            batchRequest.addNode(BatchType.INDEX, INDEX_NAME44, TYPE_NAME, data.getKey(), JSON.toJSONString(data));*/

            batchRequest.addNode(BatchType.INDEX, INDEX_NAME51, TYPE_NAME, data.getKey(), JSON.toJSONString(data));
            batchRequest.addNode(BatchType.INDEX, INDEX_NAME52, TYPE_NAME, data.getKey(), JSON.toJSONString(data));
            batchRequest.addNode(BatchType.INDEX, INDEX_NAME53, TYPE_NAME, data.getKey(), JSON.toJSONString(data));
            batchRequest.addNode(BatchType.INDEX, INDEX_NAME54, TYPE_NAME, data.getKey(), JSON.toJSONString(data));

          /*  batchRequest.addNode(BatchType.INDEX, INDEX_NAME71, TYPE_NAME, data.getKey(), JSON.toJSONString(data));
            batchRequest.addNode(BatchType.INDEX, INDEX_NAME72, TYPE_NAME, data.getKey(), JSON.toJSONString(data));
            batchRequest.addNode(BatchType.INDEX, INDEX_NAME73, TYPE_NAME, data.getKey(), JSON.toJSONString(data));
            batchRequest.addNode(BatchType.INDEX, INDEX_NAME74, TYPE_NAME, data.getKey(), JSON.toJSONString(data));*/
        }

        try {
            ESClient esClient = esOpClient.getESClient(CLUSTER_NAME);
            ESBatchResponse response = esClient.batch(batchRequest).actionGet(2, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getContent(long writeCount) {
        StringBuilder sb = new StringBuilder();
        for (long i = 0; i < writeCount * 20; i++) {
            sb.append(UUID.randomUUID().toString());
        }

        return sb.toString();
    }

    private void read(String indexName, long readCount) {
        String queryDsl = String.format("{\"query\": {\"match_all\": {}},\"size\": %d}", readCount);
        for (int i = 0; i < readCount; i++) {
            esGatewayClient.performRequest(CLUSTER_NAME, indexName, TYPE_NAME, queryDsl);
        }
    }

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        String log1 = "class=TestESReadAndWriteTask||method=execute||msg=start";
        LOGGER.info(log1);
        String indexName = IndexNameUtils.genDailyIndexName(INDEX_NAME1, 0);

        KeepReadAndWriteEntity keepReadAndWriteEntity = new KeepReadAndWriteEntity(10, 10);
        KeepReadAndWriteEntity objectSetting = ariusConfigInfoService.objectSetting("arius.test.config",
            "index.readAndWrite.frequency", keepReadAndWriteEntity, KeepReadAndWriteEntity.class);

        // 写入、查询
        ariusTaskThreadPool.run(() -> write(objectSetting.getWriteCount()));
        ariusTaskThreadPool.run(() -> read(indexName, objectSetting.getReadCount()));

        String log2 = "class=TestESReadAndWriteTask||method=execute||msg=end";

        return new TaskResult(TaskResult.SUCCESS_CODE, log1 + "\\r\\n" + log2);
    }

    static class KeepReadAndWriteEntity {
        private long writeCount;
        private long readCount;

        public KeepReadAndWriteEntity(long writeCount, long readCount) {
            this.writeCount = writeCount;
            this.readCount = readCount;
        }

        public KeepReadAndWriteEntity() {
        }

        public long getWriteCount() {
            return writeCount;
        }

        public void setWriteCount(long writeCount) {
            this.writeCount = writeCount;
        }

        public long getReadCount() {
            return readCount;
        }

        public void setReadCount(long readCount) {
            this.readCount = readCount;
        }
    }
}
