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

    List<ESPackagePO> listAll();

    int insert(ESPackagePO param);

    int update(ESPackagePO param);

    ESPackagePO getById(Long id);

    ESPackagePO getByVersionAndType(@Param("esVersion") String esVersion,
                                    @Param("manifest") Integer manifest);

    ESPackagePO getByVersionAndManifestNotSelf(@Param("esVersion") String esVersion,
                                               @Param("manifest") Integer manifest,
                                               @Param("id") Long id);

    int delete(Long id);


}
