package com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateTransferOrderDetail extends AbstractOrderDetail {
    private Integer id;

    /**
     * 目标ProjectId
     */
    private Integer sourceProjectId;

    /**
     * 目标ProjectId
     */
    private Integer tgtProjectId;

    /**
     * 名字
     */
    private String  name;


}