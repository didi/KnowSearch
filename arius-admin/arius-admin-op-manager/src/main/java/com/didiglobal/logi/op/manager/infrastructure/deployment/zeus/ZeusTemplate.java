package com.didiglobal.logi.op.manager.infrastructure.deployment.zeus;

import lombok.Data;

/**
 * @author didi
 * @date 2022-07-08 6:51 下午
 */
@Data
public class ZeusTemplate {
    private int id;
    private String keywords;
    private String account = "root";
    private int batch = 1;
    private int tolerance = 0;
    private String script;
    private Integer timeOut;

}