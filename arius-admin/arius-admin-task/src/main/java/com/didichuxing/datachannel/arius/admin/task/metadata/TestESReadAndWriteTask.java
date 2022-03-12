package com.didichuxing.datachannel.arius.admin.task.metadata;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusTaskThreadPool;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESGatewayClient;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;

import lombok.EqualsAndHashCode;

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
public class TestESReadAndWriteTask implements Job {

    private static final Logger    LOGGER = LoggerFactory.getLogger(TestESReadAndWriteTask.class);

    @Autowired
    private ESGatewayClient        esGatewayClient;

    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

    private AriusTaskThreadPool    ariusTaskThreadPool;

    @PostConstruct
    private void init() {
        ariusTaskThreadPool = new AriusTaskThreadPool();
        ariusTaskThreadPool.init(20, "TestESReadAndWriteTask");
    }

    private static final String           TYPE_NAME          = "_doc";

    private static final String           TEMPLATE_NAME      = "keep_read_and_write";

    @lombok.Data
    @EqualsAndHashCode(callSuper = true)
    static class Data extends BaseESPO {
        String name;
        String content;
        Long   logTime;

        @Override
        public String getKey() {
            return UUID.randomUUID().toString();
        }
    }

    private void write(long writeCount) {
        Data data = new Data();

        for (int i = 0; i < writeCount; i++) {
            data.name = "test" + i;
            data.content = getContent(writeCount);
            data.logTime = System.currentTimeMillis();
            // gateway写入
            esGatewayClient.performWriteRequest(null, TEMPLATE_NAME, TYPE_NAME, JSON.toJSONString(data));
        }
    }

    private String getContent(long writeCount) {
        StringBuilder sb = new StringBuilder();
        for (long i = 0; i < writeCount * 5; i++) {
            sb.append("商业数据性能测试,");
        }

        return sb.toString();
    }

    private void read(String indexName, long readCount) {
        String queryDsl = String.format("{\"query\": {\"match_all\": {}},\"size\": %d}", readCount);
        for (int i = 0; i < readCount; i++) {
            esGatewayClient.performRequest(null, indexName, TYPE_NAME, queryDsl);
        }
    }

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        String log1 = "class=TestESReadAndWriteTask||method=execute||msg=start";
        LOGGER.info(log1);
        String indexName = IndexNameUtils.genDailyIndexName(TEMPLATE_NAME, 0);

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
