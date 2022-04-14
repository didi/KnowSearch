package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 *
 * @author d06679
 * @date 2019/1/15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "模板信息")
public class GatewayTemplateDeployInfoVO extends BaseVO {

    /**
     * 模板基础信息
     */
    @ApiModelProperty("基本信息")
    private GatewayTemplateVO                     baseInfo;

    /**
     * 主模板独有的信息
     */
    @ApiModelProperty("master信息")
    private GatewayTemplatePhysicalDeployVO       masterInfo;

    /**
     * 从模板独有的信息
     */
    @ApiModelProperty("slave信息")
    private List<GatewayTemplatePhysicalDeployVO> slaveInfos;

}
