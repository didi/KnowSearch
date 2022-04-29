package com.didichuxing.datachannel.arius.admin.common.bean.common.ecm;

import com.didichuxing.datachannel.arius.admin.common.constant.esconfig.EsConfigActionEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author lyn
 * @date 2021-01-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EsConfigAction implements Serializable {

    /**
     * 配置操作类型
     * @see EsConfigActionEnum
     */
    private Integer    actionType;

    /**
     * actionType = ADD、EDIT 时，表示的是变更之后的配置id, actionType = DELETE 时， 表示的是需要删除的配置id
     */
    private List<Long> actionEsConfigIds;

}
