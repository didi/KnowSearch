package com.didichuxing.datachannel.arius.admin.common.bean.po.health;

import com.alibaba.fastjson.annotation.JSONField;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HealthCheckRecordPo extends BaseESPO {
    private Long        id;
    private int         typeId;
    private String      typeName;
    private String      cluster;
    private Timestamp   beginTime;
    private Timestamp   endTime;
    private int         result;
    private int         errRate;
    private long        checkCount;
    private long        errCount;

    @Override
    @JSONField(serialize = false)
    public String getKey() {
        return cluster + "_" + typeName;
    }
}
