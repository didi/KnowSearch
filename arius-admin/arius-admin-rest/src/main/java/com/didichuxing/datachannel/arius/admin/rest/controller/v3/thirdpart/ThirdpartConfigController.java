package com.didichuxing.datachannel.arius.admin.rest.controller.v3.thirdpart;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESZeusConfigDTO;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESClusterConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_THIRD_PART;

/**
 * @author lyn
 * @date 2020-12-29
 */
@RestController
@RequestMapping(V3_THIRD_PART + "/cluster")
@Api(value = "第三方访问接口(REST)")
public class ThirdpartConfigController {

    @Autowired
    private ESClusterConfigService esClusterConfigService;

    @GetMapping("/config/file")
    @ResponseBody
    @ApiOperation(value = "获取宙斯ES执行脚本配置内容", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster_name", value = "集群名称", required = true),
            @ApiImplicitParam(paramType = "query", dataType = "String", name = "engin_name", value = "组件名称", required = true),
            @ApiImplicitParam(paramType = "query", dataType = "String", name = "type_name", value = "配置名称", required = true)})
    public String getConfigDetail(@RequestParam(value = "cluster_name") String cluster,
                                  @RequestParam(value = "engin_name") String engin,
                                  @RequestParam(value = "type_name") String type) {
        return esClusterConfigService.getZeusConfigContent(buildESConfigZous(cluster, engin, type)).getData();
    }

    /***********************************************private********************************************************/

    private ESZeusConfigDTO buildESConfigZous(String cluster, String engin, String type) {
        ESZeusConfigDTO esZeusConfigDTO = new ESZeusConfigDTO();
        esZeusConfigDTO.setClusterName(cluster);
        esZeusConfigDTO.setEnginName(engin);
        esZeusConfigDTO.setTypeName(type);
        return esZeusConfigDTO;
    }
}
