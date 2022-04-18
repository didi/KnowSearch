package com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.request;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ElasticCloudAppDeployGroupsDTO extends BaseDTO {

   private static final long serialVersionUID = 7952859746201227569L;
   /**
    * 分组  按照百分比进行分割
    * 该值表示每个分组的比例范围，例如：10,50,100 代表如下含义：
    * 第0分组一台容器（固定）
    *
    * 第1分组10%容器
    */
   private int group1;

   /**
    * 第2分组从10%更新到50%容器的边界，即更新集群总容器数40%的容器
    */
   private int group2;

   /**
    * 第3分组从50%更新到100%容器的容器边界，即更新集群总容器数50%的容器
    */
   private int group3;

}
