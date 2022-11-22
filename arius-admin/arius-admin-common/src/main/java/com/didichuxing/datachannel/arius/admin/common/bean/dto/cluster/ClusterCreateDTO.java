package com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * es 集群创建任务处理程序
 *
 * @author shizeying
 * @date 2022/11/20
 * @since 0.3.2
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClusterCreateDTO extends BaseDTO {
		
		/**
		 * @see ESClusterTypeEnum
		 */
		@ApiModelProperty("集群类型 (-1 未知 3 docker 集群 4 host 集群 5 VM 集群)")
		private Integer type;
		
		@ApiModelProperty("集群名字")
		private String cluster;
		
		@ApiModelProperty("es 版本")
		private String esVersion;
		
		@ApiModelProperty("集群角色 对应主机列表")
		private List<ESClusterRoleHostDTO> roleClusterHosts;
		
		@ApiModelProperty("描述")
		private String phyClusterDesc;
		
		@ApiModelProperty("集群认证信息：'user:password'")
		private String password;
		
		@ApiModelProperty("集群展示用属性标签，如「集群所属资源类型」等等")
		private String tags;
		
		@ApiModelProperty("数据中心")
		private String dataCenter;
		
		@ApiModelProperty("IaaS 平台类型")
		private String platformType;
		
		@ApiModelProperty("集群资源类型 (-1 未知 1 共享 2 独立 3 独享)")
		private Integer resourceType;
		
		@ApiModelProperty("region 划分方式，为空代表根据节点名称划分，否则为 attribute 属性")
		private String  divideAttributeKey;
		@ApiModelProperty("组件 ID")
		private Integer componentId;
		@ApiModelProperty("代理地址")
		private String  proxyAddress;
		@ApiModelProperty("集群类型")
		private String clusterType;
		@ApiModelProperty("是否接入 ecm")
		private Boolean ecmAccess;

}