package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.metrics;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectMetrics {

    public static final String ES_2 = "2";
    public static final String ES_6 = "6";

    /**
     * 指标名字
     */
    private String              valueName;

    /**
     * 指标采集路径
     */
    private String              valueRoute;

    /**
     * 衍生计算需要的参数
     */
    private Map<String, String> deriveParam;

    /**
     * 指标计算方式
     */
    private MetricsComputeType  computeType;

    /**
     * 索引节点指标中是否包含,默认不包含
     */
    private boolean             isIndexToNodeMetrics = false;

    /**
     * 是否需要从Odin修正数据
     */
    private boolean             needCorrectFromOdin  = false;

    /**
     * 是否需要发送夜莺
     */
    private boolean             sendToN9e = false;

    @Data
    public static class Builder {
        private String              valueName;

        private String              valueRoute;

        private MetricsComputeType  computeType;

        private boolean             isIndexToNodeMetrics = false;

        private boolean             needCorrectFromOdin  = false;

        private boolean             sendToN9e            = false;

        private String              esVersion            = "";

        private Map<String, String> deriveParam          = Maps.newHashMap();

        public Builder valueName(String valueName) {
            this.valueName = valueName.trim();
            return this;
        }

        public Builder valueRoute(String valueRoute) {
            this.valueRoute = valueRoute.trim();
            return this;
        }

        public Builder computeType(MetricsComputeType computeType) {
            this.computeType = computeType;
            return this;
        }

        public Builder deriveParam(String name, String value) {
            deriveParam.put(name, value);
            return this;
        }

        public Builder bIndexToNodeMetrics() {
            this.isIndexToNodeMetrics = true;
            return this;
        }

        public Builder needCorrectFromOdin() {
            this.needCorrectFromOdin = true;
            return this;
        }

        public Builder sendToN9e() {
            this.sendToN9e = true;
            return this;
        }

        public CollectMetrics build() {
            return new CollectMetrics(this);
        }
    }

    public CollectMetrics(Builder builder) {
        this.valueName      = builder.getValueName();
        this.valueRoute     = builder.getValueRoute();
        this.computeType    = builder.getComputeType();
        this.deriveParam    = builder.getDeriveParam();
        this.isIndexToNodeMetrics = builder.isIndexToNodeMetrics();
        this.needCorrectFromOdin  = builder.isNeedCorrectFromOdin();
        this.sendToN9e = builder.isSendToN9e();
    }
}
