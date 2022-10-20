package com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditDsl {
    private Integer       projectId;

    private String        userName;

    private List<DslInfo> dslInfos;

    /**
     * 是否有效
     *
     * @return
     */
    @JSONField(serialize = false)
    public boolean isVaild() {
        if (projectId == null) {
            return false;
        }

        if (StringUtils.isBlank(userName)) {
            return false;
        }

        if (CollectionUtils.isEmpty(dslInfos)) {
            return false;
        }

        return true;
    }
}