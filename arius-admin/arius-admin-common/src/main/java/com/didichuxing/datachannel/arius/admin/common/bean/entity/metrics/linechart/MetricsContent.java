package com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-08-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricsContent implements Serializable {

    private String                   name;

    private List<MetricsContentCell> metricsContentCells;
}
