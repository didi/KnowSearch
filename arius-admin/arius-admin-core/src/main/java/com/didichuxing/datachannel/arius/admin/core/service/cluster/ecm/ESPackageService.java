package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESPackageDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.espackage.ESPackage;

/**
 * 程序包版本管理 服务类
 * @author didi
 * @since 2020-08-24
 */
public interface ESPackageService {
    /**
     * 获取所有的package列表
     */
    List<ESPackage> listESPackage();

    /**
     * 创建一个Package
     */
    Result<Long> addESPackage(ESPackageDTO esPackageDTO, String operator);

    /**
     * 修改 ESPackage
     */
    Result<ESPackage> updateESPackage(ESPackageDTO esPackageDTO, String operator);

    /**
     * 根据id获取指定ES安装包
     */
    ESPackage getESPackagePOById(Long id);

    /**
     * 根据id删除ES安装包
     */
    Result<Long> deleteESPackage(Long id, String operator);

    /**
     * 根据根据版本和类型查询一个程序包
     */
    ESPackage getByVersionAndType(String esVersion, Integer manifest);
}
