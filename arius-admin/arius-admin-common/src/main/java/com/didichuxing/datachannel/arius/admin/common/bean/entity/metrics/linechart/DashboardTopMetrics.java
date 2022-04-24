package com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTopMetrics extends TopMetrics {
    private List<Tuple<String/*集群名称*/, String/*索引名称/节点名称/集群名称/模板名称*/>> dashboardTopInfo;
}
