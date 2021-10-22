package com.didichuxing.datachannel.arius.admin.common.constant.template;

import com.didichuxing.datachannel.arius.admin.common.constant.ESClusterVersionEnum;

import java.util.Arrays;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ESClusterVersionEnum.*;

/**
 * @author zqr
 * @date 2020-09-09
 */
public enum TemplateServiceEnum {
    /**
     * 预创建索引模板服务
     */
    TEMPLATE_PRE_CREATE(1,   "预创建", ES_2_3_3_100, 1),

    TEMPLATE_DEL_EXPIRE(2,   "过期删除", ES_2_3_3_100, 1),

    TEMPLATE_PIPELINE(3, "Pipeline", ES_6_6_6_800, 1),

    TEMPLATE_MAPPING(4, "Mapping", ES_6_6_1_700, 1),

    TEMPLATE_SETTING(5, "Setting", ES_6_6_1_700, 1),

    TEMPLATE_LIMIT_W(6, "写入限流", ES_6_6_6_800, 1),

    TEMPLATE_DCDR(7, "Dcdr(主备)", ES_6_6_6_900, 1),

    TEMPLATE_ALIASES(8, "索引别名", ES_6_6_1_700, 1),

    TEMPLATE_QUOTA(9, "资源管控", ES_6_6_1_700, 1),

    TEMPLATE_SECURITY(10, "安全管控", ES_7_6_0_1100, 1),

    TEMPLATE_CAPA_PLAN(11, "容量规划", ES_6_6_1_700, 1),

    TEMPLATE_COLD(12, "冷存", ES_6_6_1_700, 1),

    TEMPLATE_SHARD(13, "Shard调整", ES_6_6_1_700, 1),

    ;

    private Integer code;

    private String serviceName;

    private Integer defaultSrv;

    private ESClusterVersionEnum esClusterVersion;

    public Integer getCode() {
        return code;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Integer getDefaultSrv(){ return defaultSrv; }

    public ESClusterVersionEnum getEsClusterVersion() {
        return esClusterVersion;
    }

    TemplateServiceEnum(Integer code, String serviceName, ESClusterVersionEnum esClusterVersion, Integer defaultSrv) {
        this.code               = code;
        this.serviceName        = serviceName;
        this.esClusterVersion   = esClusterVersion;
        this.defaultSrv         = defaultSrv;
    }

    public static TemplateServiceEnum getById(int code){
        for (TemplateServiceEnum templateServiceEnum : TemplateServiceEnum.values()) {
            if (templateServiceEnum.getCode().intValue() == code) {
                return templateServiceEnum;
            }
        }
        return null;
    }

    public static List<TemplateServiceEnum> allTemplateSrv(){
        return Arrays.asList(TemplateServiceEnum.values());
    }

    public boolean isDefaultSrv() {
        return 1 == getDefaultSrv();
    }
}
