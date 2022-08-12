package com.didiglobal.logi.op.manager.interfaces.controller;

import com.didiglobal.logi.op.manager.application.PackageService;
import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.infrastructure.common.Constants;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.interfaces.assembler.PackageAssembler;
import com.didiglobal.logi.op.manager.interfaces.dto.PackageDTO;
import com.didiglobal.logi.op.manager.interfaces.vo.PackageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-11 2:38 下午
 */
@Controller
@Api(value = "安装包中心api")
@RequestMapping(Constants.API_PREFIX_V3 + "/package")
public class PackageController {
    @Autowired
    private PackageService packageService;

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageController.class);

    @PostMapping("")
    @ApiOperation(value = "新建安装包")
    public Result<Void> createPackage(PackageDTO packageDTO) {
        return packageService.createPackage(PackageAssembler.toDO(packageDTO));
    }

    @PostMapping("/query-package")
    @ApiOperation(value = "查询安装包")
    public Result<List<PackageVO>> queryPackage(@RequestBody PackageDTO packageDTO) {
        Result result = packageService.queryPackage(PackageAssembler.toDO(packageDTO));
        if (result.isSuccess()) {
            result.setData(PackageAssembler.toVOList((List<Package>) result.getData()));
        }
        return Result.success();
    }

    @PostMapping("edit")
    @ApiOperation(value = "编辑安装包")
    public Result<Void> editPackage( PackageDTO packageDTO) {
        return packageService.updatePackage(PackageAssembler.toDO(packageDTO));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除安装包")
    public Result<Void> deleteScript(@PathVariable Integer id) {
        return packageService.deletePackage(id);
    }

}
