package com.didichuxing.datachannel.arius.admin.biz.software;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.software.PackageAddDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.software.PackageQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.software.PackageUpdateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.software.PackagePageVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.software.PackageQueryVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.software.PackageVersionVO;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;

import java.util.List;

public interface PackageManager {
    /**
     * 软件中心分页查询
     * @param packageDTO
     * @param projectId
     * @return
     */
    PaginationResult<PackagePageVO> pageGetPackages(PackageQueryDTO packageDTO, Integer projectId) throws NotFindSubclassException;

    /**
     * 根据id查询软件包
     * @param id
     * @return
     */
    Result<PackageQueryVO> getPackageById(Long id);

    /**
     * 新增软件包
     * @param packageAddDTO
     * @param operator
     * @param projectId
     * @return
     */
    Result<Boolean> addPackage(PackageAddDTO packageAddDTO, String operator, Integer projectId);

    /**
     * 更新软件包
     * @param packageUpdateDTO
     * @param operator
     * @param projectId
     * @return
     */
    Result<Boolean> updatePackage(PackageUpdateDTO packageUpdateDTO, String operator, Integer projectId);

    /**
     * 删除软件包
     * @param id
     * @param operator
     * @param projectId
     * @return
     */
    Result<Long> deletePackage(Long id, String operator, Integer projectId);

    /**
     * 是否正在使用软件包
     * @param id
     * @param operator
     * @param projectId
     * @return
     */
    Result<Boolean> isUsingPackage(Long id, String operator, Integer projectId);

    /**
     * 通过软件包类型获取软件包版本list
     * @param packageTypeDesc
     * @param projectId
     * @param currentVersion
     * @return
     */
    Result<List<PackageVersionVO>> listPackageWithHigherVersionByPackageTypeAndVersion(String packageTypeDesc, Integer projectId, String currentVersion);
}
