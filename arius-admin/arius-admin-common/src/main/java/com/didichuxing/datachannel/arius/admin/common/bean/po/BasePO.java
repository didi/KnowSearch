package com.didichuxing.datachannel.arius.admin.common.bean.po;

import java.io.Serializable;
import java.util.Date;

/**
 *
 *
 * @author d06679
 * @date 2019/3/13
 */
public class BasePO implements Serializable {

    protected Date createTime;

    protected Date updateTime;

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
