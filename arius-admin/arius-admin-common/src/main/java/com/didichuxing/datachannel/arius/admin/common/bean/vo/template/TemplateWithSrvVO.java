package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author chengxiang
 * @date 2022/5/18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "「模板服务」信息")
public class TemplateWithSrvVO extends BaseTemplateVO {

    @ApiModelProperty("所属集群")
    private List<String> cluster;

}
