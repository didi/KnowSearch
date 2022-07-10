package com.didichuxing.datachannel.arius.admin.rest.controller.v3.project;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.biz.project.ProjectLogicTemplateAuthManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectTemplateAuthDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.ProjectTemplateAuthVO;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
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
@RequestMapping({V3 + "/project/auth/template" })
@Api(tags = "project模板权限接口(REST)")
public class ProjectTemplateAuthV3Controller {
    
    @Autowired
    private ProjectLogicTemplateAuthManager projectLogicTemplateAuthManager;

    @GetMapping("/{projectId}")
    @ResponseBody
    @ApiOperation(value = "获取project权限接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "projectId", value = "应用ID", required = true) })
    public Result<List<ProjectTemplateAuthVO>> getAppTemplateAuths(@PathVariable("projectId") Integer projectId) {
       
        return projectLogicTemplateAuthManager.getProjectTemplateAuths(projectId);
    }

    @PostMapping("")
    @ResponseBody
    @ApiOperation(value = "增加project权限接口" )
    public Result<Void> addTemplateAuth(HttpServletRequest request, @RequestBody ProjectTemplateAuthDTO authDTO) {
        return projectLogicTemplateAuthManager.addTemplateAuth(authDTO,HttpRequestUtil.getOperator(request),
                HttpRequestUtil.getProjectId(request));
    }

    @PutMapping("")
    @ResponseBody
    @ApiOperation(value = "更新project权限接口" )
    public Result<Void> updateTemplateAuth(HttpServletRequest request, @RequestBody ProjectTemplateAuthDTO authDTO) {
        return projectLogicTemplateAuthManager.updateTemplateAuth(authDTO, HttpRequestUtil.getOperator(request));
    }

    @DeleteMapping("/{authId}")
    @ResponseBody
    @ApiOperation(value = "删除project权限接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "authId", value = "权限ID", required = true) })
    public Result<Void> deleteTemplateAuth(HttpServletRequest request, @PathVariable("authId") Long authId) {
        return projectLogicTemplateAuthManager.deleteTemplateAuth(authId, HttpRequestUtil.getOperator(request),
                HttpRequestUtil.getProjectId(request));
    }

    @DeleteMapping("/redundancy")
    @ResponseBody
    @ApiOperation(value = "删除多余的模板权限数据" )
    public Result<Void> deleteRedundancyTemplateAuths() {
        return projectLogicTemplateAuthManager.deleteRedundancyTemplateAuths(true);
    }


}