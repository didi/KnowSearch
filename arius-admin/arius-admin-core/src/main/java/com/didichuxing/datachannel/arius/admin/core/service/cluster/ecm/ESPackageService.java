package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESPackageDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.espackage.ESPackage;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;

import java.util.List;

/**
 * 程序包版本管理 服务类
 * @author didi
 * @since 2020-08-24
 */
public interface ESPackageService {
    /**
     * 获取所有的package列表
     * @return package列表
     */
    List<ESPackage> listESPackage();

    /**
     * 创建一个Package
     * @param esPackageDTO dto
     * @param operator 操作者
     * @return 创建数量
     */
    Result<Long> addESPackage(ESPackageDTO esPackageDTO, String operator);

    /**
     * 修改ES package
     *
     * @param esPackageDTO dto
     * @param operator     操作者
     * @param projectId
     * @return 更新的es package
     */
    Result<ESPackage> updateESPackage(ESPackageDTO esPackageDTO, String operator, Integer projectId);

    /**
     * 根据id获取es package
     * @param id 安装包id
     * @return 安装包
     */
    ESPackage getESPackagePOById(Long id);

    /**
     * 根据id删除ES安装包
     * @param id es安装包id
     * @param operator 操作者
     * @return 删除es安装包数量
     */
    Result<Long> deleteESPackage(Long id, String operator) throws NotFindSubclassException;

    /**
     * 根据根据版本和类型查询一个程序包
     * @param esVersion es版本
     * @param manifest 类型host或者docker
     * @return es安装包
     */
    ESPackage getByVersionAndType(String esVersion, Integer manifest);
}