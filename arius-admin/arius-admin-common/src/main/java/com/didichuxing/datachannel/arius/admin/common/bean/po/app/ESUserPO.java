package com.didichuxing.datachannel.arius.admin.common.bean.po.app;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.DigitResponsible;
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
public class ESUserPO extends BasePO implements DigitResponsible {

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
        /**
     * gateway返回结果解析开关
     */
    private Integer analyzeResponseEnable;

    /**
     * 是否生效DSL分析查询限流值 1为生效DSL分析查询限流值，0不生效DSL分析查询限流值
     */
    private Integer dslAnalyzeEnable;

    /**
     * 是否开启聚合分析  1 开启  0 不开启  默认为 1
     */
    private Integer aggrAnalyzeEnable;

    /**
     * 是否索引存储分离，1为分离，0为不分离，默认为0
     */
    private Integer isSourceSeparated;




    /******************** 即将废弃字段 ***********************/

    private String  ip;

    private String  indexExp;
    @Deprecated
    private String  responsible;
    

}