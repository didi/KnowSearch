package com.didichuxing.datachannel.arius.admin.rest.controller.v2.thirdpart;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_THIRD_PART;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESPluginVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 *
 *
 * @author didi
 * @date 2019/3/13
 */
@RestController
@RequestMapping(V2_THIRD_PART + "/zeus")
@Api(value = "第三方Zeus接口(REST)")
public class ThirdpartZeusController {

    @Autowired
    private ESPluginService esPluginService;

    @GetMapping("/plugins/{clusterName}")
    @ResponseBody
    @ApiOperation(value = "获取集群插件信息", notes = "")
    public List<ESPluginVO> getPluginsByClusterName(@PathVariable String clusterName)  {
       return ConvertUtil.list2List(esPluginService.getPluginsByClusterName(clusterName),ESPluginVO.class);
    }
}
