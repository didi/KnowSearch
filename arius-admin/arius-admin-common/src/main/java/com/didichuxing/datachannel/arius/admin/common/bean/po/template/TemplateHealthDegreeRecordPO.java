package com.didichuxing.datachannel.arius.admin.common.bean.po.template;

import com.alibaba.fastjson.annotation.JSONField;
import com.didichuxing.datachannel.arius.admin.common.util.DateTimeUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateHealthDegreeRecordPO extends TemplateHealthDegreePO {
    /**
     * 统计时间
     */
    private Long timestamp;


    /**
     * 获取主键key
     *
     * @return
     */
    @JSONField(serialize = false)
    @Override
    public String getKey() {
        return String.format("%d_%s", getLogicTemplateId(), DateTimeUtil.formatTimestamp(timestamp));
    }
}
