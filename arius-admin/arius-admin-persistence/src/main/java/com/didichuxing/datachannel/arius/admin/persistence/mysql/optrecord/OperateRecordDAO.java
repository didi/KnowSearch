package com.didichuxing.datachannel.arius.admin.persistence.mysql.optrecord;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.common.bean.po.operaterecord.OperateRecordPO;

/**
 * @author d06679
 * @date 2019/3/14
 */
@Repository
public interface OperateRecordDAO {

    List<OperateRecordPO> listByCondition(OperateRecordPO param);

    int insert(OperateRecordPO po);

    int compatible();
}
