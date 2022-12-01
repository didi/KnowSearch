package com.didichuxing.datachannel.arius.admin.common.bean.po.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * @author d06679
 * @date 2019/3/19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterPhyPO extends BasePO {

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
     * 拓展字段,这里用于存放集群展示用属性标签，如「集群所属资源类型」等等
     */
    private String  tags;

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
    private String  writeAction;

    /**
     * 健康状态 0 green 1 yellow 2 red -1 未知
     */
    private Integer health;

    /**
     * 活跃的分片数目
     */
    private Long    activeShardNum;

    private Long    diskTotal;
    private Long    diskUsage;
    private Double  diskUsagePercent;

    /**
     * IaaS平台类型
     */
    private String  platformType;

    /**
     * 集群资源类型(-1 未知 1 共享 2 独立 3 独享)
     * @see ClusterResourceTypeEnum
     */

    private Integer resourceType;

    /**
     * gateway地址
     */
    private String  gatewayUrl;
    /**
     * 是否接入 gateway
     */
    private Boolean ecmAccess;
    
    /**
     * ecm 关联组建 id
     */
    private Integer componentId;
    
    /**
     * 代理地址
     */
    private String proxyAddress;
    
    /**
     * gateway ids, 逗号分割
     */
    private String gatewayIds;

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        ClusterPhyPO other = (ClusterPhyPO) obj;

        if (StringUtils.isNotBlank(httpAddress) && !httpAddress.equals(other.getHttpAddress())) {
            return false;
        } else if (StringUtils.isNotBlank(other.getHttpAddress()) && !other.getHttpAddress().equals(httpAddress)) {
            return false;
        } else if (StringUtils.isNotBlank(password) && !password.equals(other.getPassword())) {
            return false;
        } else if (StringUtils.isNotBlank(other.getPassword()) && !other.getPassword().equals(password)) {
            return false;
        } else if (StringUtils.isNotBlank(other.getEsVersion()) && !other.getEsVersion().equals(esVersion)) {
            return false;
        } else if (StringUtils.isNotBlank(esVersion) && !esVersion.equals(other.getEsVersion())) {
            return false;
        }

        return true;
    }
}