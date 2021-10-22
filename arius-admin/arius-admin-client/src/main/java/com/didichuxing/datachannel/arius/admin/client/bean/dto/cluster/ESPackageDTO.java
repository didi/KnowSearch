package com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ESPackageDTO extends BaseDTO {

    private Long          id;
    /**
     * 镜像地址或包地址
     */
    private String        url;

    /**
     * 程序包文件名
     */
    private String        fileName;

    /**
     * 文件md5
     */
    private String        md5;

    /**
     * 版本标识
     */
    private String        esVersion;

    /**
     * 类型( 3 docker/ 4 host)
     */
    private Integer       manifest;

    /**
     * 包创建人
     */
    private String        creator;
    /**
     *备注
     */
    private String        desc;

    /**
     * 上传配置包实体
     */
    private MultipartFile uploadFile;
}
