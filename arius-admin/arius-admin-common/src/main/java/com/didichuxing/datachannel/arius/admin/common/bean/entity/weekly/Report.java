package com.didichuxing.datachannel.arius.admin.common.bean.entity.weekly;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithStats;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: CT17534
 * @date: 2020-03-11 23:07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report extends BaseEntity {

    /**
     * 应用Id
     */
    private Integer                           appId;

    /**
     * 接收人
     */
    private String                            receive;

    /**
     * 模版统计信息: 平均tps、平均qps、quota配额、索引存储容量、索引成本、索引健康分、索引价值分、不健康标签
     *
     */
    private List<IndexTemplateWithStats> templateStatsAndBaseInfo;

    /**
     * App查询信息(top10)
     */
    private List<AppQuery>                    appQuery;

}
