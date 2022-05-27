package com.didichuxing.datachannel.arius.admin.rest.controller.v3.app;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.biz.app.AppLogicTemplateAuthManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.AppTemplateAuthDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.AppTemplateAuthVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ohushenglin_v
 */
@RestController
@RequestMapping({V3 + "/app/auth/template" })
@Api(tags = "App模板权限接口(REST)")
public class AppTemplateAuthV3Controller {

    @Autowired
    private AppLogicTemplateAuthService appLogicTemplateAuthService;

    @Autowired
    private IndexTemplateService indexTemplateService;

    @Autowired
    private AppLogicTemplateAuthManager appLogicTemplateAuthManager;

    @GetMapping("/{appId}")
    @ResponseBody
    @ApiOperation(value = "获取APP权限接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "appId", value = "应用ID", required = true) })
    public Result<List<AppTemplateAuthVO>> getAppTemplateAuths(@PathVariable("appId") Integer appId) {
        List<AppTemplateAuthVO> templateAuths = ConvertUtil
            .list2List(appLogicTemplateAuthService.getAppActiveTemplateRWAndRAuths(appId), AppTemplateAuthVO.class);

        fillTemplateAuthVO(templateAuths);

        return Result.buildSucc(templateAuths);
    }

    @PostMapping("")
    @ResponseBody
    @ApiOperation(value = "增加APP权限接口" )
    public Result<Void> addTemplateAuth(HttpServletRequest request, @RequestBody AppTemplateAuthDTO authDTO) {
        return appLogicTemplateAuthService.addTemplateAuth(authDTO, HttpRequestUtil.getOperator(request));
    }

    @PutMapping("")
    @ResponseBody
    @ApiOperation(value = "更新APP权限接口" )
    public Result<Void> updateTemplateAuth(HttpServletRequest request, @RequestBody AppTemplateAuthDTO authDTO) {
        return appLogicTemplateAuthManager.updateTemplateAuth(authDTO, HttpRequestUtil.getOperator(request));
    }

    @DeleteMapping("/{authId}")
    @ResponseBody
    @ApiOperation(value = "删除APP权限接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "authId", value = "权限ID", required = true) })
    public Result<Void> deleteTemplateAuth(HttpServletRequest request, @PathVariable("authId") Long authId) {
        return appLogicTemplateAuthService.deleteTemplateAuth(authId, HttpRequestUtil.getOperator(request));
    }

    @DeleteMapping("/redundancy")
    @ResponseBody
    @ApiOperation(value = "删除多余的模板权限数据" )
    public Result<Void> deleteRedundancyTemplateAuths() {
        return Result.build(appLogicTemplateAuthService.deleteRedundancyTemplateAuths(true));
    }

    /**
     * 给AppTemplateAuthVO设置所属逻辑集群ID、name，逻辑模板name
     * @param templateAuths 模板权限列表
     */
    private void fillTemplateAuthVO(List<AppTemplateAuthVO> templateAuths) {
        if (CollectionUtils.isEmpty(templateAuths)) {
            return;
        }

        // 涉及的逻辑模板id
        List<Integer> templateIds = templateAuths.stream().map(AppTemplateAuthVO::getTemplateId)
            .collect(Collectors.toList());

        Map<Integer, IndexTemplateLogicWithClusterAndMasterTemplate> logicTemplateMap = indexTemplateService
            .getLogicTemplatesWithClusterAndMasterTemplateMap(new HashSet<>(templateIds));

        for (AppTemplateAuthVO authVO : templateAuths) {
            Integer templateId = authVO.getTemplateId();
            IndexTemplateLogicWithClusterAndMasterTemplate logicTemplate = logicTemplateMap.get(templateId);
            if (logicTemplate != null) {
                // 逻辑模板信息
                authVO.setTemplateName(logicTemplate.getName());
                // 逻辑集群信息
                ClusterLogic logicCluster = logicTemplate.getLogicCluster();
                // 物理模板被删除后有可能没有集群信息
                if (logicCluster != null) {
                    authVO.setLogicClusterId(logicCluster.getId());
                    authVO.setLogicClusterName(logicCluster.getName());
                } else {
                    authVO.setLogicClusterName("");
                }
            } else {
                authVO.setTemplateName("");
            }
        }
    }
}