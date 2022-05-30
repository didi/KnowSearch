package com.didichuxing.datachannel.arius.admin.common.bean.po.cluster;

import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.DigitResponsible;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterLogicPO extends BasePO implements DigitResponsible {

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
     * @see ClusterResourceTypeEnum
     */
    private Integer type;

    /**
     * 所属projectID
     */
    private Integer projectId;

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
     * 责任人
     */
    @Deprecated
    private String  responsible;



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

    /**
     * 健康状态 1 green 2 yellow 3 red -1 未知
     */
    private Integer health;
}