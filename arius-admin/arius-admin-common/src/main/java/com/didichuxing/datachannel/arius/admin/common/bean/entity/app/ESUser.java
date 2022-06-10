package com.didichuxing.datachannel.arius.admin.common.bean.entity.app;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author d06679
 * @date 2019/3/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ESUser extends BaseEntity   {

    private Integer id;

    private String  name;

    private Integer isRoot;

    private String  verifyCode;

    private String  departmentId;

    private String  department;

    private String  responsible;

    private String  memo;

    private Integer isActive;

    private Integer queryThreshold;

    private String  cluster;

    private Integer searchType;

    private String  dataCenter;
    private Integer projectId;
     private Boolean defaultDisplay;

    /******************** 即将废弃字段 ***********************/

    private String  ip;

    private String  indexExp;

}