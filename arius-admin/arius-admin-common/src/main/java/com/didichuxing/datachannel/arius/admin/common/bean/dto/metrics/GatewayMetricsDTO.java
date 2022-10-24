package com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Created by fitz on 2021-08-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "gateway指标公共参数")
public abstract class GatewayMetricsDTO extends BaseDTO {
    private static final Long ONE_HOUR = 60 * 60 * 1000L;

    @ApiModelProperty("开始时间")
    private Long              startTime;

    @ApiModelProperty("结束时间")
    private Long              endTime;

    @ApiModelProperty("指标类型")
    private List<String>      metricsTypes;

    public void validParam() {
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        if (startTime == null) {
            startTime = endTime - ONE_HOUR;
        }
        //超过一周时间容易引起熔断，不允许
        if ((endTime - startTime) > ONE_HOUR * 24 * 7) {
            throw new RuntimeException("时间跨度不要超过一周");
        }

        if (CollectionUtils.isEmpty(metricsTypes)) {
            throw new RuntimeException("指标类型为空");
        }

    }

    public abstract String getGroup();

}
