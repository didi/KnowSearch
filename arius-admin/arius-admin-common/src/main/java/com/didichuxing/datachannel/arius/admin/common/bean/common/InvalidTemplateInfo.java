package com.didichuxing.datachannel.arius.admin.common.bean.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Deprecated
public class InvalidTemplateInfo {

    private Integer logicId;

    private String  name;

    private Integer appId;

    private String  responsible;

    private String  appResponsible;

    private Double  diskG;

}