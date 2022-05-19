package com.didichuxing.datachannel.arius.admin.common.bean.vo.template.srv;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
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
public class TemplateWithSrvVO extends BaseVO {

    @ApiModelProperty("模板ID")
    private Integer id;

    @ApiModelProperty("模板名称")
    private String name;

    @ApiModelProperty("所属集群")
    private List<String> cluster;

    @ApiModelProperty("开启的服务")
    private List<TemplateWithSrvVO> openSrv;

    @ApiModelProperty("不可用的服务")
    private List<UnavailableTemplateSrvVO> unavailableSrv;

}
