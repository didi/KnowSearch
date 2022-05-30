package com.didichuxing.datachannel.arius.admin.common.bean.po.app;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *
 * @author d06679
 * @date 2019/3/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectConfigPO extends BasePO {



    /**
     * projectId
     */
    private Integer projectId;

    /**
     * gateway返回结果解析开关
     */
    private Integer analyzeResponseEnable;

    /**
     * 是否生效DSL分析查询限流值 1为生效DSL分析查询限流值，0不生效DSL分析查询限流值
     */
    private Integer dslAnalyzeEnable;

    /**
     * 是否开启聚合分析  1 开启  0 不开启  默认为 1
     */
    private Integer aggrAnalyzeEnable;

    /**
     * 是否索引存储分离，1为分离，0为不分离，默认为0
     */
    private Integer isSourceSeparated;
    /**
     * 慢查询
     */
    private Integer slowQueryTimes;

}