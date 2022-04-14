package com.didichuxing.datachannel.arius.admin.biz.workorder.content;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-06-17
 */
@Data
@NoArgsConstructor
public class ClusterLogicTransferContent extends BaseContent {

    private Integer targetAppId;

    private Integer sourceAppId;

    private Long    clusterLogicId;

    private String  clusterLogicName;

    private String  targetResponsible;

    private String  memo;
}
