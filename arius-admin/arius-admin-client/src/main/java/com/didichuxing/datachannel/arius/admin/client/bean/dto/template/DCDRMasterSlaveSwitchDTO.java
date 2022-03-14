package com.didichuxing.datachannel.arius.admin.client.bean.dto.template;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by linyunan on 12/14/21
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "DCDRMasterSlaveSwitchDTO", description = "DCDR主从切换")
public class DCDRMasterSlaveSwitchDTO extends BaseVO {
    @ApiModelProperty("切换类型 1 平滑 2 强制")
    private Integer    type;

    @ApiModelProperty("主模板Id列表")
    private List<Long> templateIds;
}
