package com.didichuxing.datachannel.arius.admin.persistence.mysql.app;

import com.didichuxing.datachannel.arius.admin.common.bean.po.app.ESUserConfigPO;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * esconfigdao
 *
 * @author shizeying
 * @date 2022/05/25
 */
@Repository
public interface ESUserConfigDAO {

    ESUserConfigPO getByESUser(@Param("esUser") int esUser);

    int insert(ESUserConfigPO param);

    int update(ESUserConfigPO param);

    List<ESUserConfigPO> listAll();
}