package com.didichuxing.datachannel.arius.admin.common.bean.po.cluster;

import com.didichuxing.datachannel.arius.admin.client.constant.resource.ResourceLogicTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.DigitResponsible;

import lombok.Data;

/**
 * @author d06679
 * @date 2019/3/22
 */
@Data
public class LogicClusterPO extends BasePO implements DigitResponsible {

    /**
     * 主键
     */
    private Long    id;

    /**
     * 名字
     */
    private String  name;

    /**
     * 类型
     * @see ResourceLogicTypeEnum
     */
    private Integer type;

    /**
     * 所属APPID
     */
    private Integer appId;

    /**
     * 数据中心
     */
    private String  dataCenter;

    /**
     * dataNode的规格
     */
    private String  dataNodeSpec;

    /**
     * dataNode的个数
     */
    private Integer  dataNodeNu;

    /**
     * 责任人，id列表，英文逗号分隔
     */
    private String  responsible;

    /**
     * 成本部门
     */
    private String  libraDepartmentId;

    /**
     * 成本部门
     */
    private String  libraDepartment;

    /**
     * 备注
     */
    private String  memo;

    /**
     * 独立资源的大小
     */
    private Double  quota;

    /**
     * 服务等级
     */
    private Integer level;

    /**
     * 配置
     */
    private String  configJson;
}
