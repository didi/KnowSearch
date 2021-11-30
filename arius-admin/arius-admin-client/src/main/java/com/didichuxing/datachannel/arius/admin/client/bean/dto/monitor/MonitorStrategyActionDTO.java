package com.didichuxing.datachannel.arius.admin.client.bean.dto.monitor;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "通知方式")
public class MonitorStrategyActionDTO extends BaseDTO {
    @ApiModelProperty(value = "报警组")
    private List<String> notifyGroup;

    @ApiModelProperty(value = "连续报警时间")
    private String converge;

    @ApiModelProperty(value = "回调地址")
    private String callback;

    public boolean paramLegal() {
        if (CollectionUtils.isEmpty(notifyGroup)
                || StringUtils.isBlank(converge)) {
            return false;
        }

        callback = (null == callback) ? "" : callback;
        return true;
    }
}