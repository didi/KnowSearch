package com.didichuxing.datachannel.arius.admin.remote.storage.gift.bean;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GiftResponseDTO {

    @JSONField(name = "creation_time")
    private Integer creationTime;

    @JSONField(name = "download_url")
    private String  downloadUrl;

    @JSONField(name = "file_size")
    private Integer fileSize;

    @JSONField(name = "download_url_https")
    private String  downloadUrlHttps;

    @JSONField(name = "status_code")
    private Integer statusCode;

    @JSONField(name = "md5")
    private String  md5;

    @JSONField(name = "resource_key")
    private String  resourceKey;

    @JSONField(name = "status")
    private String  status;
}
