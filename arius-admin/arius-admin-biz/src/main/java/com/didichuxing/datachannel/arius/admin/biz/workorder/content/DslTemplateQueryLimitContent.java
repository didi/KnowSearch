package com.didichuxing.datachannel.arius.admin.biz.workorder.content;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.dsl.DslQueryLimitDTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DslTemplateQueryLimitContent {

    /**
     * 查询语句限流值相关参数
     */
    private List<DslQueryLimitDTO> dslQueryLimitDTOList;

    /**
     * 项目id
     */
    private Integer projectId;

    /**
     * 操作者
     */
    private String operator;
}
