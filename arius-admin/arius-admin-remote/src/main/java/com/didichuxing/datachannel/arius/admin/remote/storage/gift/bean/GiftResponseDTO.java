package com.didichuxing.datachannel.arius.admin.remote.storage.gift.bean;

/**
 * @author linyunan
 * @date 2021-05-21
 */
public class GiftResponseDTO {

    private Integer creation_time;

    private String  download_url;

    private Integer file_size;

    private String  download_url_https;

    private Integer status_code;

    private String  md5;

    private String  resource_key;

    private String  status;

    public String getDownload_url() {
        return download_url;
    }

    public Integer getStatus_code() {
        return status_code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
