package com.didichuxing.datachannel.arius.admin.rest.controller.v2.thirdpart;

import com.didichuxing.datachannel.arius.admin.biz.thardpart.CommonManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.config.AriusConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ThirdpartAppVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ThirdPartClusterVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ThirdPartLogicClusterVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.config.ThirdpartConfigVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ThirdPartTemplateLogicWithMasterTemplateResourceVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ThirdpartTemplateLogicVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ThirdpartTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ThirdpartTemplateVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_THIRD_PART;

@RestController
@RequestMapping(V2_THIRD_PART + "/common")
@Api(tags = "第三方公共接口(REST)")
public class ThirdpartCommonController {

    @Autowired
    private CommonManager commonManager;

    @PostMapping("/operate/record/add")
    @ResponseBody
    @ApiOperation(value = "保存操作记录接口", notes = "")
    public Result<Void> addOperateRecord(@RequestBody OperateRecordDTO param) {
        return commonManager.addOperateRecord(param);
    }

    @GetMapping("/logicCluster/list")
    @ResponseBody
    @ApiOperation(value = "获取逻辑集群列表接口", notes = "获取逻辑集群列表")
    public Result<List<ThirdPartLogicClusterVO>> listLogicCluster() {
        return commonManager.listLogicCluster();
    }

    @GetMapping("/logicClusterWithRack/list")
    @ResponseBody
    @ApiOperation(value = "获取逻辑集群列表接口", notes = "获取逻辑集群列表(带上逻辑集群对应的rack信息)")
    public Result<List<ThirdPartLogicClusterVO>> listLogicClusterWithRack() {
        return commonManager.listLogicClusterWithRack();
    }

    @GetMapping("/logicCluster/query")
    @ResponseBody
    @ApiOperation(value = "根据物理集群名称获取rack匹配到的逻辑集群接口", notes = "根据物理集群名称获取rack匹配到的逻辑集群")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "物理集群名称", required = true),
                         @ApiImplicitParam(paramType = "query", dataType = "String", name = "rack", value = "rack", required = true) })
    public Result<ThirdPartLogicClusterVO> queryLogicCluster(@RequestParam("cluster") String cluster,
                                                             @RequestParam("rack") String rack) {
        return commonManager.queryLogicCluster(cluster, rack);

    }

    @GetMapping("/app/list")
    @ResponseBody
    @ApiOperation(value = "获取APP列表接口", notes = "获取app列表,包含APP全部元信息")
    public Result<List<ThirdpartAppVO>> listApp() {
        return commonManager.listApp();

    }

    @PutMapping("/app/verify")
    @ResponseBody
    @ApiOperation(value = "验证APP校验码接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "appId", value = "应用ID", required = true),
                         @ApiImplicitParam(paramType = "query", dataType = "String", name = "appsecret", value = "校验码", required = true) })
    public Result<Void> verifyApp(HttpServletRequest request, @RequestParam("appid") Integer appId,
                            @RequestParam("appsecret") String appSecret) throws UnsupportedEncodingException {
        return commonManager.verifyApp(request, appId, appSecret);

    }

    @GetMapping("/cluster/list")
    @ResponseBody
    @ApiOperation(value = "获取物理集群列表接口", notes = "")
    public Result<List<ThirdPartClusterVO>> listDataCluster() {
        return commonManager.listDataCluster();

    }

    @GetMapping("/cluster/getByName")
    @ResponseBody
    @ApiOperation(value = "获取集群接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "集群名称", required = true) })
    public Result<ThirdPartClusterVO> getDataCluster(@RequestParam("cluster") String cluster) {
        return commonManager.getDataCluster(cluster);

    }

    @PostMapping("/config/query")
    @ResponseBody
    @ApiOperation(value = "获取配置列表接口", notes = "")
    public Result<List<ThirdpartConfigVO>> queryConfig(@RequestBody AriusConfigInfoDTO param) {
        return commonManager.queryConfig(param);

    }

    @GetMapping("/template/listLogic")
    @ResponseBody
    @ApiOperation(value = "获取所有逻辑模板接口", notes = "")
    public Result<List<ThirdpartTemplateLogicVO>> listLogicTemplate() {
        return commonManager.listLogicTemplate();

    }

    @GetMapping("/template/listLogicWithMasterTemplateAndResource")
    @ResponseBody
    @ApiOperation(value = "获取所有逻辑模板接口", notes = "")
    public Result<List<ThirdPartTemplateLogicWithMasterTemplateResourceVO>> listLogicWithMasterTemplateAndResource() {
        return commonManager.listLogicWithMasterTemplateAndResource();

    }

    @GetMapping("/template/listLogicByName")
    @ResponseBody
    @ApiOperation(value = "获取所有逻辑模板接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "template", value = "逻辑模板名称", required = true) })
    public Result<List<ThirdpartTemplateLogicVO>> listLogicByName(@RequestParam("template") String template) {
        return commonManager.listLogicByName(template);

    }

    @GetMapping("/template/listPhysical")
    @ResponseBody
    @ApiOperation(value = "获取所有物理模板接口", notes = "")
    public Result<List<ThirdpartTemplatePhysicalVO>> listPhysicalTemplate() {
        return commonManager.listPhysicalTemplate();

    }

    @GetMapping("/template/listPhysicalWithLogic")
    @ResponseBody
    @ApiOperation(value = "获取所有物理模板接口", notes = "包含逻辑模板信息")
    public Result<List<ThirdpartTemplateVO>> listPhysicalWithLogic() {
        return commonManager.listPhysicalWithLogic();

    }

    @GetMapping("/template/getMasterByLogicId")
    @ResponseBody
    @ApiOperation(value = "获取主模板接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "逻辑模板ID", required = true) })
    public Result<ThirdpartTemplateVO> getMasterByLogicId(@RequestParam("logicId") Integer logicId) {
        return commonManager.getMasterByLogicId(logicId);

    }

    @GetMapping("/template/getPhysical")
    @ResponseBody
    @ApiOperation(value = "获取物理模板接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "physicalId", value = "物理模板ID", required = true) })
    public Result<ThirdpartTemplatePhysicalVO> getPhysicalTemplateById(@RequestParam("physicalId") Long physicalId) {
        return commonManager.getPhysicalTemplateById(physicalId);

    }

    @GetMapping("/template/listLogicByAppIdAuthDataCenter")
    @ResponseBody
    @ApiOperation(value = "获取授权的模板列表接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "appId", value = "应用ID", required = true),
                         @ApiImplicitParam(paramType = "query", dataType = "String", name = "auth", value = "权限（1:管理；2:读写；3:读）", required = true),
                         @ApiImplicitParam(paramType = "query", dataType = "String", name = "dataCenter", value = "数据中心") })
    public Result<List<ThirdpartTemplateLogicVO>> listLogicByAppIdAuthDataCenter(@RequestParam(value = "appId") Integer appId,
                                                                                 @RequestParam(value = "auth") String auth,
                                                                                 @RequestParam(value = "dataCenter", required = false) String dataCenter) {
        return commonManager.listLogicByAppIdAuthDataCenter( appId, auth, dataCenter );
    }
}
