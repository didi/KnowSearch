package com.didichuxing.datachannel.arius.admin.client.bean.common;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ESPlugin {
    /**
     * ID主键自增
     */
    private Long id;

    /**
     * 插件名
     */
    private String name;

    /**
     * 物理集群Ids
     */
    private String physicClusterIds;

    /**
     * 插件版本
     */
    private String version;

    /**
     * 插件存储地址
     */
    private String url;

    /**
     * 插件文件md5
     */
    private String md5;

    /**
     * 插件描述
     */
    private String desc;

    /**
     * 插件创建人
     */
    private String creator;

    /**
     * 上传的文件名
     */
    private String fileName;

    /**
     * 上传的文件
     */
    private MultipartFile uploadFile;

    /**
     * 是否为系统默认： 0 否  1 是
     */
    private Boolean pDefault;

    /**
     * 是否安装
     */
    private Boolean installed;
}