package com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord;

import java.util.Date;

import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *
 * @author d06679
 * @date 2019/3/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperateRecord extends BaseEntity {

    /**
     * 主键
     */
    private Integer id;

    /**
     * @see ModuleEnum
     */
    private Integer moduleId;

    /**
     * @see OperationEnum
     */
    private Integer operateId;

    /**
     * 操作业务id String类型
     */
    private String  bizId;

    /**
     * 操作描述
     */
    private String  content;

    /**
     * 操作人  邮箱前缀
     */
    private String  operator;

    /**
     * 操作时间
     */
    private Date    operateTime;

}
