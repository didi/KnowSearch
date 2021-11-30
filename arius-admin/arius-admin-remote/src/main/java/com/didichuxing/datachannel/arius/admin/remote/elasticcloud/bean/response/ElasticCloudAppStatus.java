package com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.response;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.ESAppGroupStatusDTO;
import com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.ESAppHostStatusDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ElasticCloudAppStatus extends BaseDTO {

   private static final long serialVersionUID = 8665383910585087185L;
   /**
    * 容器云任务类型 create(新建)/update(升级、重启)/scale(扩缩容)
    */
   private  String          type;

   /**
    * 任务ID
    */
   private  Integer         taskId;

   /**
    * 任务状态 doing/done
    */
   private  String          state;

   /**
    * 阶段
    */
   private  String          phase;

   /**
    * 容器更新状态	发布更新task有意义(升级、重启)
    */
   private List<ESAppHostStatusDTO> hostStatus;

   /**
    * 当前更新分组	发布更新task有意义(升级、重启)
    */
   private  Integer         currentGroup;

   /**
    * 集群名称
    */
   private  String          clusterName ;

   /**
    * 服务节点
    */
   private  String          namespace ;

   /**
    * 分组更新状态	发布更新task有意义(升级、重启)
    */
   private  List<ESAppGroupStatusDTO> groupStatus;

   /**
    * 当前更新容器index	发布更新task有意义(升级、重启)
    */
   private  Integer         currentIndex;

   /**
    * 容器个数
    */
   private  Integer         podCount;

   /**
    * 最大容器个数
    */
   private Integer          podQuotaCount;

   /**
    * 任务是否暂停
    */
   private Boolean          paused;

   /**
    * 任务是否关闭
    */
   private Boolean          closed;

   /**
    * pod列表    新建task有意义(创建、扩缩容)
    */
   private List<ElasticCloudPod>   pods;

   /**
    * 容器名集合
    */
   private List<String>     containers;
}
