package com.didiglobal.logi.op.manager.domain.component.entity.value;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.util.Strings;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Set;

import static com.didiglobal.logi.op.manager.infrastructure.common.Constants.REX;

/**
 * @author didi
 * @date 2022-07-12 10:49 上午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComponentGroupConfig {
    /**
     * 配置id
     */
    private Integer id;
    /**
     * 组件id
     */
    private Integer componentId;
    /**
     * 分组名
     */
    private String groupName;
    /**
     * 系统配置
     */
    private String systemConfig;
    /**
     * 运行时配置
     */
    private String runningConfig;
    /**
     * 文件配置
     */
    private String fileConfig;
    /**
     * 安装目录配置
     */
    private String installDirectoryConfig;
    /**
     * 进程数配置
     */
    private String processNumConfig;
    /**
     * 节点列表
     */
    private String hosts;
    /**
     * 配置版本
     */
    private String version;
    /**
     * 创建时间
     */
    private Timestamp createTime;
    /**
     * 更新时间
     */
    private Timestamp updateTime;

    public ComponentGroupConfig create(){
        this.version = "1";
        this.createTime = new Timestamp(System.currentTimeMillis());
        this.updateTime = new Timestamp(System.currentTimeMillis());
        return this;
    }

    public ComponentGroupConfig createWithoutVersion(){
        this.createTime = new Timestamp(System.currentTimeMillis());
        this.updateTime = new Timestamp(System.currentTimeMillis());
        return this;
    }

    public ComponentGroupConfig updateExpand(ComponentGroupConfig newConfig, Set<String> hosts) {
        this.updateTime = new Timestamp(System.currentTimeMillis());
        this.setHosts(this.getHosts() + REX + Strings.join(hosts, REX));
        JSONObject processNumConfigJson = JSON.parseObject(this.getProcessNumConfig());
        JSONObject installDirectoryJson = JSON.parseObject(this.getInstallDirectoryConfig());
        JSON.parseObject(newConfig.getProcessNumConfig()).forEach((k, v) -> {
            if (hosts.contains(k)) {
                processNumConfigJson.put(k, v);
            }
        });

        JSON.parseObject(newConfig.getInstallDirectoryConfig()).forEach((k, v) -> {
            if (hosts.contains(k)) {
                installDirectoryJson.put(k, v);
            }
        });

        this.setProcessNumConfig(processNumConfigJson.toJSONString());
        this.setInstallDirectoryConfig(installDirectoryJson.toJSONString());
        return this;
    }
}
