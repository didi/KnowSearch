package com.didichuxing.datachannel.arius.admin.common.bean.entity.app;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.StringResponsible;

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
public class App extends BaseEntity implements StringResponsible {

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

    /******************** 即将废弃字段 ***********************/

    private String  ip;

    private String  indexExp;

}
