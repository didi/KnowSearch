package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.software;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.software.PackageManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.software.PackageAddDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.software.PackageQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.software.PackageUpdateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.software.*;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didiglobal.knowframework.security.util.HttpRequestUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(V3_OP + "/software/package")
@Api(tags = "软件中心接口(REST)")
public class PackageV3Controller {
    @Autowired
    private PackageManager packageManager;

    @PostMapping("/page")
    @ApiOperation(value = "获取安装包分页列表接口")
    public PaginationResult<PackagePageVO> pageGetPackages(@RequestBody PackageQueryDTO packageDTO, HttpServletRequest request) throws NotFindSubclassException {
        return packageManager.pageGetPackages(packageDTO, HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据安装包id获取安装包接口")
    public Result<PackageQueryVO> getPackageById(@PathVariable Long id) {
        return packageManager.getPackageById(id);
    }

    @PostMapping("")
    @ApiOperation(value = "新增安装包接口", notes = "")
    public Result<Boolean> addPackage(HttpServletRequest request, PackageAddDTO packageAddDTO) {
        return packageManager.addPackage(packageAddDTO, HttpRequestUtil.getOperator(request),
                HttpRequestUtil.getProjectId(request));
    }

    @PostMapping("/update")
    @ApiOperation(value = "修改安装包接口", notes = "")
    public Result<Boolean> updatePackage(HttpServletRequest request, PackageUpdateDTO packageUpdateDTO) {
        return packageManager.updatePackage(packageUpdateDTO, HttpRequestUtil.getOperator(request),
                HttpRequestUtil.getProjectId(request));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除安装包接口", notes = "")
    public Result<Long> deletePackage(HttpServletRequest request, @PathVariable Long id) {
        return packageManager.deletePackage(id, HttpRequestUtil.getOperator(request),
                HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("/{id}/using")
    @ApiOperation(value = "是否正在使用安装包", notes = "")
    public Result<Boolean> isUsingPackage(HttpServletRequest request, @PathVariable Long id) {
        return packageManager.isUsingPackage(id, HttpRequestUtil.getOperator(request),
                HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("{packageTypeDesc}/versions")
    @ApiOperation(value = "安装包版本list", notes = "")
    public Result<List<PackageBriefVO>> listPackageVersionByPackageType(HttpServletRequest request, @PathVariable String packageTypeDesc) {
        return packageManager.listPackageVersionByPackageType(packageTypeDesc, HttpRequestUtil.getOperator(request), HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("{packageId}/higher-versions")
    @ApiOperation(value = "比当前版本高的安装包版本", notes = "")
    public Result<List<PackageBriefVO>> listPackageWithHigherVersion(HttpServletRequest request, @PathVariable Integer packageId, String currentVersion) {
        return packageManager.listPackageWithHigherVersion(HttpRequestUtil.getProjectId(request), currentVersion, packageId);
    }

    @GetMapping("/{version}/package-group-configs")
    @ApiOperation(value = "es安装包默认配置组", notes = "")
    public Result<List<PackageGroupConfigQueryVO>> listPackageGroupConfigByVersion(HttpServletRequest request, @PathVariable String version) {
        return packageManager.listPackageGroupConfigByVersion(version, HttpRequestUtil.getOperator(request),
                HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("/package-name/package-group-configs")
    @ApiOperation(value = "通过某个包类型的包名称和版本获取配置组列表", notes = "")
    public Result<List<PackageGroupConfigQueryVO>> listPackageGroupConfigByKey(HttpServletRequest request, String name, String version, String packageTypeDesc) {
        return packageManager.listPackageGroupConfigByKey(name, HttpRequestUtil.getProjectId(request), version, packageTypeDesc);
    }
}
