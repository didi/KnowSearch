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
@ApiModel(description = "指标过滤规则")
public class MonitorStrategyFilterDTO extends BaseDTO {
    @ApiModelProperty(value = "指标")
    private String tkey;

    @ApiModelProperty(value = "条件")
    private String topt;

    @ApiModelProperty(value = "值")
    private List<String> tval;

    public boolean paramLegal() {
        if (StringUtils.isBlank(tkey)
                || StringUtils.isBlank(topt)
                || CollectionUtils.isEmpty(tval)) {
            return false;
        }
        return true;
    }
}