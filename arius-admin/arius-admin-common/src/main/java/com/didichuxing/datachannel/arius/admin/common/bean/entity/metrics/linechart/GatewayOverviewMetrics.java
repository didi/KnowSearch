package com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Created by fitz on 2021-08-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GatewayOverviewMetrics implements Serializable {

    private String                   type;

    private List<MetricsContentCell> metrics;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GatewayOverviewMetrics that = (GatewayOverviewMetrics) o;
        return type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    public void addMetrics(MetricsContentCell contentCell) {
        if (this.metrics == null) {
            this.metrics = Lists.newArrayList();
        }
        this.metrics.add(contentCell);
    }
}
