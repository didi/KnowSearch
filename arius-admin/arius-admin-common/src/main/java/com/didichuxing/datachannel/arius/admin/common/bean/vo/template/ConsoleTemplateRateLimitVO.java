package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@ApiModel("索引限流信息")
public class ConsoleTemplateRateLimitVO extends BaseVO {
    @ApiModelProperty("限流大小")
    private Integer rateLimit;
}
