package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.software;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.software.PackageAddDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.software.PackageQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.software.PackageUpdateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.software.PackageQueryVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.software.PackageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(V3_OP + "/software/package")
@Api(tags = "软件中心接口(REST)")
public class PackageV3Controller {
    @PostMapping("/page")
    @ApiOperation(value = "获取安装包分页列表接口")
    public PaginationResult<PackageQueryVO> pageGetPackages(@RequestBody PackageQueryDTO packageDTO) {
        return new PaginationResult<>();
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据安装包id获取安装包接口")
    public Result<PackageVO> getPackageByPackageId(@PathVariable Long id) {
        return new Result<>();
    }

    @PostMapping("")
    @ApiOperation(value = "新增安装包接口", notes = "")
    public Result<Long> savePackage(HttpServletRequest request, @RequestBody PackageAddDTO packageAddDTO) {
        return new Result<>();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改安装包接口", notes = "")
    public Result<Long> updatePackage(HttpServletRequest request, @RequestBody PackageUpdateDTO packageUpdateDTO) {
        return new Result<>();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除安装包接口", notes = "")
    public Result<Long> deletePackage(HttpServletRequest request, @PathVariable Long id) {
        return new Result<>();
    }
    @GetMapping("/{id}/using")
    @ApiOperation(value = "是否正在使用安装包", notes = "")
    public Result<Boolean> isUsingPackage(HttpServletRequest request, @PathVariable Long id) {
        return new Result<>();
    }
}