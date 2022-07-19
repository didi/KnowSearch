package com.didi.arius.gateway.core.component.log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.common.metrics.log.DslLogEntity;
import com.didi.arius.gateway.common.metrics.log.DslMetricHelper;
import com.didi.arius.gateway.core.component.log.process.LogProcess;
import com.didi.arius.gateway.core.component.log.process.MetricLogProcess;
import com.didi.arius.gateway.core.component.log.process.TemplateLogProcess;
import com.didi.arius.gateway.core.service.ESRestClientService;
import com.didi.arius.gateway.core.service.arius.IndexTemplateService;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author didi
 * @date 2021-09-16 11:21 下午
 */
@Component
public class DslLogManager extends AbstractAggLogManager {
    public static final String LOG_TIME = "logTime";
    public static final String FLINK_TIME = "flinkTime";
    public static final String TIMESTAMP = "timeStamp";
    public static final String VERSION = "version";
    public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS Z";
    public static final String[] OUTPUT_FIELD_NAME = new String[]{
            "appid", "dslTemplate", "dslTemplateMd5", "timeStamp", "dsl", "indices", "indiceSample", "requestType", "searchType",
            "dslType", "isFromUserConsole", "version", "dslLenAvg", "responseLenAvg", "beforeCostAvg", "esCostAvg",
            "totalCostAvg", "successfulShardsAvg", "totalShardsAvg", "failedShardsAvg", "totalHitsAvg", "searchCount", "gatewayNode", "appidDslTemplateMd5","projectIdDslTemplateMd5"
    };
    public static final int ONE = 1;
    public static final int INTERVAL_MINUTES = 1;

    @Autowired
    private ESRestClientService esRestClientService;

    @Autowired
    protected IndexTemplateService indexTemplateService;

    @PostConstruct
    public void init() {
        DslLogProcessDecorator logProcess = new DslLogProcessDecorator(
                new MetricLogProcess(esRestClientService, indexTemplateService),
                new TemplateLogProcess(esRestClientService, indexTemplateService)
        );
        super.init(ONE, logProcess, INTERVAL_MINUTES);
    }

    public class DslLogProcessDecorator implements Runnable {

        List<LogProcess> logProcessList = new ArrayList<>();

        public DslLogProcessDecorator(LogProcess... logProcesses) {
            Arrays.stream(logProcesses).forEach(x -> logProcessList.add(x));
        }

        @Override
        public void run() {
            Collection<DslLogEntity> dslLogEntities = DslMetricHelper.getDslLogMap().values();
            if (dslLogEntities.size() > 0) {
                //这里清空，如果写失败也丢弃日志，等待下一批再写入
                DslMetricHelper.resetMap();
                List<JSONObject> records = dslLogEntities.stream().map(x -> dslLog(x)).collect(Collectors.toList());
                logProcessList.stream().forEach(process -> process.dealLog(records));
            }
        }

        private JSONObject dslLog(DslLogEntity dslLogEntity) {
            calculateAvg(dslLogEntity);
            if (null == dslLogEntity.getIndiceSample()) {
                dslLogEntity.setIndiceSample(dslLogEntity.getIndices());
            }
            JSONObject mapResult = new JSONObject();
            JSONObject dslJs = (JSONObject) JSON.toJSON(dslLogEntity);
            SimpleDateFormat sd = new SimpleDateFormat(TIME_FORMAT);
            Arrays.stream(OUTPUT_FIELD_NAME).forEach(field -> {
                mapResult.put(field, dslJs.get(field));
            });
            mapResult.put(LOG_TIME, sd.format(new Date(dslJs.getLong(TIMESTAMP))));
            mapResult.put(FLINK_TIME, sd.format(new Date()));
            mapResult.put(VERSION, "V2");
            return mapResult;
        }


        private void calculateAvg(DslLogEntity dslLogEntity) {
            double searchCount = dslLogEntity.getSearchCount();
            dslLogEntity.setDslLenAvg(dslLogEntity.getDslLen() / searchCount);
            dslLogEntity.setResponseLenAvg(dslLogEntity.getResponseLen() / searchCount);
            dslLogEntity.setBeforeCostAvg(dslLogEntity.getBeforeCost() / searchCount);
            dslLogEntity.setEsCostAvg(dslLogEntity.getEsCost() / searchCount);
            dslLogEntity.setTotalCostAvg(dslLogEntity.getTotalCost() / searchCount);
            dslLogEntity.setTotalShardsAvg(dslLogEntity.getTotalShards() / searchCount);
            dslLogEntity.setTotalHitsAvg(dslLogEntity.getTotalHits() / searchCount);
            dslLogEntity.setFailedShardsAvg(dslLogEntity.getFailedShards() / searchCount);
            dslLogEntity.setSuccessfulShardsAvg(dslLogEntity.getTotalShardsAvg() - dslLogEntity.getFailedShardsAvg());
        }
    }

}