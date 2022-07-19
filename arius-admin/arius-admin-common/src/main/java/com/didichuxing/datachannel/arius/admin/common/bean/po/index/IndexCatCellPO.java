package com.didichuxing.datachannel.arius.admin.common.bean.po.index;

import com.alibaba.fastjson.annotation.JSONField;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lyn
 * @date 2021/09/30
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexCatCellPO extends BaseESPO {
    private String  cluster;
    private String  clusterPhy;
    private String  clusterLogic;
    private Long    resourceId;
    private Integer projectId;
    private String  health;
    private String  status;
    private String  index;
    private Long    pri;
    private Long    rep;
    private Long    docsCount;
    private Long    docsDeleted;
    private Long    storeSize;
    private Long    priStoreSize;
    private Boolean readFlag;
    private Boolean writeFlag;
    private Boolean deleteFlag;
    private Long    timestamp;

    private Long    primariesSegmentCount;
    private Long    totalSegmentCount;
    private Integer templateId;

    @Override
    @JSONField(serialize = false)
    public String getKey() {
        return cluster + "@" + index;
    }

    @Override
    public String getRoutingValue() {
        return null;
    }
}