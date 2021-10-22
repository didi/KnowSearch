package com.didichuxing.datachannel.arius.admin.common.bean.po.ecm;



import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import lombok.Data;
/**
 * 容器规格列表
 * @author didi
 * @since 2020-08-24
 */
@Data
public class ESMachineNormsPO extends BasePO {

  private static final long serialVersionUID = 1L;

  private Long id;

  /**
   * 角色(masternode/datanode/clientnode/datanode-ceph)
   */
  private String role;

  /**
   * 规格(16-48Gi-100g)
   */
  private String spec;

  /**
   * 标记删除
   */
  private Boolean deleteFlag;


}
