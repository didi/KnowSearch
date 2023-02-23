package com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.dsl.DslQueryLimitDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wuxuan
 * @date 2022/11/15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DslTemplateQueryLimitDetail extends AbstractOrderDetail{
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
