package com.didichuxing.datachannel.arius.admin.common.bean.dto.monitor;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "监控告警")
public class MonitorSilenceDTO extends BaseDTO {
    @ApiModelProperty(value = "ID, 修改时传")
    private Long id;

    @ApiModelProperty(value = "告警ID")
    private Long monitorId;

    @ApiModelProperty(value = "开始时间")
    private Long startTime;

    @ApiModelProperty(value = "结束时间")
    private Long endTime;

    @ApiModelProperty(value = "备注")
    private String description;

    public boolean paramLegal() {
        if (null == monitorId
                || null == startTime
                || null == endTime
                || null == description) {
            return false;
        }
        return true;
    }
}