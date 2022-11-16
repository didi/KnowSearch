package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.software.PackageQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.software.PackagePageVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didiglobal.logi.op.manager.application.PackageService;
import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PackagePageSearchHandle extends AbstractPageSearchHandle<PackageQueryDTO, PackagePageVO>{
    @Autowired
    private PackageService packageService;
    @Override
    protected Result<Boolean> checkCondition(PackageQueryDTO condition, Integer projectId) {
        return Result.buildSucc();
    }

    @Override
    protected void initCondition(PackageQueryDTO condition, Integer projectId) {
        //do nothing
    }

    @Override
    protected PaginationResult<PackagePageVO> buildPageData(PackageQueryDTO queryDTO, Integer projectId) {
        Package pagingPackage = ConvertUtil.obj2Obj(queryDTO, Package.class);
        List<Package> packageList = null;
        Long count = 0L;
        try {
            packageList = packageService.pagingByCondition(pagingPackage, queryDTO.getPage(), queryDTO.getSize());
            count = packageService.countByCondition(pagingPackage);
        } catch (Exception e) {
            LOGGER.error("class=PackagePageSearchHandle||method=buildPageData||err={}",
                    e.getMessage(), e);
        }
        List<PackagePageVO> packagePageVOS = ConvertUtil.list2List(packageList, PackagePageVO.class);
        List<Integer> packageIds = packagePageVOS.stream().map(PackagePageVO::getId).collect(Collectors.toList());
        List<Integer> usingPackageIds = packageService.hasPackagesDependComponent(packageIds);
        packagePageVOS.forEach(packagePageVO -> {
            packagePageVO.setIsUsing(usingPackageIds.contains(packagePageVO.getId()));
        });
        return PaginationResult.buildSucc(packagePageVOS, count, queryDTO.getPage(), queryDTO.getSize());
    }
}
