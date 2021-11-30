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
    private String  health;
    private String  status;
    private String  index;
    private long    pri;
    private long    rep;
    private long    docsCount;
    private long    docsDeleted;
    private long    storeSize;
    private long    priStoreSize;
    private Boolean readFlag;
    private Boolean writeFlag;
    private boolean deleteFlag;
    private long    timestamp;

    public boolean getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    @Override
    @JSONField(serialize = false)
    public String getKey() {
        return cluster + "@" + index;
    }
}
