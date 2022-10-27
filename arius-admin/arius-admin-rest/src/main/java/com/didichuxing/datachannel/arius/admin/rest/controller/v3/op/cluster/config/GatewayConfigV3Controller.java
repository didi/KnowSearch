package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.config;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.config.ConfigConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayConfigVO;
import com.didiglobal.logi.op.manager.interfaces.vo.GeneralGroupConfigHostVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 *
 * @author shizeying
 * @date 2022/10/24
 * @since 0.3.2
 */
@NoArgsConstructor
@RestController()
@RequestMapping(V3 + "/gateway/config")
@Api(tags = "GATEWAY CONFIG管理接口 (REST)")
public class GatewayConfigV3Controller {
    @PostMapping("/page")
    @ResponseBody
    @ApiOperation(value = "按条件分页获取 gateway 配置列表", tags = "")
    public PaginationResult<GatewayConfigVO> pageGetGatewayConfig(HttpServletRequest request,
                                                                  @RequestBody ConfigConditionDTO condition) {
        return new PaginationResult<>(Collections.singletonList(new GatewayConfigVO()), 10, 1, 10);
    }
    
    @GetMapping("/{gatewayClusterId}/{configId}")
    @ResponseBody
    @ApiOperation(value = "根据 configId 获取 gateway 配置信息", tags = "")
    public Result<GeneralGroupConfigHostVO> getConfigByGatewayId(HttpServletRequest request,
                                                                 @PathVariable("gatewayClusterId") Integer gatewayClusterId,
                                                                 @PathVariable("configId") Integer configId) {
        return Result.buildSucc(new GeneralGroupConfigHostVO());
    }
}