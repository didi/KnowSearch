package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.software;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.ecm.PackageConditionDTO;
import com.didiglobal.logi.op.manager.interfaces.dto.PackageDTO;
import com.didiglobal.logi.op.manager.interfaces.vo.PackageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

@RestController
@RequestMapping(V3_OP + "/software/package")
@Api(tags = "软件中心接口(REST)")
public class PackageController {
    @GetMapping("/page")
    @ApiOperation(value = "获取安装包列表接口")
    public PaginationResult<PackageVO> pageGetPackages(@RequestBody PackageConditionDTO packageDTO) {
        return new PaginationResult<>();
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据安装包id获取安装包接口")
    public Result<PackageVO> getPackageByPackageId(@PathVariable Long id) {
        return new Result<>();
    }

    @PostMapping("")
    @ApiOperation(value = "新增安装包接口", notes = "")
    public Result<Long> savePackage(HttpServletRequest request, PackageDTO packageDTO) {
        return new Result<>();
    }

    @PutMapping("/update")
    @ApiOperation(value = "修改安装包接口", notes = "")
    public Result<PackageVO> updatePackage(HttpServletRequest request, @RequestBody PackageDTO packageDTO) {
        return new Result<>();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除安装包接口", notes = "")
    public Result<Long> deletePackage(HttpServletRequest request, @PathVariable Long id) {
        return new Result<>();
    }
}
