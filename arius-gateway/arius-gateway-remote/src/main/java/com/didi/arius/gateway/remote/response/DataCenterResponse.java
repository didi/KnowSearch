package com.didi.arius.gateway.remote.response;

import lombok.Data;

/**
 * @author weizijun
 * @date：2016年10月31日
 */
@Data
public class DataCenterResponse {
    /**
     *
     */
    private long id;
    /**
     *
     */
    private String cluster;
    /**
     *
     */
    private String readAddress;
    /**
     *
     */
    private String httpAddress;
    /**
     *
     */
    private String writeAddress;
    /**
     *
     */
    private String httpWriteAddress;
    /**
     *
     */
    private String desc;
    /**
     *
     */
    private int type;
    /**
     *
     */
    private String esVersion;
    /**
     *
     */
    private String password;

}
