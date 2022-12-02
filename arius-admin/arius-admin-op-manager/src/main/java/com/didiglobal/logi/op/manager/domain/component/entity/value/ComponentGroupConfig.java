package com.didiglobal.logi.op.manager.domain.component.entity.value;

import static com.didiglobal.logi.op.manager.infrastructure.common.Constants.REX;
import static com.didiglobal.logi.op.manager.infrastructure.common.Constants.SPLIT;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import java.sql.Timestamp;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.util.Strings;

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
        /**
     * 机器规格
     */
    private String    machineSpec;
    public ComponentGroupConfig create() {
        this.version = "1";
        this.createTime = new Timestamp(System.currentTimeMillis());
        this.updateTime = new Timestamp(System.currentTimeMillis());
        return this;
    }

    public ComponentGroupConfig createWithoutVersion() {
        this.createTime = new Timestamp(System.currentTimeMillis());
        this.updateTime = new Timestamp(System.currentTimeMillis());
        this.id = null;
        return this;
    }

    public ComponentGroupConfig updateExpandConfig(ComponentGroupConfig newConfig, Set<String> hosts) {
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

    public ComponentGroupConfig updateShrinkConfig(ComponentGroupConfig newConfig, Set<String> hosts) {
        this.updateTime = new Timestamp(System.currentTimeMillis());
        Set oriHostSet = Sets.newHashSet(this.hosts.split(SPLIT));
        hosts.forEach(oriHostSet::remove);
        this.setHosts(Strings.join(oriHostSet, REX));
        JSONObject processNumConfigJson = JSON.parseObject(this.getProcessNumConfig());
        JSONObject installDirectoryJson = JSON.parseObject(this.getInstallDirectoryConfig());
        JSON.parseObject(newConfig.getProcessNumConfig()).forEach((k, v) -> {
            if (hosts.contains(k)) {
                processNumConfigJson.remove(k);
            }
        });

        JSON.parseObject(newConfig.getInstallDirectoryConfig()).forEach((k, v) -> {
            if (hosts.contains(k)) {
                installDirectoryJson.remove(k);
            }
        });

        this.setProcessNumConfig(processNumConfigJson.toJSONString());
        this.setInstallDirectoryConfig(installDirectoryJson.toJSONString());
        return this;
    }

    public ComponentGroupConfig updateInstallConfig(Set<String> invalidHosts) {
        Set oriHostSet = Sets.newHashSet(this.hosts.split(SPLIT));
        JSONObject processNumConfigJson = JSON.parseObject(this.getProcessNumConfig());
        JSONObject installDirectoryJson = JSON.parseObject(this.getInstallDirectoryConfig());
        invalidHosts.forEach(host -> {
            oriHostSet.remove(host);
            processNumConfigJson.remove(host);
            installDirectoryJson.remove(host);
        });
        this.setHosts(Strings.join(oriHostSet, REX));
        this.setProcessNumConfig(processNumConfigJson.toJSONString());
        this.setInstallDirectoryConfig(installDirectoryJson.toJSONString());
        return this;
    }

    /**
     * 是否是相同配置
     * @param targetConfig
     * @return
     */
    public boolean isSame(ComponentGroupConfig targetConfig) {
        if (systemConfig.equals(targetConfig.getSystemConfig()) &&
                runningConfig.equals(targetConfig.getSystemConfig()) &&
                fileConfig.equals(targetConfig.getFileConfig()) &&
                processNumConfig.equals(targetConfig.getProcessNumConfig())) {
            return true;
        }
        return false;
    }
}