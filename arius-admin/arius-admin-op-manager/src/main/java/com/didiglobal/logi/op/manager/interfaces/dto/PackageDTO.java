package com.didiglobal.logi.op.manager.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author didi
 * @date 2022-07-11 2:55 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PackageDTO {
    /**
     * 安装包id
     */
    private Integer id;
    /**
     * 安装包名字
     */
    private String name;
    /**
     * 地址
     */
    private String url;
    /**
     * 版本
     */
    private String version;
    /**
     * 描述
     */
    private String describe;
    /**
     * 类型，0是配置依赖，1是配置独立
     */
    private Integer type;
    /**
     * 脚本id
     */
    private Integer scriptId;
    /**
     * 创建时间
     */
    private Timestamp createTime;
    /**
     * 更新时间
     */
    private Timestamp updateTime;
    /**
     * 创建者
     */
    private String creator;
    /**
     * 传输文件
     */
    private MultipartFile uploadFile;


    /**
     * 关联的默认安装包分组配置
     */
    private List<PackageGroupConfigDTO> groupConfigDTOList;
}
