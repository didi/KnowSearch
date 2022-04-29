package com.didichuxing.datachannel.arius.admin.common.bean.dto.template;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateSettingDTO extends BaseDTO {

    @ApiModelProperty("索引ID")
    private Integer logicId;

    @ApiModelProperty("是否开启副本")
    private boolean cancelCopy;

    @ApiModelProperty("是否开启异步translog")
    private boolean asyncTranslog;
}
