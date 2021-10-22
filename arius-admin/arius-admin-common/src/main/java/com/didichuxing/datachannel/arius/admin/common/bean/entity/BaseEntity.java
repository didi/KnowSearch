package com.didichuxing.datachannel.arius.admin.common.bean.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * @author d06679
 * @date 2019/3/22
 */
public class BaseEntity implements Serializable {
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
