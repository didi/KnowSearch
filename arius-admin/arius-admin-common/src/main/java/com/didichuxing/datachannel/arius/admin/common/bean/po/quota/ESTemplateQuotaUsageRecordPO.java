package com.didichuxing.datachannel.arius.admin.common.bean.po.quota;

import java.util.Date;

import com.didichuxing.datachannel.arius.admin.common.util.AriusDateUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019-09-03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ESTemplateQuotaUsageRecordPO extends ESTemplateQuotaUsagePO {

    /**
     * 时间戳
     */
    private Date timestamp;

    /**
     * 获取主键key
     *
     * @return
     */
    @Override
    public String getKey() {
        return getLogicId() + "@" + AriusDateUtils.date2Str(timestamp, "yyyy-MM-dd HH:mm");
    }

}
