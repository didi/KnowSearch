package com.didi.arius.gateway.common.metadata;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author weizijun
 * @date：2016年8月18日
 */
@Data
@NoArgsConstructor
public class AppDetail {
    private int id;
    private Integer projectId;

    /**
     * appid授权的索引列表
     */
    private List<String> indexExp;

    /**
     * appid授权写权限的索引列表
     */
    private List<String> windexExp;

    /**
     * appid校验码
     */
    private String verifyCode;

    /**
     * appid配置的白名单列表
     */
    private List<String> ip;

    /**
     * appid对应的集群名称
     */
    private String cluster;

    /**
     * appid限流值
     */
    private int queryThreshold;

    /**
     * 是否生效DSL分析查询限流值
     */
    private boolean dslAnalyzeEnable;

    /**
     * 是否生效聚合查询分析
     */
    private boolean aggrAnalyzeEnable;

    /**
     * 是否生效记录响应结果的索引列表
     */
    private boolean analyzeResponseEnable;

    private RequestType searchType;

    private Integer isRoot;

    public enum RequestType {
        CLUSTER(0),
        INDEX(1),
        ORIGIN_CLUSTER(2);

        int type;

        public int getType() {
            return type;
        }

        private RequestType(int type) {
            this.type = type;
        }

        public static RequestType integerToType(int code) {
            for (RequestType type : RequestType.values()) {
                if (type.type == code) {
                    return type;
                }
            }
            return CLUSTER;
        }
    }
}