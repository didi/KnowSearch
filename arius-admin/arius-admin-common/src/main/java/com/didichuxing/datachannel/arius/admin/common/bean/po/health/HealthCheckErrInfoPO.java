package com.didichuxing.datachannel.arius.admin.common.bean.po.health;


import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HealthCheckErrInfoPO extends BaseESPO {
    private Long        id;
    private Long        recordId;
    private String      checkTypeName;
    private String      template;
    private String      idx;
    private String      type;
    private String      shard;
    private String      node;
    private String      rack;
    private String      extendInfo;
    private String      value;
    private int         status;

    @Override
    public String getKey() {
        return template;
    }
}
