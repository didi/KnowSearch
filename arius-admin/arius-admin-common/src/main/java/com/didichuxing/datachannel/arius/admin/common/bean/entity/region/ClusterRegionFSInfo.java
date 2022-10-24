package com.didichuxing.datachannel.arius.admin.common.bean.entity.region;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chengxiang
 * @date 2022/06/08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterRegionFSInfo extends BaseEntity {

    private Integer regionId;

    private Long    totalInBytes     = 0L;

    private Long    freeInBytes      = 0L;

    private Long    availableInBytes = 0L;
}
