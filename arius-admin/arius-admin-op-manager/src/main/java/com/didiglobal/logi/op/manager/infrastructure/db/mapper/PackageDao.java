package com.didiglobal.logi.op.manager.infrastructure.db.mapper;

import com.didiglobal.logi.op.manager.infrastructure.db.PackagePO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-011 7:22 下午
 */
@Repository
public interface PackageDao {
    /**
     * 通过id获取PackagePo
     *
     * @param id
     * @return PackagePo
     */
    PackagePO findById(int id);

    /**
     * 通过name获取PackagePo
     *
     * @param name
     * @return PackagePo
     */
    PackagePO findByName(String name);

    /**
     * 通过version获取PackagePo
     *
     * @param version
     * @return PackagePo
     */
    PackagePO findByVersion(String version);

    /**
     * 新建安装包
     *
     * @param po
     * @return
     */
    int insert(PackagePO po);

    /**
     * 更新安装包
     *
     * @param po
     * @return 删除条数
     */
    int update(PackagePO po);

    /**
     * 查询安装包
     *
     * @param po
     * @return List<PackagePO>
     */
    List<PackagePO> query(PackagePO po);

    /**
     * 删除包
     *
     * @param id
     * @return 删除条数
     */
    int delete(int id);

    /**
     * 根据软件包类型获取软件包版本
     * @param packageType
     * @return
     */
    List<PackagePO> listPackageVersionByPackageType(Integer packageType);

    /**
     * 分页查询软件包列表
     * @param packagePO
     * @param from
     * @param size
     * @return
     */
    List<PackagePO> pagingByCondition(@Param("param") PackagePO packagePO, @Param("from") Long from, @Param("size") Long size);

    /**
     * 软件包总数
     * @param packagePO
     * @return
     */
    Long countByCondition(PackagePO packagePO);
}
