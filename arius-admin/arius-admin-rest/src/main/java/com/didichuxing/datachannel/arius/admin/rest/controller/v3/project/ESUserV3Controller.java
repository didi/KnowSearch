package com.didichuxing.datachannel.arius.admin.rest.controller.v3.project;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.project.ESUserManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.ConsoleESUserVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.ESUserVO;
import com.didiglobal.logi.security.util.HttpRequestUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 项目关联的es user
 *
 * @author shizeying
 * @date 2022/05/26
 * @since 0.3
 */
@RestController
@RequestMapping({ V3 + "/es-user" })
@Api(tags = "应用关联es user (REST)")
public class ESUserV3Controller {

    @Autowired
    private ESUserManager esUserManager;

    @PostMapping("/{projectId}")
    @ResponseBody
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "projectId", value = "projectId", required = true) })
    @ApiOperation(value = "新增es user")
    public Result<Integer> createESUerByProject(HttpServletRequest request,
                                                @PathVariable("projectId") Integer projectId,
                                                @RequestBody ESUserDTO appDTO) {
        return esUserManager.registerESUser(appDTO, projectId, HttpRequestUtil.getOperator(request));
    }

    @GetMapping("/project/{projectId}")
    @ResponseBody
    @ApiOperation(value = "获取项目下的es user")
    @ApiImplicitParam(name = "projectId", value = "项目id", dataType = "String", required = true)
    public Result<List<ESUserVO>> listESUserByProjectId(HttpServletRequest request,
                                                        @PathVariable("projectId") String projectId) {

        return esUserManager.listESUsersByProjectId(projectId, request);
    }

    @DeleteMapping("/{projectId}/{esUser}")
    @ResponseBody
    @ApiOperation(value = "删除项目下指定的es user")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "path", dataType = "Integer", name = "projectId", value = "projectId", required = true),
                         @ApiImplicitParam(paramType = "path", dataType = "Integer", name = "esUser", value = "es user", required = true) })
    public Result<Void> deleteESUserByProject(HttpServletRequest request, @PathVariable("projectId") Integer projectId,
                                              @PathVariable("esUser") Integer esUserName) {

        return esUserManager.deleteESUserByProject(esUserName, projectId, HttpRequestUtil.getOperator(request));
    }

    @DeleteMapping("/{projectId}")
    @ResponseBody
    @ApiOperation(value = "删除项目下全部的es user")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "path", dataType = "Integer", name = "projectId", value = "projectId", required = true) })
    public Result<Void> deleteAllESUserByProject(HttpServletRequest request,
                                                 @PathVariable("projectId") Integer projectId) {

        return esUserManager.deleteAllESUserByProject(projectId, HttpRequestUtil.getOperator(request));
    }

    @PutMapping("")
    @ResponseBody
    @ApiOperation(value = "编辑es user接口", notes = "支持修改数据中心、备注")
    public Result<Void> update(HttpServletRequest request, @RequestBody ESUserDTO esUserDTO) {
        //获取操作用户
        String userName = HttpRequestUtil.getOperator(request);
        return esUserManager.editESUser(esUserDTO, userName);
    }

    @GetMapping("/{esUser}")
    @ResponseBody
    @ApiOperation(value = "获取es user详情接口")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "path", dataType = "Integer", name = "esUser", value = "esUser详情", required = true) })
    public Result<ConsoleESUserVO> get(@PathVariable("esUser") Integer esUser) {
        return esUserManager.get(esUser);
    }

}