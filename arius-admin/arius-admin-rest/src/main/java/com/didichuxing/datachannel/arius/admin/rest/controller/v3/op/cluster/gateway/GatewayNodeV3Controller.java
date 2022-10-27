package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.gateway;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayNodeConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterNodeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 网关节点v3
 *
 * @author shizeying
 * @date 2022/10/25
 * @since 0.3.2
 */
@NoArgsConstructor
@RestController()
@RequestMapping(V3 + "/gateway/node")
@Api(tags = "GATEWAY NODE管理接口 (REST)")
public class GatewayNodeV3Controller {
    @PostMapping("/page")
    @ResponseBody
    @ApiOperation(value = "按条件分页获取 gateway 节点集群列表",tags = "")
    public PaginationResult<GatewayClusterNodeVO> pageGetGatewayNodes(HttpServletRequest request,
                                                                      @RequestBody GatewayNodeConditionDTO condition) {
        return new PaginationResult<>(Collections.singletonList(new GatewayClusterNodeVO()), 10, 1, 10);
    }
}