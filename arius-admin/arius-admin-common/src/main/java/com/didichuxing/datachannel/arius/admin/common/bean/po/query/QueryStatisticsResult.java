package com.didichuxing.datachannel.arius.admin.common.bean.po.query;

import com.google.common.collect.Maps;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2019/2/27 下午2:49
 * @modified By D10865
 *
 * 查询统计子任务结果
 *
 */
@Deprecated
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryStatisticsResult {
    /**
     *  索引模板维度访问次数
     */
    private Map<String/*templateId*/, TemplateAccessCountPO> templateAccessCountMap = Maps.newLinkedHashMap();
    /**
     * 索引维度访问次数
     */
    private Map<String/*templateId_indexName*/, IndexNameAccessCountPO> indexNameAccessCountMap = Maps.newLinkedHashMap();
    /**
     * appid维度访问次数
     */
    private Map<String/*templateId_appId*/, AppIdTemplateAccessCountPO> appIdTemplateAccessCountMap = Maps.newLinkedHashMap();

}