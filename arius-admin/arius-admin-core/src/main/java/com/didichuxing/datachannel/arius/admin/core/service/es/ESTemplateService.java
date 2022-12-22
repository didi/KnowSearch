package com.didichuxing.datachannel.arius.admin.core.service.es;

import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didiglobal.knowframework.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.knowframework.elasticsearch.client.response.setting.template.MultiTemplatesConfig;
import com.didiglobal.knowframework.elasticsearch.client.response.setting.template.TemplateConfig;
import java.util.List;
import java.util.Map;

/**
 * @author d06679
 * @date 2019/4/2
 */
public interface ESTemplateService {

    /**
     * 删除模板
     * @param cluster 集群名字
     * @param name 模板名字
     * @param retryCount 重试次数
     * @return result
     * @throws ESOperateException
     */
    boolean syncDelete(String cluster, String name, int retryCount) throws ESOperateException;

    /**
     * 修改模板rack和shard
     * @param cluster 集群
     * @param name 模板明细
     * @param shard shard
     * @param retryCount 重试次数
     * @return result
     * @throws ESOperateException
     */
    boolean syncUpdateShard(String cluster, String name, Integer shard, Integer shardRouting,
                            int retryCount) throws ESOperateException;

    /**
     * 创建模板, 会覆盖之前的存在的
     * @param cluster 集群
     * @param name 模板名字
     * @param expression 表达式
     * @param shard shard
     * @param retryCount 重试次数
     * @return result
     * @throws ESOperateException
     */
    boolean syncCreate(String cluster, String name, String expression, Integer shard, Integer shardRouting,
                       int retryCount) throws ESOperateException;

    /**
     * 创建模板, 会覆盖之前的存在的
     * @param settings 模板settings
     * @param cluster 集群
     * @param name 模板名字
     * @param expression 表达式
     * @param mappings 模板mappings
     * @param retryCount 重试次数
     * @return
     * @throws ESOperateException
     */
    boolean syncCreate(Map<String, String> settings, String cluster, String name, String expression,
                       MappingConfig mappings, int retryCount) throws ESOperateException;

    /**
     * 修改模板
     * @param cluster 集群
     * @param name 模板名字
     * @param expression 表达式
     * @param retryCount 重试次数
     * @return result
     * @throws ESOperateException
     */
    boolean syncUpdateExpression(String cluster, String name, String expression,
                                 int retryCount) throws ESOperateException;

    /**
     * 修改模板分片数量
     * @param cluster 集群
     * @param name 模板姓名
     * @param shardNum 分片数目
     * @param retryCount 重试次数
     * @return
     * @throws ESOperateException
     */
    boolean syncUpdateShardNum(String cluster, String name, Integer shardNum, int retryCount) throws ESOperateException;

    /**
     * 修改模板setting
     * @param cluster 集群
     * @param name 模板明细
     * @param setting 配置
     * @param retryCount 重试次数
     * @return result
     * @throws ESOperateException
     */
    boolean syncUpsertSetting(String cluster, String name, Map<String, String> setting,
                              int retryCount) throws ESOperateException;

    /**
     * 更新集群的设置，检查分配和分片是否正确
     *
     * @param cluster 集群名称
     * @param name 索引的名称
     * @param setting 要更新的设置
     * @param retryCount 重试次数。
     */
    boolean syncUpdateSettingCheckAllocationAndShard(String cluster, String name, Map<String, String> setting,
                                                     int retryCount) throws ESOperateException;
    /**
     * 跨集群拷贝模板mapping和索引
     * @param srcCluster 源集群
     * @param srcTemplateName 原模板
     * @param tgtCluster 目标集群
     * @param tgtTemplateName 目标模板
     * @param retryCount 重试次数
     * @return result
     */
    boolean syncCopyMappingAndAlias(String srcCluster, String srcTemplateName, String tgtCluster,
                                    String tgtTemplateName, int retryCount) throws ESOperateException;

    /**
     * 同步更新物理模板配置
     * @param cluster 集群名称
     * @param templateName 物理模板名称
     * @param templateConfig 模板配置
     * @param retryCount 重试次数
     * @return
     * @throws ESOperateException
     */
    boolean syncUpdateTemplateConfig(String cluster, String templateName, TemplateConfig templateConfig,
                                     int retryCount) throws ESOperateException;

    /**
     * 获取模板信息
     * @param cluster 集群
     * @param name    名字
     * @return Config
     */
    TemplateConfig syncGetTemplateConfig(String cluster, String name) throws ESOperateException;

    /**
     * 获取模板的mapping配置
     *
     * @param clusterName
     * @param templateName
     * @return
     */
    MappingConfig syncGetMappingsByClusterName(String clusterName, String templateName) throws ESOperateException;

    /**
     * 获取模板配置
     * @param clusterName 集群名称
     * @param templateName 模板名称
     * @return
     */
    MultiTemplatesConfig syncGetTemplates(String clusterName, String templateName) throws ESOperateException;

    /**
     * 获取所有引擎模板
     * @param clusters 集群名
     * @return
     */
    Map<String, TemplateConfig> syncGetAllTemplates(List<String> clusters) throws ESOperateException;

    /**
     * 修改模板名称
     * @param cluster 集群
     * @param srcName 源名称
     * @param tgtName 现名称
     * @return
     */
    boolean syncUpdateName(String cluster, String srcName, String tgtName, int retryCount) throws ESOperateException;

    /**
     * 验证模板配置是否正常
     * @param cluster 物理集群名称
     * @param name 模板名称
     * @param templateConfig 模板配置
     * @return
     * @throws ESOperateException
     */
    boolean syncCheckTemplateConfig(String cluster, String name, TemplateConfig templateConfig,
                                    int retryCount) throws ESOperateException;

    /**
     * 获取集群模板个数, 不包涵原生自带模板
     */
    long syncGetTemplateNum(String cluster);

    /**
     * 获取集群模板个数，兼容2.3.3低版本的集群
     * @param cluster 物理集群名称
     * @return 集群模板个数
     */
    long synGetTemplateNumForAllVersion(String cluster) throws ESOperateException;
    
    boolean syncGetEsClusterIsNormal(String cluster);
    
    /**
     * > 检查指定集群的索引是否匹配指定的表达式和模板健康状态
     *
     * @param cluster 集群名称
     * @param expression 索引的表达式，如“log-*”
     * @param templateHealthEnum 模板的健康状态，为枚举类型，枚举值如下：
     * @return 布尔值
     */
    boolean hasMatchHealthIndexByExpressionTemplateHealthEnum(String cluster, String expression,
                                                                     TemplateHealthEnum templateHealthEnum) ;

    /**
     * 从元数据索引 arius_cat_index_info 中获取模版每个索引的health状态，从而确定模版health
     * @param cluster 集群名称
     * @param wildcard 通配符，如“log-*”
     * @return  模版健康状态
     */
    Integer getTemplateHealthCode(String cluster, String wildcard);
}