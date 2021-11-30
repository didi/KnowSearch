package com.didichuxing.datachannel.arius.admin.common.bean.po.task;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WorkTask PO 对象
 * 
 * @author fengqiongfeng
 * @date 2020-12-21
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkTaskPO extends BasePO {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Integer id;

    /**
     * 标题 
     */
    private String title;

    /**
     * 任务类型
     */
    private Integer taskType;

    /**
     * 业务数据主键 
     */
    private Integer businessKey;

    /**
     * 任务状态：
     * success:成功 failed:失败
     * running:执行中 waiting:等待
     * cancel:取消 pause:暂停
     */
    private String status;

    /**
     * 创建人 
     */
    private String creator;

    /**
     * 标记删除 
     */
    private Boolean deleteFlag;

    /**
     * expandData 
     */
    private String expandData;

}

