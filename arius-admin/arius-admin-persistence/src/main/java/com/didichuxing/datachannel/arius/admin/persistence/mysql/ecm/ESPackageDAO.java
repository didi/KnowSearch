package com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm;

import com.didichuxing.datachannel.arius.admin.common.bean.po.espackage.ESPackagePO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 程序包版本管理 Dao 接口
 * @author didi
 * @since 2020-08-24
 */
@Repository
public interface ESPackageDAO {

    /**
     * 获取所有es package
     * @return es安装包list
     */
    List<ESPackagePO> listAll();

    /**
     * 新增es安装包
     * @param param 安装包
     * @return 插入成功数
     */
    int insert(ESPackagePO param);

    /**
     * 更新es安装包
     * @param param 安装包
     * @return 更新数
     */
    int update(ESPackagePO param);

    /**
     * 根据id获取es安装包
     * @param id 安装包id
     * @return es安装包po
     */
    ESPackagePO getById(Long id);

    /**
     * 通过版本和类型获取安装包
     * @param esVersion es版本
     * @param manifest 类型
     * @return es安装包po
     */
    ESPackagePO getByVersionAndType(@Param("esVersion") String esVersion, @Param("manifest") Integer manifest);

    /**
     * 通过es版本、类型以及id获取安装包
     * @param esVersion es版本
     * @param manifest 类型
     * @param id 安装包id
     * @return es安装包po
     */
    ESPackagePO getByVersionAndManifestNotSelf(@Param("esVersion") String esVersion,
                                               @Param("manifest") Integer manifest, @Param("id") Long id);

    /**
     * 通过id删除es安装包
     * @param id es版本id
     * @return 删除数量
     */
    int delete(Long id);
}
