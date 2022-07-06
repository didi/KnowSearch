package com.didichuxing.datachannel.arius.admin.common.bean.po.project;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *
 * @author d06679
 * @date 2019/3/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ESUserPO extends BasePO  {

    /**
     * 主键
     */
    private Integer id;



    /**
     * 是否是超管
     */
    private Integer isRoot;

    /**
     * 验证码
     */
    private String  verifyCode;


    /**
     * 备注
     */
    private String  memo;

    /**
     * 删除标志
     */
    private Integer isActive;

    /**
     * 查询限流值
     */
    private Integer queryThreshold;

    /**
     * 租户查询集群
     */
    private String  cluster;

    /**
     * 查询模式
     */
    private Integer searchType;

    /**
     * 数据中心
     */
    private String  dataCenter;
    private Integer projectId;
    /**
     * 默认显示
     */
    private Boolean defaultDisplay;




    /******************** 即将废弃字段 ***********************/

    private String  ip;

    private String  indexExp;
    @Deprecated
    private String  responsible;
    

}