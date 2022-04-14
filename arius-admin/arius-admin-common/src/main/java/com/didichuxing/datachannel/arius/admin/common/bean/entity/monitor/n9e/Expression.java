package com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.n9e;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Expression {
    /**
     * 所有触发还是触发一条即可，=0所有， =1一条
     */
    @JSONField(name = "together_or_any")
    private Integer togetherOrAny = 1;
    @JSONField(name = "trigger_conditions")
    List<Exp> triggerConditions;
    @JSONField(name = "tags_filters")
    List<TagFilter> tagFilters;
}
