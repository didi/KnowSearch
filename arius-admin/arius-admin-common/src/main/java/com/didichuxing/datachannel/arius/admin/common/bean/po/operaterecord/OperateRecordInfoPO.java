package com.didichuxing.datachannel.arius.admin.common.bean.po.operaterecord;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019/3/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperateRecordInfoPO extends BasePO {

    /**
     * 主键
     */
    private Integer id;

    /**
     * @see com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.NewModuleEnum
     */
    private Integer moduleId;

    /**
     * @see OperateTypeEnum
     */
    private Integer operateId;


    /**
     * 操作描述
     */
    private String  content;

    /**
     * 操作人
     */
    private String userOperation;

    /**
     * 操作时间
     */
    private Date    operateTime;
    /**
    * 触发方式
     * @see com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum
    */
    private Integer triggerWayId;
    /**
    * 应用id
    */
    private String  projectName;
    /**
     * 业务id
     */
    private Integer bizId;

}