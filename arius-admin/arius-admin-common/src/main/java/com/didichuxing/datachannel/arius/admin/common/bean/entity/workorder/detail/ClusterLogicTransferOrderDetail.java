package com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-06-17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterLogicTransferOrderDetail extends AbstractOrderDetail {

    private Integer targetAppId;

    private Integer sourceAppId;

    private Long    clusterLogicId;

    private String  clusterLogicName;

    private String  targetResponsible;

    private String  memo;

}
