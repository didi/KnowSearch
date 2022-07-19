package com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterLogic extends BaseEntity implements Comparable<ClusterLogic> {

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
     * 所属project ID
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
    private Integer dataNodeNu;

    /**
     * 责任人
     */
    @Deprecated
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

    /**
     * 健康状态 1 green 2 yellow 3 red -1 未知
     */
    private Integer health;

    /**
     * 磁盘使用率
     */
    private Double diskUsagePercent;

    /**
     * 磁盘总量
     */
    private Long diskTotal;

    /**
     * 磁盘使用量
     */
    private Long diskUsage;

    @Override
    public int compareTo(ClusterLogic o) {
        if (null == o) {
            return 0;
        }

        return o.getId().intValue() - this.getId().intValue();
    }
}