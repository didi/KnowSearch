package com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.config.OpManagerConfigVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 网关配置
 *
 * @author shizeying
 * @date 2022/10/20
 * @since 0.3.2
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "gateway 配置信息")
public class GatewayConfigVO extends OpManagerConfigVO {

    @ApiModelProperty("节点列表")
    private List<GatewayClusterNodeVO> nodes;
    
}