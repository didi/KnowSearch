package com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.list;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-08-01
 * 
 * 列表类型的指标类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricList extends BaseEntity {
    /**
     * 当前时间
     */
    private Long                    currentTime;

    /**
     * 指标类型
     */
    private String                  type;

    /**
     * 具体指标信息
     */
    private List<MetricListContent> metricListContents;
}