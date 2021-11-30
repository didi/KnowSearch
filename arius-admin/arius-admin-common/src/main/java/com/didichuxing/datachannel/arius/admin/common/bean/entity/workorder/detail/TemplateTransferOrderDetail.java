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
     * 目标APPID
     */
    private Integer sourceAppId;

    /**
     * 目标APPID
     */
    private Integer tgtAppId;

    /**
     * 名字
     */
    private String  name;

    /**
     * 目标责任人
     */
    private String  tgtResponsible;
}
