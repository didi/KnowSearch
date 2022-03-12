package com.didichuxing.datachannel.arius.admin.common.bean.po.cluster;

import org.apache.commons.lang3.StringUtils;

import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019/3/19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterPO extends BasePO {

    /**
     * 集群id
     */
    private Integer id;

    /**
     * 集群名字
     */
    private String  cluster;

    /**
     * 描述
     */
    private String  desc;

    /**
     * 读地址
     */
    private String  readAddress;

    /**
     * 写地址
     */
    private String  writeAddress;

    /**
     * http地址
     */
    private String  httpAddress;

    /**
     * http写地址
     */
    private String  httpWriteAddress;

    /**
     * 集群类型
     * @see ESClusterTypeEnum
     */
    private Integer type;

    /**
     * 数据中心
     */
    private String  dataCenter;

    /**
     * 机房
     */
    private String  idc;

    /**
     * 服务等级
     */
    private Integer level;

    /**
     * es版本
     */
    private String  esVersion;

    /**
     * 配置包Id
     */
    private Long    cfgId;

    /**
     * 插件Id列表
     */
    private String  plugIds;

    /**
     * 程序包Id
     */
    private Long    packageId;

    /**
     * 集群创建人
     */
    private String  creator;

    /**
     * 集群支持的索引服务id，英文逗号分隔
     */
    private String  templateSrvs;

    /**
     * 镜像名
     */
    private String  imageName;
    /**
     * odin 树节点
     */
    private String  nsTree;

    /**
     * 集群密码
     */
    private String  password;

    /**
     * client运行模式，读写共享还是读写分离
     */
    private Integer runMode;

    /**
     * 读写分离时候，指定action（获取写client）
     */
    private String writeAction;

    /**
     * 健康状态 1 green 2 yellow 3 red -1 未知
     */
    private Integer health;

    @Override
    public boolean equals(Object o) {
        ClusterPO other = (ClusterPO)o;

        if (other == null) {
            return false;
        } else if (StringUtils.isNotBlank(httpAddress) && !httpAddress.equals(other.getHttpAddress())) {
            return false;
        } else {
            return !StringUtils.isNotBlank(other.getHttpAddress()) || other.getHttpAddress().equals(httpAddress);
        }
    }
}
