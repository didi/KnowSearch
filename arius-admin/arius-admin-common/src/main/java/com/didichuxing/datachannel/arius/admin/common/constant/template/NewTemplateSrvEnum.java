package com.didichuxing.datachannel.arius.admin.common.constant.template;

import com.didichuxing.datachannel.arius.admin.common.constant.ESClusterVersionEnum;
import java.util.Arrays;
import java.util.List;
import static com.didichuxing.datachannel.arius.admin.common.constant.ESClusterVersionEnum.*;

/**
 * @author chengxiang
 * @date 2022/5/17
 */
public enum NewTemplateSrvEnum {

    /**
     * 预创建：对于分区创建的索引，支持预创建，减轻集群负担，提高稳定性.
     */
    TEMPLATE_PRE_CREATE(1, "预创建", ES_2_3_3_100),

    /**
     * Pipeline：提供索引分区规则（索引模版到具体的物理索引的映射）和写入限流能力.
     */
    TEMPLATE_PIPELINE(2, "Pipeline", ES_6_6_6_800),

    /**
     * 索引规划：索引shard动态调整+indexRollover能力
     */
    INDEX_PLAN(3, "索引规划", ES_6_6_1_700),

    /**
     * 过期删除：支持索引根据保存周期自动清理，避免磁盘过满.
     */
    TEMPLATE_DEL_EXPIRE(4, "过期删除", ES_2_3_3_100),

    /**
     * 冷热分离：提供SSD和HDD两种类型的磁盘来保存索引，从而降低成本.
     */
    TEMPLATE_COLD(5, "冷热分离", ES_6_6_1_700),

    /**
     * 清理
     */
    TEMPLATE_CLEAR(6, "清理", ES_2_3_3_100),

    /**
     * 升级版本
     */
    TEMPLATE_UPGRADE(7, "升级版本", ES_2_3_3_100),

    /**
     * 扩缩容（调整shard）
     */
    TEMPLATE_ADJUST_SHARD(8, "扩缩容", ES_2_3_3_100),

    /**
     * 跨集群同步（DCDR）：跨集群数据复制功能，用于集群间的数据复制，类似ES官方的CCR能力.
     */
    TEMPLATE_DCDR(9, "跨集群同步(DCDR)", ES_6_6_1_902),;

    private Integer code;

    private String serviceName;

    private ESClusterVersionEnum esClusterVersion;

    public Integer getCode() {
        return code;
    }

    public String getServiceName() {
        return serviceName;
    }

    public ESClusterVersionEnum getEsClusterVersion() {
        return esClusterVersion;
    }

    NewTemplateSrvEnum(Integer code, String serviceName, ESClusterVersionEnum esClusterVersion) {
        this.code               = code;
        this.serviceName        = serviceName;
        this.esClusterVersion   = esClusterVersion;
    }

    public static NewTemplateSrvEnum getByCode(Integer code) {
        for (NewTemplateSrvEnum templateSrv: NewTemplateSrvEnum.values()) {
            if (templateSrv.getCode().equals(code)) {
                return templateSrv;
            }
        }
        return null;
    }

    public static List<NewTemplateSrvEnum> getAll() {
        return Arrays.asList(NewTemplateSrvEnum.values());
    }
}
