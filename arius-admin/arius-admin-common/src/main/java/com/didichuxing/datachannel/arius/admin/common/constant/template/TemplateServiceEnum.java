package com.didichuxing.datachannel.arius.admin.common.constant.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ESClusterVersionEnum.ES_2_3_3_100;
import static com.didichuxing.datachannel.arius.admin.common.constant.ESClusterVersionEnum.ES_6_6_1_700;
import static com.didichuxing.datachannel.arius.admin.common.constant.ESClusterVersionEnum.ES_6_6_1_902;
import static com.didichuxing.datachannel.arius.admin.common.constant.ESClusterVersionEnum.ES_6_6_6_800;
import static com.didichuxing.datachannel.arius.admin.common.constant.ESClusterVersionEnum.ES_7_6_0_1100;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterTemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.constant.ESClusterVersionEnum;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zqr
 * @date 2020-09-09
 */
public enum TemplateServiceEnum {

                                 /**
                                  * 预创建：对于分区创建的索引，支持预创建，减轻集群负担，提高稳定性.
                                  */
                                 TEMPLATE_PRE_CREATE(1, "预创建", ES_2_3_3_100, 1),
                                     /**
                                  * Pipeline：提供索引分区规则（索引模版到具体的物理索引的映射）和写入限流能力.
                                  */
                                 TEMPLATE_PIPELINE(2, "Pipeline", ES_6_6_6_800, 1),
       /**
                                  * 索引规划：索引shard动态调整+indexRollover能力
                                  */
                                 INDEX_PLAN(3, "索引规划", ES_6_6_1_700, 1),

                                 /**
                                  * 过期删除：支持索引根据保存周期自动清理，避免磁盘过满.
                                  */
                                 TEMPLATE_DEL_EXPIRE(4, "过期删除", ES_2_3_3_100, 1),
                                 /**
                                  * 冷热分离：提供SSD和HDD两种类型的磁盘来保存索引，从而降低成本.
                                  */
                                 TEMPLATE_COLD(5, "冷热分离", ES_6_6_1_700, 1),
                                     /**
                                  * 索引别名：支持通过接口来设置和修改索引别名.
                                  */
                                 TEMPLATE_ALIASES(6, "索引别名", ES_6_6_1_700, 1),
    
    /**
     * Mapping 设置 ：提供修改索引的 mapping 的信息的功能.
     * 4-》7
     */
    TEMPLATE_MAPPING(7, "Mapping设置", ES_6_6_1_700, 1),


                                 /**
                                  * Setting 设置 ：提供修改索引 Setting 的信息的功能.
                                  * 5-》8
                                  */
                                 TEMPLATE_SETTING(8, "Setting设置", ES_6_6_1_700, 1),
                                     /**
                                  * 写入限流：对索引写入量进行限制，避免过大影响集群稳定性.
                                  */
                                 TEMPLATE_LIMIT_W(9, "写入限流", ES_6_6_6_800, 1),

    
                               

                                 /**
                                  * 跨集群同步（DCDR）：跨集群数据复制功能，用于集群间的数据复制，类似ES官方的CCR能力.
                                  */
                                 TEMPLATE_DCDR(10, "跨集群同步(DCDR)", ES_6_6_1_902, 1),



                              

                                 /**
                                  * 安全管控:对于索引的安全服务进行权限控制.
                                  */
                                 TEMPLATE_SECURITY(13, "安全管控", ES_7_6_0_1100, 0),

                                 /**
                                  * 清理
                                  */
                                  @Deprecated
                                 TEMPLATE_CLEAR(14, "清理", ES_2_3_3_100, 0),

                                 /**
                                  * 升级版本
                                  */
                                  @Deprecated
                                 TEMPLATE_UPGRADE(15, "升级版本", ES_2_3_3_100, 0),

                                 /**
                                  * 扩缩容（调整shard）
                                  */
                                 @Deprecated
                                 TEMPLATE_ADJUST_SHARD(16, "扩缩容", ES_2_3_3_100, 0);

    private Integer              code;

    private String               serviceName;

    private Integer              defaultSrv;

    private ESClusterVersionEnum esClusterVersion;

    public Integer getCode() {
        return code;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Integer getDefaultSrv() {
        return defaultSrv;
    }

    public ESClusterVersionEnum getEsClusterVersion() {
        return esClusterVersion;
    }

    TemplateServiceEnum(Integer code, String serviceName, ESClusterVersionEnum esClusterVersion, Integer defaultSrv) {
        this.code = code;
        this.serviceName = serviceName;
        this.esClusterVersion = esClusterVersion;
        this.defaultSrv = defaultSrv;
    }

    public static TemplateServiceEnum getById(int code) {
        for (TemplateServiceEnum templateServiceEnum : TemplateServiceEnum.values()) {
            if (templateServiceEnum.getCode().intValue() == code) {
                return templateServiceEnum;
            }
        }
        return null;
    }

    public static List<TemplateServiceEnum> allTemplateSrv() {
        return Arrays.asList(TemplateServiceEnum.values());
    }

    public boolean isDefaultSrv() {
        return 1 == getDefaultSrv();
    }

    public static String getDefaultSrvs() {
        List<TemplateServiceEnum> serviceEnumList = new ArrayList<>();
        for (TemplateServiceEnum templateServiceEnum : TemplateServiceEnum.values()) {
            if (templateServiceEnum.isDefaultSrv()) {
                serviceEnumList.add(templateServiceEnum);
            }
        }

        if (CollectionUtils.isEmpty(serviceEnumList)) {
            return "";
        }

        return String.join(",",
            serviceEnumList.stream().map(t -> String.valueOf(t.getCode())).collect(Collectors.toList()));
    }

    public static ClusterTemplateSrv convertFromEnum(TemplateServiceEnum serviceEnum) {
        if (Objects.isNull(serviceEnum)) {
            return null;
        }
        ClusterTemplateSrv clusterTemplateSrv = new ClusterTemplateSrv();
        clusterTemplateSrv.setServiceId(serviceEnum.getCode());
        clusterTemplateSrv.setServiceName(serviceEnum.getServiceName());
        clusterTemplateSrv.setEsVersion(serviceEnum.getEsClusterVersion().getVersion());
        return clusterTemplateSrv;
    }
    
    public static List<TemplateServiceEnum> usePartitionService(){
        
        
        return Lists.newArrayList(TemplateServiceEnum.TEMPLATE_PRE_CREATE,TemplateServiceEnum.TEMPLATE_DEL_EXPIRE,
                TemplateServiceEnum.TEMPLATE_COLD);
    }
    
    /**
     * str是否保存传入的枚举类
     *
     * @param openSrvStr  打开电脑,str
     * @param serviceEnum
     * @return boolean
     */
    public static boolean strContainsSrv(String openSrvStr, TemplateServiceEnum serviceEnum) {
        if (StringUtils.isBlank(openSrvStr)) {
            return Boolean.FALSE;
        }
        return Arrays.stream(StringUtils.split(openSrvStr, ",")).filter(StringUtils::isNumeric).map(Integer::parseInt)
                .map(TemplateServiceEnum::getById).filter(Objects::nonNull).anyMatch(srv -> srv.equals(serviceEnum));
    }
    
}