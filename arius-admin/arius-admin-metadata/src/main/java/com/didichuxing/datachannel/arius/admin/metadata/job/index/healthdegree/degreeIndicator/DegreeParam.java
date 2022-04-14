package com.didichuxing.datachannel.arius.admin.metadata.job.index.healthdegree.degreeIndicator;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.IndexRealTimeInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.IndicatorChild;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESIndexToNodeStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DegreeParam {
    IndexTemplateLogicWithClusterAndMasterTemplate indexTemplate;

    long templateDocNu;

    double templateSizeInBytes;

    long templateAccessCount;

    IndexRealTimeInfo todayReaTimelInfo;

    IndexRealTimeInfo yesdayReaTimelInfo;

    List<ESIndexToNodeStats> esIndexToNodeStats;

    List<IndicatorChild> indicatorChilds;

    @Override
    public String toString() {
        return "DegreeParam" +
                ": template="               + indexTemplate.getName()  +
                ": templateDocNu="          + templateDocNu   +
                ": templateSizeInBytes="    + templateSizeInBytes +
                ": templateAccessCount="    + templateAccessCount   +
                ": todayReaTimelInfo="      + todayReaTimelInfo.toString()   +
                ", yesdayReaTimelInfo="     + yesdayReaTimelInfo.toString() +
                ", esIndexToNodeStats="  + JSON.toJSONString( esIndexToNodeStats );
    }
}
